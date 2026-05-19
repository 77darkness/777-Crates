package me.darkness.crates.crate.animation.impl;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.darkness.utilities.task.SchedulerUtil;
import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.CrateAnimation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public final class PlayerRouletteAnimation extends CrateAnimation {

    private final UUID p1;
    private final UUID p2;
    private final UUID forcedWinner;
    private final String title;

    private Gui gui;
    private ItemStack headP1;
    private ItemStack headP2;
    private int tick, delay = 2, step;
    private boolean stopping;

    public PlayerRouletteAnimation(CratesPlugin plugin, Player viewer, Crate crate,
                                   UUID p1, UUID p2, String title, UUID winner) {
        super(plugin, viewer, crate, null);
        this.p1 = p1;
        this.p2 = p2;
        this.title = title;
        this.forcedWinner = winner;
    }

    @Override
    public void start() {
        this.headP1 = buildHead(p1);
        this.headP2 = buildHead(p2);

        this.gui = Gui.gui()
                .title(TextUtil.toComponent(title))
                .rows(3)
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (isFinished()) return;
            SchedulerUtil.run(plugin, () -> {
                if (player.isOnline()) player.openInventory(gui.getInventory());
            });
        });

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
        SchedulerUtil.runLater(plugin, () -> {
            if (plugin.getBattleService() != null) {
                plugin.getBattleService().playerRouletteFinished(player.getUniqueId(), forcedWinner);
            }
            finish();
            player.closeInventory();
        }, 50L);
    }

    private void update(UUID uuid) {
        ItemStack head = uuid.equals(p1) ? headP1 : headP2;
        for (int s = 10; s <= 16; s++) {
            gui.updateItem(s, new GuiItem(head.clone()));
        }
    }

    private static ItemStack buildHead(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        String name = online != null ? online.getName() : "?";

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;

        if (online != null) {
            String texture = online.getPlayerProfile().getProperties().stream()
                    .filter(p -> p.getName().equals("textures"))
                    .map(ProfileProperty::getValue)
                    .findFirst()
                    .orElse(null);

            if (texture != null) {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), uuid.toString().substring(0, 16));
                profile.setProperty(new ProfileProperty("textures", texture));
                meta.setPlayerProfile(profile);
            }
        }

        meta.displayName(TextUtil.toComponent("&#FFFF00" + name));
        head.setItemMeta(meta);
        return head;
    }

    @Override public boolean isAnimation(Inventory inv) { return gui != null && gui.getInventory().equals(inv); }
    @Override public boolean isLocked() { return true; }
    @Override protected void onFinish() {}
    @Override protected void onCancel() {}
}