package me.darkness.crates.listener;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.inv.PreviewInv;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public final class PlayerInteractionListener implements Listener {

    private final CratesPlugin plugin;
    private final CrateService crateService;
    private final PreviewInv previewInv;

    public PlayerInteractionListener(CratesPlugin plugin, CrateService crateService) {
        this.plugin = plugin;
        this.crateService = crateService;
        this.previewInv = new PreviewInv(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        if (this.plugin.getAnimationService().hasActiveAnimation(event.getPlayer())) {
            return;
        }

        Location location = block.getLocation();
        Optional<Crate> crateOptional = this.crateService.getCrateByLocation(location);

        if (crateOptional.isEmpty()) {
            return;
        }

        event.setCancelled(true);

        Crate crate = crateOptional.get();
        this.previewInv.open(event.getPlayer(), crate);
    }
}
