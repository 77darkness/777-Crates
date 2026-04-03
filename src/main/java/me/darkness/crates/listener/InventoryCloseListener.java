package me.darkness.crates.listener;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.animation.CrateAnimation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public final class InventoryCloseListener implements Listener {

    private final CratesPlugin plugin;

    public InventoryCloseListener(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        CrateAnimation animation = this.plugin.getAnimationService().getActiveAnimation(player);
        if (animation == null || !animation.isAnimation(event.getInventory())) {
            return;
        }

        if (animation.isLocked()) {
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                CrateAnimation current = this.plugin.getAnimationService().getActiveAnimation(player);
                if (current == animation && player.isOnline()) {
                    player.openInventory(event.getInventory());
                }
            });
            return;
        }

        animation.cancelAnimation();
    }
}
