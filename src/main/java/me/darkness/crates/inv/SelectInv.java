package me.darkness.crates.inv;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.SelectInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.battle.BattleSession;
import me.darkness.crates.util.ItemBuilder;
import me.darkness.crates.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SelectInv {

    private final CratesPlugin plugin;
    private final BattleService service;

    public SelectInv(CratesPlugin plugin, BattleService service) {
        this.plugin = plugin;
        this.service = service;
    }

    public void open(Player challenger, Player target) {
        BattleSession battleSession = service.createSession(challenger.getUniqueId(), target.getUniqueId());

        final SelectInvConfig cfg = this.plugin.getConfigService().getBattleCrateSelectInv();
        final int rows = Math.max(1, Math.min(6, cfg.rows));
        final int size = rows * 9;

        Gui gui = Gui.gui()
                .title(Component.text(TextUtil.color(cfg.title)))
                .rows(rows)
                .disableAllInteractions()
                .create();

        if (cfg.items != null) {
            for (SelectInvConfig.GuiItem item : cfg.items.values()) {
                if (item == null) continue;
                if (item.slot < 0 || item.slot >= size) continue;

                String action = item.action == null ? "NONE" : item.action.toUpperCase(Locale.ROOT);
                ItemStack stack = ItemBuilder.of(item.material != null ? item.material : Material.BARRIER)
                        .amount(item.amount)
                        .placeholders(Map.of(
                                "opponent", target.getName()
                        ))
                        .name(item.name)
                        .lore(item.lore)
                        .build();

                gui.setItem(item.slot, new GuiItem(stack, e -> {
                    e.setCancelled(true);
                    if ("CLOSE".equals(action)) {
                        challenger.closeInventory();
                    }
                }));
            }
        }

        final List<Integer> preferredSlots = prepareDisplaySlots(cfg.crateSlots, size);

        int preferredIndex = 0;
        int scanIndex = 0;

        for (Crate crate : service.getAllCrates()) {
            if (crate == null) continue;

            Integer slot = null;

            while (preferredIndex < preferredSlots.size() && slot == null) {
                int candidate = preferredSlots.get(preferredIndex++);
                if (gui.getGuiItem(candidate) != null) continue;
                slot = candidate;
            }

            while (slot == null) {
                while (scanIndex < size && gui.getGuiItem(scanIndex) != null) {
                    scanIndex++;
                }
                if (scanIndex >= size) break;
                slot = scanIndex++;
            }

            if (slot == null || slot < 0 || slot >= size) break;

            Material mat = Material.CHEST;
            String matName = cfg.crateMaterial;
            if (matName != null && !matName.isBlank()) {
                try {
                    mat = Material.valueOf(matName.trim().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ignored) {
                }
            }

            ItemStack icon = ItemBuilder.of(new ItemStack(mat))
                    .placeholders(Map.of(
                            "crate", crate.getDisplayName(),
                            "opponent", target.getName()
                    ))
                    .name(cfg.crateItemName)
                    .lore(cfg.crateItemLore)
                    .build();

            int finalSlot = slot;
            gui.setItem(finalSlot, new GuiItem(icon, e -> {
                e.setCancelled(true);
                battleSession.setCrateName(crate.getName());
                new AmountInv(plugin, service).open(challenger);
            }));
        }

        gui.open(challenger);
    }

    private List<Integer> prepareDisplaySlots(String crateSlots, int guiSize) {
        List<Integer> preferredSlots = new ArrayList<>();
        if (crateSlots == null || crateSlots.isBlank()) {
            return preferredSlots;
        }

        Set<Integer> seen = new HashSet<>();
        for (String raw : crateSlots.split(",")) {
            String token = raw.trim();
            if (token.isEmpty()) continue;

            try {
                int slot = Integer.parseInt(token);
                if (slot < 0 || slot >= guiSize) continue;
                if (seen.add(slot)) preferredSlots.add(slot);
            } catch (NumberFormatException ignored) {
            }
        }

        return preferredSlots;
    }
}
