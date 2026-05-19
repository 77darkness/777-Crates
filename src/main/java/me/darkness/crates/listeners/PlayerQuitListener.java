package me.darkness.crates.listeners;

import me.darkness.crates.CratesPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerQuitListener implements Listener {

    private final CratesPlugin plugin;
    private final PlayerInteractionListener playerInteractionListener;

    public PlayerQuitListener(CratesPlugin plugin, PlayerInteractionListener playerInteractionListener) {
        this.plugin = plugin;
        this.playerInteractionListener = playerInteractionListener;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        java.util.UUID uuid = event.getPlayer().getUniqueId();
        this.plugin.getBattleService().cleanupForPlayer(uuid);
        this.plugin.getEditSessionManager().end(uuid);
        this.playerInteractionListener.clearPlayer(uuid);
    }
}
