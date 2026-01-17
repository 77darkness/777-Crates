package me.darkness.crates.crate.animation;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.impl.RouletteAnimation;
import me.darkness.crates.crate.animation.impl.WithoutAnimation;
import me.darkness.crates.crate.reward.CrateReward;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AnimationService {

    private final CratesPlugin plugin;
    private final Map<UUID, CrateAnimation> activeAnimations;

    public AnimationService(CratesPlugin plugin) {
        this.plugin = plugin;
        this.activeAnimations = new HashMap<>();
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
    }

    private CrateAnimation createAnimation(AnimationType type, Player player, Crate crate, CrateReward reward) {
        return switch (type) {
            case ROULETTE -> new RouletteAnimation(this.plugin, player, crate, reward);
            default -> new WithoutAnimation(this.plugin, player, crate, reward);
        };
    }

    public CrateAnimation getActiveAnimation(Player player) {
        return this.activeAnimations.get(player.getUniqueId());
    }
}
