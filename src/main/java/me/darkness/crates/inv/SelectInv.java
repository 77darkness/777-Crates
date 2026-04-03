package me.darkness.crates.inv;

import dev.darkness.utilities.item.ItemBuilder;
import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.SelectInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.battle.BattleSession;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class SelectInv {

    private final CratesPlugin plugin;
    private final BattleService service;

    public SelectInv(CratesPlugin plugin, BattleService service) {
        this.plugin = plugin;
        this.service = service;
    }

    public void open(Player challenger, Player target) {
        BattleSession battleSession = service.createSession(challenger.getUniqueId(), target.getUniqueId());
        SelectInvConfig cfg = this.plugin.getConfigService().getBattleCrateSelectInv();
        int rows = Math.max(1, Math.min(6, cfg.rows));
        int size = rows * 9;

        Gui gui = Gui.gui()
                .title(TextUtil.toComponent(cfg.title))
                .rows(rows)
                .disableAllInteractions()
                .create();

        if (cfg.items != null) {
            for (SelectInvConfig.GuiItem item : cfg.items.values()) {
                if (item == null || item.slot < 0 || item.slot >= size) continue;

                String action = item.action == null ? "NONE" : item.action.toUpperCase(Locale.ROOT);
                List<String> loreList = item.lore != null ? new ArrayList<>(item.lore) : new ArrayList<>();

                ItemStack stack = ItemBuilder.of(Objects.requireNonNullElse(item.material, Material.BARRIER))
                        .amount(item.amount)
                        .placeholders(Map.of("opponent", target.getName()))
                        .name(item.name)
                        .lore(loreList)
                        .build();

                gui.setItem(item.slot, new GuiItem(stack, e -> {
                    e.setCancelled(true);
                    if ("CLOSE".equals(action)) challenger.closeInventory();
                }));
            }
        }

        populateCrates(gui, challenger, target, battleSession, cfg, size);
        gui.open(challenger);
    }

    private void populateCrates(Gui gui, Player challenger, Player target, BattleSession session, SelectInvConfig cfg, int size) {
        Set<Integer> usedSlots = new HashSet<>();

        for (Crate crate : service.getAllCrates()) {
            if (crate == null) continue;

            SelectInvConfig.CrateItem crateConfig = cfg.crates.get(crate.getName());
            if (crateConfig == null || crateConfig.slot < 0 || crateConfig.slot >= size) continue;
            if (gui.getGuiItem(crateConfig.slot) != null || !usedSlots.add(crateConfig.slot)) continue;

            Material mat = Material.matchMaterial(Objects.requireNonNullElse(crateConfig.material, "CHEST"));
            if (mat == null) mat = Material.CHEST;

            ItemStack icon = ItemBuilder.of(mat)
                    .placeholders(Map.of("crate", crate.getDisplayName(), "opponent", target.getName()))
                    .name(crateConfig.displayName)
                    .lore(crateConfig.lore)
                    .build();

            gui.setItem(crateConfig.slot, new GuiItem(icon, event -> {
                event.setCancelled(true);
                session.setCrateName(crate.getName());
                new AmountInv(plugin, service).open(challenger);
            }));
        }
    }
}