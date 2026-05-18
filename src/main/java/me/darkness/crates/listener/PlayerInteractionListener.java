package me.darkness.crates.listener;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.inv.PreviewInv;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerInteractionListener implements Listener {

    private final CratesPlugin plugin;
    private final CrateService crateService;
    private final PreviewInv previewInv;
    private final Map<UUID, Long> lastOpen = new ConcurrentHashMap<>();
    private final Set<UUID> previewViewers = ConcurrentHashMap.newKeySet();

    public PlayerInteractionListener(CratesPlugin plugin, CrateService crateService, PreviewInv previewInv) {
        this.plugin = plugin;
        this.crateService = crateService;
        this.previewInv = previewInv;
    }

    public PreviewInv getPreviewInv() {
        return previewInv;
    }

    public void addPreviewViewer(UUID uuid) {
        previewViewers.add(uuid);
    }

    public void removePreviewViewer(UUID uuid) {
        previewViewers.remove(uuid);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;

        var block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        if (this.plugin.getAnimationService().hasActiveAnimation(player)) return;

        this.crateService.getCrateByLocation(block.getLocation()).ifPresent(crate -> {
            event.setCancelled(true);

            long now = System.currentTimeMillis();
            Long last = lastOpen.get(player.getUniqueId());
            if (last != null && now - last < 750L) return;
            lastOpen.put(player.getUniqueId(), now);

            previewViewers.add(player.getUniqueId());
            this.previewInv.open(player, crate);
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPreviewInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!previewViewers.contains(player.getUniqueId())) return;
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastOpen.remove(uuid);
        previewViewers.remove(uuid);
    }
}
