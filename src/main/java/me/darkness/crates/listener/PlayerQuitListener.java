package me.darkness.crates.listener;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.edit.EditSession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerQuitListener implements Listener {

    private final CratesPlugin plugin;

    public PlayerQuitListener(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.plugin.getBattleService().cleanupForPlayer(event.getPlayer().getUniqueId());
        EditSession.end(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        this.plugin.getBattleService().cleanupForPlayer(event.getPlayer().getUniqueId());
        EditSession.end(event.getPlayer().getUniqueId());
    }
}
