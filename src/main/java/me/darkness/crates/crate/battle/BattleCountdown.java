package me.darkness.crates.crate.battle;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class BattleCountdown implements Listener {

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
        Bukkit.getPluginManager().registerEvents(this, plugin);
        tick();
        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
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
        Lang lang = plugin.getConfigService().getLangConfig();

        if (a == null || b == null) {
            cancel(a, b, null);
            return;
        }

        int keysA = plugin.getKeyService().countKeys(a, challenge.getCrateName());
        int keysB = plugin.getKeyService().countKeys(b, challenge.getCrateName());

        if (keysA < challenge.getAmount() || keysB < challenge.getAmount()) {
            cancelled = true;
            cleanup();
            lang.battleCountdownCancelledBySelf.send(a);
            lang.battleCountdownCancelledByOpponent.send(b);
            a.playSound(a.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            b.playSound(b.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        Map<String, String> phA = Map.of("player", b.getName(), "seconds", String.valueOf(sec));
        Map<String, String> phB = Map.of("player", a.getName(), "seconds", String.valueOf(sec));

        lang.battleCountdown.send(a, phA);
        lang.battleCountdown.send(b, phB);

        a.playSound(a.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
        b.playSound(b.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
    }

    private void finish() {
        cleanup();
        onStart.run();
    }

    public void cancel(Player canceller, Player other, String cancellerName) {
        if (cancelled) return;
        cancelled = true;
        cleanup();

        Lang lang = plugin.getConfigService().getLangConfig();

        String otherName = other != null ? other.getName() : "";

        if (canceller != null && cancellerName != null) {
            lang.battleCountdownCancelledBySelf.send(canceller, Map.of("player", otherName));
        }
        if (other != null && cancellerName != null) {
            lang.battleCountdownCancelledByOpponent.send(other, Map.of("player", cancellerName));
        }

        if (canceller != null) canceller.playSound(canceller.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        if (other != null) other.playSound(other.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }

    private void cleanup() {
        cancelled = true;
        if (task != null && !task.isCancelled()) task.cancel();
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (cancelled) return;
        UUID playerId = event.getPlayer().getUniqueId();

        boolean isChallenger = playerId.equals(challenge.getChallenger());
        boolean isTarget = playerId.equals(challenge.getTarget());
        if (!isChallenger && !isTarget) return;

        if (!event.getMessage().trim().equalsIgnoreCase("anuluj")) return;

        event.setCancelled(true);

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (cancelled) return;
            Player self = Bukkit.getPlayer(playerId);
            Player other = isChallenger
                    ? Bukkit.getPlayer(challenge.getTarget())
                    : Bukkit.getPlayer(challenge.getChallenger());

            String selfName = self != null ? self.getName() : "";
            cancel(self, other, selfName);
        });
    }
}

