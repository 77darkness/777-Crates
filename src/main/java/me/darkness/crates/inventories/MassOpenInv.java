package me.darkness.crates.inventories;

import dev.darkness.utilities.item.ItemBuilder;
import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.inventories.MassOpenInvConfig;
import me.darkness.crates.configuration.LangConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.impl.MassRouletteAnimation;
import me.darkness.crates.crate.key.KeyService;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.reward.RewardRoller;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MassOpenInv {

    private final CratesPlugin plugin;

    public MassOpenInv(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Crate crate) {
        MassOpenInvConfig cfg = this.plugin.getConfigService().massOpenInv();
        int rows = Math.max(1, Math.min(6, cfg.rows));
        int size = rows * 9;

        Gui gui = Gui.gui()
                .title(TextUtil.toComponent(cfg.title.replace("{crate}", crate.getDisplayName())))
                .rows(rows)
                .disableAllInteractions()
                .create();

        if (cfg.items != null) {
            cfg.items.values().forEach(item -> {
                if (item == null || item.slot < 0 || item.slot >= size) return;
                String action = item.action == null ? "NONE" : item.action.toUpperCase(Locale.ROOT);
                gui.setItem(item.slot, new GuiItem(ItemBuilder.of(item.material)
                        .amount(item.amount)
                        .placeholders(Map.of("crate", crate.getDisplayName()))
                        .name(item.name)
                        .lore(item.lore != null ? new ArrayList<>(item.lore) : new ArrayList<>())
                        .build()));
                gui.addSlotAction(item.slot, event -> handleAction(player, crate, action));            });
        }

        gui.open(player);
    }

    private void handleAction(Player player, Crate crate, String action) {
        if ("NONE".equals(action)) return;
        if ("BACK".equals(action)) {
            this.plugin.getPreviewInv().open(player, crate);
            return;
        }

        if (!action.startsWith("AMOUNT_")) return;

        try {
            int amount = Integer.parseInt(action.substring(7));
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            startMassOpen(player, crate, Math.max(2, Math.min(6, amount)));
        } catch (NumberFormatException ignored) {}
    }

    private void startMassOpen(Player player, Crate crate, int amount) {
        LangConfig langConfig = this.plugin.getConfigService().lang();

        if (crate.getRewards().isEmpty()) {
            langConfig.crateNoRewards.send(player, Map.of("crate", crate.getDisplayName()));
            player.closeInventory();
            return;
        }

        if (this.plugin.getAnimationService().hasActiveAnimation(player)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (this.plugin.getRewardExecutor().countFreeSlots(player) < amount) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            langConfig.inventoryFull.send(player);
            player.closeInventory();
            return;
        }

        KeyService keyService = this.plugin.getKeyService();
        if (keyService.countKeys(player, crate.getName()) < amount || !keyService.takeKeys(player, crate.getName(), amount)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            langConfig.noKey.send(player, Map.of("crate", crate.getDisplayName(), "need", String.valueOf(amount)));
            player.closeInventory();
            return;
        }

        List<CrateReward> winners = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            CrateReward reward = RewardRoller.roll(crate.getRewards());
            if (reward != null) winners.add(reward);
        }

        if (winners.size() < 2) {
            winners.forEach(r -> this.plugin.getRewardExecutor().giveReward(player, crate, r));
            return;
        }

        player.closeInventory();
        this.plugin.getAnimationService().startAnimation(player, new MassRouletteAnimation(this.plugin, player, crate, winners));
    }
}