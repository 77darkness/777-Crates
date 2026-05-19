package me.darkness.crates.crate.battle;

import dev.darkness.utilities.task.SchedulerUtil;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.LangConfig;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class BattleCountdown {

    private final CratesPlugin plugin;
    private final Challenge challenge;
    private final Runnable onStart;
    private final AtomicInteger remaining;
    private BukkitTask task;
    private boolean cancelled = false;

    public BattleCountdown(CratesPlugin plugin, Challenge challenge, int seconds, Runnable onStart) {
        this.plugin = plugin;
        this.challenge = challenge;
        this.onStart = onStart;
        this.remaining = new AtomicInteger(seconds);
    }

    public void start() {
        tick();
        this.task = SchedulerUtil.runTimer(plugin, this::tick, 20L, 20L);
    }

    public void tryCancel(UUID playerId) {
        if (cancelled) return;
        if (!playerId.equals(challenge.getChallenger()) && !playerId.equals(challenge.getTarget())) return;

        boolean isChallenger = playerId.equals(challenge.getChallenger());
        Player self = Bukkit.getPlayer(playerId);
        Player other = isChallenger
                ? Bukkit.getPlayer(challenge.getTarget())
                : Bukkit.getPlayer(challenge.getChallenger());

        String selfName = self != null ? self.getName() : "";
        cancel(self, other, selfName);
        plugin.getBattleService().removeCountdown(challenge.getChallenger(), challenge.getTarget());
    }

    public void cancel(Player canceller, Player other, String cancellerName) {
        if (cancelled) return;
        cancelled = true;
        cleanup();

        LangConfig langConfig = plugin.getConfigService().lang();
        String otherName = other != null ? other.getName() : "";

        if (canceller != null && cancellerName != null) {
            langConfig.battleCountdownCancelledBySelf.send(canceller, Map.of("player", otherName));
        }
        if (other != null && cancellerName != null) {
            langConfig.battleCountdownCancelledByOpponent.send(other, Map.of("player", cancellerName));
        }

        if (canceller != null) canceller.playSound(canceller.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        if (other != null) other.playSound(other.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }

    private void finish() {
        cleanup();
        onStart.run();
    }

    private void tick() {
        if (cancelled) return;

        int sec = remaining.getAndDecrement();

        if (sec <= 0) {
            finish();
            return;
        }

        Player a = Bukkit.getPlayer(challenge.getChallenger());
        Player b = Bukkit.getPlayer(challenge.getTarget());
        LangConfig langConfig = plugin.getConfigService().lang();

        if (a == null || b == null) {
            cancel(a, b, null);
            plugin.getBattleService().removeCountdown(challenge.getChallenger(), challenge.getTarget());
            return;
        }

        int keysA = plugin.getKeyService().countKeys(a, challenge.getCrateName());
        int keysB = plugin.getKeyService().countKeys(b, challenge.getCrateName());

        if (keysA < challenge.getAmount() || keysB < challenge.getAmount()) {
            cancelled = true;
            cleanup();
            plugin.getBattleService().removeCountdown(challenge.getChallenger(), challenge.getTarget());
            langConfig.battleCountdownCancelledBySelf.send(a);
            langConfig.battleCountdownCancelledByOpponent.send(b);
            a.playSound(a.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            b.playSound(b.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        Map<String, String> phA = Map.of("player", b.getName(), "seconds", String.valueOf(sec));
        Map<String, String> phB = Map.of("player", a.getName(), "seconds", String.valueOf(sec));

        langConfig.battleCountdown.send(a, phA);
        langConfig.battleCountdown.send(b, phB);

        a.playSound(a.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
        b.playSound(b.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
    }

    private void cleanup() {
        cancelled = true;
        if (task != null && !task.isCancelled()) task.cancel();
    }
}
