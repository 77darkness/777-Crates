package me.darkness.crates.crate.animation.impl;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.CrateAnimation;
import me.darkness.crates.util.ItemStackSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;
import java.util.UUID;

public final class PlayerRouletteAnimation extends CrateAnimation {

    private final UUID p1;
    private final UUID p2;
    private final UUID forcedWinner;
    private final String title;
    private final Map<UUID, String> headBase64Cache;

    private Gui gui;
    private int tick = 0, delay = 2, step = 0;
    private boolean stopping = false;

    public PlayerRouletteAnimation(CratesPlugin plugin, Player viewer, Crate crate,
                                   UUID p1, UUID p2, Map<UUID, String> headBase64Cache,
                                   String title, UUID winner) {
        super(plugin, viewer, crate, null);
        this.p1 = p1;
        this.p2 = p2;
        this.headBase64Cache = headBase64Cache;
        this.title = title;
        this.forcedWinner = winner;
    }

    @Override
    public void start() {
        this.gui = Gui.gui()
                .title(TextUtil.toComponent(title))
                .rows(3)
                .disableAllInteractions()
                .create();
        this.gui.open(player);
    }

    @Override
    public void tick() {
        if (stopping) return;

        tick++;
        if (tick % delay == 0) {
            update(step % 2 == 0 ? p1 : p2);
            step++;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.5f);
            if (tick > 70 && tick % 6 == 0) delay++;
        }
        if (tick >= 100) stop();
    }

    private void stop() {
        stopping = true;
        update(forcedWinner);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.closeInventory();
            if (plugin.getBattleService() != null) {
                plugin.getBattleService().playerRouletteFinished(player.getUniqueId(), forcedWinner);
            }
            finish();
        }, 50L);
    }

    public static String buildHead(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online == null) return null;

        String name = online.getName();

        PlayerProfile sourceProfile = online.getPlayerProfile();
        String base64 = sourceProfile.getProperties().stream()
                .filter(p -> p.getName().equals("textures"))
                .map(ProfileProperty::getValue)
                .findFirst()
                .orElse(null);

        if (base64 == null) return null;

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return null;

        PlayerProfile freshProfile = Bukkit.createProfile(UUID.randomUUID(), uuid.toString().substring(0, 16));
        freshProfile.setProperty(new ProfileProperty("textures", base64));
        meta.setPlayerProfile(freshProfile);
        meta.displayName(TextUtil.toComponent("&#FFFF00" + name));
        head.setItemMeta(meta);

        return ItemStackSerializer.toBase64(head);
    }

    private void update(UUID uuid) {
        String base64 = headBase64Cache.get(uuid);
        ItemStack head = base64 != null
                ? ItemStackSerializer.fromBase64(base64)
                : fallbackHead(uuid);
        if (head == null) head = fallbackHead(uuid);

        for (int s = 10; s <= 16; s++) {
            gui.updateItem(s, new GuiItem(head));
        }
    }

    private ItemStack fallbackHead(UUID uuid) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            Player online = Bukkit.getPlayer(uuid);
            String name = online != null ? online.getName() : "?";
            meta.displayName(TextUtil.toComponent("&#FFFF00" + name));
            head.setItemMeta(meta);
        }
        return head;
    }

    @Override public boolean isAnimation(Inventory inv) { return gui != null && gui.getInventory().equals(inv); }
    @Override public boolean isLocked() { return true; }
    @Override protected void onFinish() {}
    @Override protected void onCancel() {}
}