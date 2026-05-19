package me.darkness.crates.listeners;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.inventories.PreviewInv;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerInteractionListener implements Listener {

    private final CratesPlugin plugin;
    private final CrateService crateService;
    private final PreviewInv previewInv;
    private final Map<UUID, Long> lastOpen = new ConcurrentHashMap<>();

    public PlayerInteractionListener(CratesPlugin plugin, CrateService crateService, PreviewInv previewInv) {
        this.plugin = plugin;
        this.crateService = crateService;
        this.previewInv = previewInv;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;

        var block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        if (plugin.getAnimationService().hasActiveAnimation(player)) return;

        crateService.getCrateByLocation(block.getLocation()).ifPresent(crate -> {
            event.setCancelled(true);

            long now = System.currentTimeMillis();
            Long last = lastOpen.get(player.getUniqueId());
            if (last != null && now - last < 750L) return;
            lastOpen.put(player.getUniqueId(), now);

            previewInv.open(player, crate);
        });
    }

    public void clearPlayer(UUID uuid) {
        lastOpen.remove(uuid);
    }
}
