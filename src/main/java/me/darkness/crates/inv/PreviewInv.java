package me.darkness.crates.inv;

import dev.darkness.utilities.item.ItemBuilder;
import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.PreviewInvConfig;
import me.darkness.crates.configuration.Lang;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.impl.WithoutAnimation;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.reward.RewardRoller;
import me.darkness.crates.crate.reward.SlottedCrateReward;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class PreviewInv {

    private final CratesPlugin plugin;

    public PreviewInv(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Crate crate) {
        PreviewInvConfig cfg = this.plugin.getConfigService().getPreviewInv();
        int rows = Math.max(1, Math.min(6, cfg.rows));
        int size = rows * 9;
        boolean[] used = new boolean[size];

        Gui gui = Gui.gui()
                .title(TextUtil.toComponent(cfg.title.replace("{crate}", crate.getDisplayName())))
                .rows(rows)
                .disableAllInteractions()
                .create();

        if (cfg.items != null) {
            cfg.items.values().forEach(item -> {
                if (item == null || item.slot < 0 || item.slot >= size) return;
                String action = item.action == null ? "NONE" : item.action.toUpperCase(Locale.ROOT);
                gui.setItem(item.slot, new GuiItem(createItem(item, crate), e -> {
                    e.setCancelled(true);
                    handleAction(player, crate, action);
                }));
                used[item.slot] = true;
            });
        }

        if (cfg.rewardPreviewLore != null) {
            placeSlottedRewards(gui, crate, cfg, size, used);
            placeUnslottedRewards(gui, crate, cfg, size, used);
        }

        gui.open(player);
    }

    private void placeSlottedRewards(Gui gui, Crate crate, PreviewInvConfig cfg, int size, boolean[] used) {
        for (CrateReward reward : crate.getRewards()) {
            if (!(reward instanceof SlottedCrateReward slotted) || reward.getDisplayItem() == null) continue;
            int slot = slotted.getSlot();
            if (slot < 0 || slot >= size || used[slot]) continue;
            gui.setItem(slot, new GuiItem(formatRewardItem(reward, crate, cfg), e -> e.setCancelled(true)));
            used[slot] = true;
        }
    }

    private void placeUnslottedRewards(Gui gui, Crate crate, PreviewInvConfig cfg, int size, boolean[] used) {
        int index = 0;
        for (CrateReward reward : crate.getRewards()) {
            if (reward == null || reward.getDisplayItem() == null || reward instanceof SlottedCrateReward) continue;
            while (index < size && used[index]) index++;
            if (index >= size) break;
            gui.setItem(index, new GuiItem(formatRewardItem(reward, crate, cfg), e -> e.setCancelled(true)));
            used[index++] = true;
        }
    }

    private void handleAction(Player player, Crate crate, String action) {
        switch (action) {
            case "OPEN_ANIMATED" -> openAnimated(player, crate);
            case "OPEN_WITHOUT_ANIMATION" -> WithoutAnimation.openWithoutAnimation(this.plugin, player, crate);
            case "MASS_OPEN" -> new MassOpenInv(this.plugin).open(player, crate);
            case "EDIT" -> new EditInv(this.plugin, this.plugin.getCrateService(), this.plugin.getCrateLoader()).open(player, crate);
            case "BACK" -> player.closeInventory();
        }
    }

    private void openAnimated(Player player, Crate crate) {
        Lang lang = this.plugin.getConfigService().getLangConfig();

        if (crate.getRewards().isEmpty()) {
            lang.crateNoRewards.send(player, Map.of("crate", crate.getDisplayName()));
            player.closeInventory();
            return;
        }

        if (this.plugin.getAnimationService().hasActiveAnimation(player)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (this.plugin.getRewardExecutor().countFreeSlots(player) < 1) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            lang.inventoryFull.send(player);
            player.closeInventory();
            return;
        }

        if (plugin.getKeyService().tryConsumeKey(player, crate.getName())) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            lang.noKey.send(player, Map.of("crate", crate.getDisplayName(), "need", "1"));
            return;
        }

        CrateReward reward = RewardRoller.roll(crate.getRewards());
        if (reward == null) {
            lang.crateNoRewards.send(player, Map.of("crate", crate.getDisplayName()));
            player.closeInventory();
            return;
        }

        player.closeInventory();
        this.plugin.getAnimationService().startAnimation(player, crate, reward);
    }

    private ItemStack formatRewardItem(CrateReward reward, Crate crate, PreviewInvConfig cfg) {
        ItemStack display = reward.getDisplayItem().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) return display;

        List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        Map<String, String> ph = Map.of("chance", String.valueOf(reward.getChance()), "crate", crate.getDisplayName());
        cfg.rewardPreviewLore.forEach(line -> lore.add(TextUtil.toComponent(TextUtil.applyPlaceholders(line, ph))));

        meta.lore(lore);
        display.setItemMeta(meta);
        return display;
    }

    private ItemStack createItem(PreviewInvConfig.GuiItem item, Crate crate) {
        return ItemBuilder.of(Objects.requireNonNullElse(item.material, Material.BARRIER))
                .placeholders(Map.of("crate", crate.getDisplayName()))
                .name(item.name)
                .lore(item.lore != null ? new ArrayList<>(item.lore) : new ArrayList<>())
                .build();
    }
}