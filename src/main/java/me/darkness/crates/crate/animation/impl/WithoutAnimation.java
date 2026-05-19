package me.darkness.crates.crate.animation.impl;

import dev.darkness.utilities.task.SchedulerUtil;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.LangConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.CrateAnimation;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.reward.RewardRoller;
import me.darkness.crates.inventories.WinInv;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;

public final class WithoutAnimation extends CrateAnimation {

    public static void openWithoutAnimation(CratesPlugin plugin, Player player, Crate crate) {
        if (plugin == null || player == null || crate == null) {
            return;
        }

        LangConfig langConfig = plugin.getConfigService().lang();

        if (crate.getRewards().isEmpty()) {
            langConfig.crateNoRewards.send(player, Map.of("crate", crate.getDisplayName()));
            player.closeInventory();
            return;
        }

        if (plugin.getAnimationService().hasActiveAnimation(player)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

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

        CrateReward reward = RewardRoller.roll(crate.getRewards());
        if (reward == null) {
            langConfig.crateNoRewards.send(player, Map.of("crate", crate.getDisplayName()));
            player.closeInventory();
            return;
        }

        player.closeInventory();
        plugin.getAnimationService().startAnimation(player, new WithoutAnimation(plugin, player, crate, reward));
    }

    public WithoutAnimation(CratesPlugin plugin, Player player, Crate crate, CrateReward reward) {
        super(plugin, player, crate, reward);
    }

    @Override
    public void start() {
        SchedulerUtil.run(plugin, () -> {
            this.player.closeInventory();
            new WinInv(this.plugin).open(this.player, this.crate, this.reward);

            this.finish();
        });
    }

    @Override protected void onFinish() {}

    @Override
    protected void onCancel() {
        SchedulerUtil.run(plugin, () -> {
            this.player.closeInventory();
            new WinInv(this.plugin).open(this.player, this.crate, this.reward);
        });
    }
}
