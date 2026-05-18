package me.darkness.crates.crate.animation;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.impl.RouletteAnimation;
import me.darkness.crates.crate.animation.impl.WithoutAnimation;
import me.darkness.crates.crate.reward.CrateReward;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AnimationService {

    private final CratesPlugin plugin;
    private final Map<UUID, CrateAnimation> activeAnimations;
    private org.bukkit.scheduler.BukkitTask globalTask;

    public AnimationService(CratesPlugin plugin) {
        this.plugin = plugin;
        this.activeAnimations = new HashMap<>();
        this.globalTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (activeAnimations.isEmpty()) return;
            for (CrateAnimation animation : new ArrayList<>(activeAnimations.values())) {
                animation.tick();
            }
        }, 1L, 1L);
    }

    public void startAnimation(Player player, Crate crate, CrateReward reward) {
        if (this.hasActiveAnimation(player)) {
            return;
        }

        CrateAnimation animation = this.createAnimation(crate.getAnimationType(), player, crate, reward);
        this.activeAnimations.put(player.getUniqueId(), animation);
        animation.start();
    }

    public void startCustomAnimation(Player player, CrateAnimation animation) {
        if (player == null || animation == null) {
            return;
        }
        if (this.hasActiveAnimation(player)) {
            return;
        }

        this.activeAnimations.put(player.getUniqueId(), animation);
        animation.start();
    }

    public boolean hasActiveAnimation(Player player) {
        return this.activeAnimations.containsKey(player.getUniqueId());
    }

    public void removeAnimation(Player player) {
        this.activeAnimations.remove(player.getUniqueId());
    }

    public void cancelAll() {
        this.activeAnimations.values().forEach(CrateAnimation::cancel);
        this.activeAnimations.clear();
        if (this.globalTask != null) {
            this.globalTask.cancel();
            this.globalTask = null;
        }
    }

    private CrateAnimation createAnimation(AnimationType type, Player player, Crate crate, CrateReward reward) {
        return switch (type) {
            case ROULETTE -> new RouletteAnimation(this.plugin, player, crate, reward);
            case WITHOUT -> new WithoutAnimation(this.plugin, player, crate, reward);
        };
    }

    public CrateAnimation getActiveAnimation(Player player) {
        return this.activeAnimations.get(player.getUniqueId());
    }
}
