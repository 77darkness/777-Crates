package me.darkness.crates.listener;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.animation.CrateAnimation;
import me.darkness.crates.crate.animation.impl.BattleRouletteAnimation;
import me.darkness.crates.crate.animation.impl.PlayerRouletteAnimation;
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

        if (!this.plugin.getAnimationService().hasActiveAnimation(player)) {
            return;
        }

        CrateAnimation animation = this.plugin.getAnimationService().getActiveAnimation(player);
        if (animation == null) {
            return;
        }

        if (!animation.isAnimation(event.getInventory())) {
            return;
        }

        if (animation instanceof BattleRouletteAnimation || animation instanceof PlayerRouletteAnimation) {
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                if (!this.plugin.getAnimationService().hasActiveAnimation(player)) {
                    return;
                }
                CrateAnimation stillActive = this.plugin.getAnimationService().getActiveAnimation(player);
                if (stillActive == null || stillActive != animation) {
                    return;
                }

                if (!player.isOnline()) {
                    return;
                }

                player.openInventory(event.getInventory());
            });
            return;
        }

        animation.cancelAnimation();
    }
}
