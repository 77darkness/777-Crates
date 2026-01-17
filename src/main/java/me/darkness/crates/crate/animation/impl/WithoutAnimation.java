package me.darkness.crates.crate.animation.impl;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.CrateAnimation;
import me.darkness.crates.crate.key.KeyService;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.reward.RewardRoller;
import me.darkness.crates.inv.WinInv;
import me.darkness.crates.util.TextUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;

public final class WithoutAnimation extends CrateAnimation {

    public static void openWithoutAnimation(CratesPlugin plugin, Player player, Crate crate) {
        if (plugin == null || player == null || crate == null) {
            return;
        }

        if (crate.getRewards().isEmpty()) {
            TextUtil.send(plugin.getConfigService().getLangConfig(), plugin, player,
                    plugin.getConfigService().getLangConfig().crateNoRewards,
                    Map.of("crate", crate.getDisplayName())
            );
            player.closeInventory();
            return;
        }

        if (plugin.getAnimationService().hasActiveAnimation(player)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        KeyService keyService = plugin.getKeyServiceProvider().get();
        if (keyService.tryConsumeKey(player, crate.getName())) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            TextUtil.send(plugin.getConfigService().getLangConfig(), plugin, player,
                    plugin.getConfigService().getLangConfig().noKeyInHand,
                    Map.of(
                            "crate", crate.getDisplayName(),
                            "need", "1"
                    )
            );
            return;
        }

        CrateReward reward = RewardRoller.roll(crate.getRewards());
        if (reward == null) {
            TextUtil.send(plugin.getConfigService().getLangConfig(), plugin, player,
                    plugin.getConfigService().getLangConfig().crateNoRewards,
                    Map.of("crate", crate.getDisplayName())
            );
            player.closeInventory();
            return;
        }

        player.closeInventory();
        plugin.getAnimationService().startCustomAnimation(player, new WithoutAnimation(plugin, player, crate, reward));
    }

    public WithoutAnimation(CratesPlugin plugin, Player player, Crate crate, CrateReward reward) {
        super(plugin, player, crate, reward);
    }

    @Override
    public void start() {
        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            this.player.closeInventory();
            new WinInv(this.plugin, this.plugin.getKeyServiceProvider().get())
                    .open(this.player, this.crate, this.reward);

            this.finish();
        });
    }

    @Override
    protected void onFinish() {
    }

    @Override
    protected void onCancel() {
        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            this.player.closeInventory();
            new WinInv(this.plugin, this.plugin.getKeyServiceProvider().get())
                    .open(this.player, this.crate, this.reward);
        });
    }
}
