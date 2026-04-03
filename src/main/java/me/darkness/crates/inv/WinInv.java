package me.darkness.crates.inv;

import dev.darkness.utilities.item.ItemBuilder;
import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.WinInvConfig;
import me.darkness.crates.configuration.Lang;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.reward.RewardRoller;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class WinInv {

    private final CratesPlugin plugin;

    public WinInv(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Crate crate, CrateReward reward) {
        WinInvConfig cfg = this.plugin.getConfigService().getWinInv();
        int rows = Math.max(1, Math.min(6, cfg.rows));
        int rewardSlot = cfg.rewardSlot > 0 ? cfg.rewardSlot : 13;

        Gui gui = Gui.gui()
                .title(TextUtil.toComponent(cfg.title))
                .rows(rows)
                .disableAllInteractions()
                .create();

        placeReward(gui, reward, rewardSlot);
        placeGuiItems(gui, cfg, player, crate);

        this.plugin.getRewardExecutor().giveReward(player, crate != null ? crate.getName() : "", reward);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        gui.open(player);
    }

    private void placeReward(Gui gui, CrateReward reward, int slot) {
        if (reward.getDisplayItem() != null && slot >= 0 && slot < gui.getRows() * 9) {
            gui.setItem(slot, new GuiItem(reward.getDisplayItem().clone()));
        }
    }

    private void placeGuiItems(Gui gui, WinInvConfig cfg, Player player, Crate crate) {
        if (cfg.items == null) return;
        int size = gui.getRows() * 9;

        cfg.items.values().forEach(item -> {
            if (item == null || item.slot < 0 || item.slot >= size) return;

            String action = item.action == null ? "NONE" : item.action.toUpperCase(Locale.ROOT);
            gui.setItem(item.slot, new GuiItem(createItem(item), e -> {
                e.setCancelled(true);
                handleAction(action, player, crate);
            }));
        });
    }

    private void handleAction(String action, Player player, Crate crate) {
        Lang lang = this.plugin.getConfigService().getLangConfig();

        switch (action) {
            case "CLOSE" -> player.closeInventory();
            case "REOPEN" -> {
                if (plugin.getKeyService().tryConsumeKey(player, crate.getName())) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    lang.noKey.send(player, Map.of("crate", crate.getDisplayName(), "need", "1"));
                    return;
                }

                CrateReward newReward = RewardRoller.roll(crate.getRewards());
                if (newReward == null) {
                    lang.crateNoRewards.send(player, Map.of("crate", crate.getDisplayName()));
                    player.closeInventory();
                    return;
                }

                open(player, crate, newReward);
            }
        }
    }

    private ItemStack createItem(WinInvConfig.GuiItem item) {
        return ItemBuilder.of(Objects.requireNonNullElse(item.material, Material.BARRIER))
                .name(item.name)
                .lore(item.lore != null ? new ArrayList<>(item.lore) : new ArrayList<>())
                .build();
    }
}