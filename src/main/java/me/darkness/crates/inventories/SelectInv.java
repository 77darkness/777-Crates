package me.darkness.crates.inventories;

import dev.darkness.utilities.item.ItemBuilder;
import dev.darkness.utilities.task.SchedulerUtil;
import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.inventories.SelectInvConfig;
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
        openInternal(challenger, target.getName(), battleSession, false);
    }

    public void openForOpen(Player creator) {
        BattleSession battleSession = service.createSession(creator.getUniqueId(), null);
        battleSession.setOpenMode(true);
        openInternal(creator, null, battleSession, true);
    }

    private void openInternal(Player challenger, String targetName, BattleSession battleSession, boolean isOpen) {
        SelectInvConfig cfg = this.plugin.getConfigService().selectInv();
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

                Map<String, String> ph = targetName != null
                        ? Map.of("opponent", targetName)
                        : Map.of("opponent", "");

                ItemStack stack = ItemBuilder.of(item.material)
                        .amount(item.amount)
                        .placeholders(ph)
                        .name(item.name)
                        .lore(loreList)
                        .build();

                gui.setItem(item.slot, new GuiItem(stack));

                gui.addSlotAction(item.slot, event -> {
                    switch (action) {
                        case "NONE" -> {}
                        case "CLOSE" -> challenger.closeInventory();
                        case "BACK" -> {
                            if (isOpen) {
                                challenger.closeInventory();
                                SchedulerUtil.run(plugin, () ->
                                        new BattleInv(plugin, service).open(challenger));
                            } else {
                                challenger.closeInventory();
                            }
                        }
                    }
                });
            }
        }

        fillCrates(gui, challenger, targetName, battleSession, cfg, size, isOpen);
        gui.open(challenger);
    }

    private void fillCrates(Gui gui, Player challenger, String targetName, BattleSession session,
                            SelectInvConfig cfg, int size, boolean isOpen) {
        Set<Integer> usedSlots = new HashSet<>();

        for (Crate crate : plugin.getCrateService().getAllCrates()) {
            if (crate == null) continue;

            SelectInvConfig.CrateItem crateConfig = cfg.crates.get(crate.getName());
            if (crateConfig == null || crateConfig.slot < 0 || crateConfig.slot >= size) continue;
            if (gui.getGuiItem(crateConfig.slot) != null || !usedSlots.add(crateConfig.slot)) continue;

            Material mat = Material.matchMaterial(Objects.requireNonNullElse(crateConfig.material, "CHEST"));
            if (mat == null) mat = Material.CHEST;

            Map<String, String> ph = targetName != null
                    ? Map.of("crate", crate.getDisplayName(), "opponent", targetName)
                    : Map.of("crate", crate.getDisplayName(), "opponent", "");

            ItemStack icon = ItemBuilder.of(mat)
                    .placeholders(ph)
                    .name(crateConfig.displayName)
                    .lore(crateConfig.lore)
                    .build();

            gui.setItem(crateConfig.slot, new GuiItem(icon));
            gui.addSlotAction(crateConfig.slot, event -> {
                session.setCrateName(crate.getName());
                if (isOpen) {
                    new AmountInv(plugin, service).openForOpen(challenger);
                } else {
                    new AmountInv(plugin, service).open(challenger);
                }
            });
        }
    }
}