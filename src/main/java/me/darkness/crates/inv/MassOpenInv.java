package me.darkness.crates.inv;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.MassOpenInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.impl.MassRouletteAnimation;
import me.darkness.crates.crate.key.KeyService;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.reward.RewardRoller;
import me.darkness.crates.util.ItemBuilder;
import me.darkness.crates.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
        final MassOpenInvConfig cfg = this.plugin.getConfigService().getMassOpenInv();

        final int rows = Math.max(1, Math.min(6, cfg.rows));

        final Gui gui = Gui.gui()
                .title(Component.text(TextUtil.color(cfg.title.replace("{crate}", crate.getDisplayName()))))
                .rows(rows)
                .disableAllInteractions()
                .create();

        int size = rows * 9;

        if (cfg.items != null) {
            for (MassOpenInvConfig.GuiItem item : cfg.items.values()) {
                if (item == null) continue;
                if (item.slot < 0 || item.slot >= size) continue;

                String action = item.action == null ? "NONE" : item.action.toUpperCase(Locale.ROOT);

                ItemStack stack = toItemStack(item, crate);

                gui.setItem(item.slot, new GuiItem(stack, e -> {
                    e.setCancelled(true);

                    if ("BACK".equals(action)) {
                        new PreviewInv(this.plugin).open(player, crate);
                        return;
                    }

                    if (action.startsWith("AMOUNT_")) {
                        int amount;
                        try {
                            amount = Integer.parseInt(action.substring("AMOUNT_".length()));
                        } catch (Exception ex) {
                            return;
                        }

                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
                        startMassOpen(player, crate, amount);
                    }
                }));
            }
        }

        gui.open(player);
    }

    private void startMassOpen(Player player, Crate crate, int amount) {
        if (crate.getRewards().isEmpty()) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
                    this.plugin.getConfigService().getLangConfig().crateNoRewards,
                    Map.of("crate", crate.getDisplayName())
            );
            player.closeInventory();
            return;
        }

        amount = Math.max(2, Math.min(6, amount));

        if (this.plugin.getAnimationService().hasActiveAnimation(player)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        KeyService keyService = this.plugin.getKeyServiceProvider().get();
        int keys = keyService.countKeys(player, crate.getName());
        if (keys < amount) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
                    this.plugin.getConfigService().getLangConfig().noKeyInHand,
                    Map.of(
                            "crate", crate.getDisplayName(),
                            "need", String.valueOf(amount)
                    )
            );
            return;
        }

        if (!keyService.takeKeys(player, crate.getName(), amount)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
                    this.plugin.getConfigService().getLangConfig().noKeyInHand,
                    Map.of(
                            "crate", crate.getDisplayName(),
                            "need", String.valueOf(amount)
                    )
            );
            return;
        }

        List<CrateReward> winners = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            CrateReward reward = RewardRoller.roll(crate.getRewards());
            if (reward != null) {
                winners.add(reward);
            }
        }

        if (winners.size() < 2) {
            for (CrateReward r : winners) {
                this.plugin.getRewardExecutor().giveReward(player, crate.getName(), r);
            }
            return;
        }

        player.closeInventory();

        MassRouletteAnimation animation = new MassRouletteAnimation(this.plugin, player, crate, winners);
        this.plugin.getAnimationService().startCustomAnimation(player, animation);
    }

    private ItemStack toItemStack(MassOpenInvConfig.GuiItem item, Crate crate) {
        if (item == null) {
            return ItemBuilder.of(Material.BARRIER).build();
        }

        return ItemBuilder.of(item.material != null ? item.material : Material.BARRIER)
                .amount(item.amount)
                .placeholders(Map.of("crate", crate.getDisplayName()))
                .name(item.name)
                .lore(item.lore)
                .build();
    }
}
