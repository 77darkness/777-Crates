package me.darkness.crates.crate.animation;

import dev.darkness.utilities.task.SchedulerUtil;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.impl.RouletteAnimation;
import me.darkness.crates.crate.animation.impl.WithoutAnimation;
import me.darkness.crates.crate.reward.CrateReward;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AnimationService {

    private final CratesPlugin plugin;
    private final Map<UUID, CrateAnimation> activeAnimations;
    private BukkitTask globalTask;

    public AnimationService(CratesPlugin plugin) {
        this.plugin = plugin;
        activeAnimations = new ConcurrentHashMap<>();
        globalTask = SchedulerUtil.runTimer(plugin, () -> {
            if (activeAnimations.isEmpty()) return;
            CrateAnimation[] snapshot = activeAnimations.values().toArray(new CrateAnimation[0]);
            for (CrateAnimation animation : snapshot) {
                animation.tick();
            }
        }, 1L, 1L);
    }

    public void startAnimation(Player player, Crate crate, CrateReward reward) {
        if (hasActiveAnimation(player)) {
            return;
        }

        CrateAnimation animation = createAnimation(crate.getAnimationType(), player, crate, reward);
        activeAnimations.put(player.getUniqueId(), animation);
        animation.start();
    }

    public void startAnimation(Player player, CrateAnimation animation) {
        if (player == null || animation == null) {
            return;
        }
        if (hasActiveAnimation(player)) {
            return;
        }

        activeAnimations.put(player.getUniqueId(), animation);
        animation.start();
    }

    public boolean hasActiveAnimation(Player player) {
        return activeAnimations.containsKey(player.getUniqueId());
    }

    public void removeAnimation(Player player) {
        activeAnimations.remove(player.getUniqueId());
    }

    public void cancelAll() {
        activeAnimations.values().forEach(CrateAnimation::cancel);
        activeAnimations.clear();
        if (globalTask != null) {
            globalTask.cancel();
            globalTask = null;
        }
    }

    private CrateAnimation createAnimation(AnimationType type, Player player, Crate crate, CrateReward reward) {
        return switch (type) {
            case ROULETTE -> new RouletteAnimation(plugin, player, crate, reward);
            case WITHOUT -> new WithoutAnimation(plugin, player, crate, reward);
        };
    }
}
