package me.darkness.crates.inventories;

import dev.darkness.utilities.item.ItemBuilder;
import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.inventories.WinInvConfig;
import me.darkness.crates.configuration.LangConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.reward.RewardRoller;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public final class WinInv {

    private final CratesPlugin plugin;

    public WinInv(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Crate crate, CrateReward reward) {
        WinInvConfig cfg = this.plugin.getConfigService().winInv();
        int rows = Math.max(1, Math.min(6, cfg.rows));
        int rewardSlot = cfg.rewardSlot > 0 ? cfg.rewardSlot : 13;

        Gui gui = Gui.gui()
                .title(TextUtil.toComponent(cfg.title))
                .rows(rows)
                .disableAllInteractions()
                .create();

        placeReward(gui, reward, rewardSlot);
        placeGuiItems(gui, cfg, player, crate);

        this.plugin.getRewardExecutor().giveReward(player, crate, reward);
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
            gui.setItem(item.slot, new GuiItem(ItemBuilder.of(item.material)
                    .name(item.name)
                    .lore(item.lore != null ? new ArrayList<>(item.lore) : new ArrayList<>())
                    .build()));

            gui.addSlotAction(item.slot, event -> handleAction(action, player, crate));
        });
    }

    private void handleAction(String action, Player player, Crate crate) {
        LangConfig langConfig = this.plugin.getConfigService().lang();

        switch (action) {
            case "NONE" -> {}
            case "CLOSE" -> player.closeInventory();
            case "REOPEN" -> {
                if (plugin.getRewardExecutor().countFreeSlots(player) < 1) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    langConfig.inventoryFull.send(player);
                    player.closeInventory();
                    return;
                }

                if (!plugin.getKeyService().tryConsumeKey(player, crate.getName())) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    langConfig.noKey.send(player, Map.of("crate", crate.getDisplayName(), "need", "1"));
                    player.closeInventory();
                    return;
                }

                CrateReward newReward = RewardRoller.roll(crate.getRewards());
                if (newReward == null) {
                    langConfig.crateNoRewards.send(player, Map.of("crate", crate.getDisplayName()));
                    player.closeInventory();
                    return;
                }

                open(player, crate, newReward);
            }
        }
    }
}