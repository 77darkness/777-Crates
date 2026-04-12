package me.darkness.crates.inv;

import dev.darkness.utilities.item.ItemBuilder;
import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.OpenBattleInvConfig;
import me.darkness.crates.configuration.Inv.SelectInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.battle.OpenChallenge;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class OpenBattleInv {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private final CratesPlugin plugin;
    private final BattleService battleService;

    public OpenBattleInv(CratesPlugin plugin, BattleService battleService) {
        this.plugin = plugin;
        this.battleService = battleService;
    }

    public void open(Player player) {
        OpenBattleInvConfig cfg = plugin.getConfigService().getOpenBattleInv();
        int rows = Math.max(1, Math.min(6, cfg.rows));
        int size = rows * 9;

        Gui gui = Gui.gui()
                .title(TextUtil.toComponent(cfg.title))
                .rows(rows)
                .disableAllInteractions()
                .create();

        if (cfg.items != null) {
            cfg.items.values().forEach(item -> {
                if (item == null || item.slot < 0 || item.slot >= size) return;
                String action = item.action == null ? "NONE" : item.action.toUpperCase(Locale.ROOT);
                ItemStack stack = ItemBuilder.of(Objects.requireNonNullElse(item.material, Material.BARRIER))
                        .amount(item.amount)
                        .name(item.name)
                        .lore(item.lore != null ? new ArrayList<>(item.lore) : new ArrayList<>())
                        .build();
                gui.setItem(item.slot, new GuiItem(stack, e -> {
                    e.setCancelled(true);
                    handleStaticAction(player, action);
                }));
            });
        }

        populateBattles(gui, player, cfg, size);
        gui.open(player);
    }

    private void handleStaticAction(Player player, String action) {
        switch (action) {
            case "CLOSE" -> player.closeInventory();
            case "CREATE" -> {
                player.closeInventory();
                plugin.getServer().getScheduler().runTask(plugin, () ->
                        new SelectInv(plugin, battleService).openForOpen(player));
            }
        }
    }

    private void populateBattles(Gui gui, Player player, OpenBattleInvConfig cfg, int size) {
        SelectInvConfig selectCfg = plugin.getConfigService().getBattleCrateSelectInv();
        List<OpenChallenge> battles = new ArrayList<>(battleService.getOpenChallenges());

        List<Integer> slots = (cfg.battleSlots != null && !cfg.battleSlots.isEmpty())
                ? cfg.battleSlots
                : List.of();

        int slotIndex = 0;
        for (OpenChallenge battle : battles) {
            while (slotIndex < slots.size() && isOccupied(gui, slots.get(slotIndex))) slotIndex++;
            if (slotIndex >= slots.size()) break;

            int slot = slots.get(slotIndex);
            if (slot < 0 || slot >= size) { slotIndex++; continue; }

            if (battle.getCreator().equals(player.getUniqueId())) {
                slotIndex++;
                continue;
            }

            Crate crate = battleService.findCrate(battle.getCrateName()).orElse(null);
            if (crate == null) { slotIndex++; continue; }

            Player creator = plugin.getServer().getPlayer(battle.getCreator());
            String creatorName = creator != null ? creator.getName() : "?";
            String time = TIME_FMT.format(Instant.ofEpochMilli(battle.getCreatedAtMillis()));

            Map<String, String> ph = Map.of(
                    "crate", crate.getDisplayName(),
                    "amount", String.valueOf(battle.getAmount()),
                    "creator", creatorName,
                    "time", time
            );

            ItemStack icon = buildCrateIcon(crate, selectCfg, ph, cfg);

            final int finalSlot = slots.get(slotIndex);
            final OpenChallenge finalBattle = battle;
            gui.setItem(finalSlot, new GuiItem(icon, e -> {
                e.setCancelled(true);
                handleJoin(player, finalBattle, crate);
            }));
            slotIndex++;
        }
    }

    private ItemStack buildCrateIcon(Crate crate, SelectInvConfig selectCfg,
                                      Map<String, String> ph, OpenBattleInvConfig cfg) {
        SelectInvConfig.CrateItem crateItem = selectCfg.crates.get(crate.getName());

        Material mat = Material.CHEST;
        String displayName = crate.getDisplayName();

        if (crateItem != null) {
            Material parsed = crateItem.material != null ? Material.matchMaterial(crateItem.material) : null;
            if (parsed != null) mat = parsed;
            if (crateItem.displayName != null) displayName = crateItem.displayName;
        }

        List<String> lore = new ArrayList<>();
        if (cfg.battleItemLore != null) {
            for (String line : cfg.battleItemLore) {
                if (line == null) continue;
                String resolved = line;
                for (Map.Entry<String, String> entry : ph.entrySet()) {
                    resolved = resolved.replace("{" + entry.getKey() + "}", entry.getValue());
                }
                lore.add(resolved);
            }
        }

        return ItemBuilder.of(mat)
                .placeholders(ph)
                .name(displayName)
                .lore(lore)
                .build();
    }

    private void handleJoin(Player joiner, OpenChallenge battle, Crate crate) {
        Player creator = plugin.getServer().getPlayer(battle.getCreator());
        if (creator == null) {
            battleService.removeOpenChallenge(battle.getId());
            joiner.playSound(joiner.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            plugin.getServer().getScheduler().runTask(plugin, () -> open(joiner));
            return;
        }

        int have = plugin.getKeyService().countKeys(joiner, crate.getName());
        if (have < battle.getAmount()) {
            battleService.noKeys(joiner, have, battle.getAmount());
            joiner.playSound(joiner.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (plugin.getRewardExecutor().countFreeSlots(joiner) < battle.getAmount()) {
            plugin.getConfigService().getLangConfig().inventoryFull.send(joiner);
            joiner.playSound(joiner.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        joiner.closeInventory();
        plugin.getServer().getScheduler().runTask(plugin, () ->
                new ConfirmInv(plugin, battleService).openForJoinOpen(joiner, creator, crate, battle));
    }

    private boolean isOccupied(Gui gui, int slot) {
        return gui.getGuiItem(slot) != null;
    }
}

