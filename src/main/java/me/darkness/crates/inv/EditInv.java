package me.darkness.crates.inv;

import dev.darkness.utilities.item.ItemBuilder;
import dev.darkness.utilities.item.ItemNbt;
import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.EditInvConfig;
import me.darkness.crates.configuration.Lang;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateLoader;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.crate.edit.EditSession;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.reward.RewardType;
import me.darkness.crates.crate.reward.SlottedCrateReward;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public final class EditInv {

    private final CratesPlugin plugin;
    private final CrateService crateService;
    private final CrateLoader crateLoader;
    private final NamespacedKey tempLoreKey;

    public EditInv(CratesPlugin plugin, CrateService crateService, CrateLoader crateLoader) {
        this.plugin = plugin;
        this.crateService = crateService;
        this.crateLoader = crateLoader;
        this.tempLoreKey = new NamespacedKey(plugin, "edit_temp_lore");
    }

    public void open(Player player, Crate crate) {
        EditInvConfig config = plugin.getConfigService().getEditInv();
        int rows = Math.max(1, Math.min(6, config.rows));
        UUID playerId = player.getUniqueId();
        Set<Integer> blockedSlots = new HashSet<>();

        Gui gui = Gui.gui()
                .title(TextUtil.toComponent(config.title.replace("{crate}", crate.getDisplayName())))
                .rows(rows)
                .create();

        handleSession(playerId, crate, rows);
        applyItems(gui, playerId, crate, config, rows, blockedSlots);

        gui.setDefaultClickAction(event -> {
            if (!(event.getWhoClicked() instanceof Player p)) return;
            handleClick(event, p, p.getUniqueId(), crate, config, rows, blockedSlots, gui);
        });

        gui.setCloseGuiAction(event -> {
            if (!(event.getPlayer() instanceof Player p)) return;
            onClose(p, p.getUniqueId(), crate, event.getInventory(), rows, blockedSlots);
        });

        gui.open(player);
    }

    private void onClose(Player player, UUID playerId, Crate crate, Inventory inv, int rows, Set<Integer> blocked) {
        EditSession.Session session = plugin.getEditSessionManager().get(playerId);
        if (session != null && session.isReopening()) {
            session.setReopening(false);
            return;
        }
        if (plugin.getEditSessionManager().getEditingChanceSlot(playerId) != null
                || plugin.getEditSessionManager().getEditingCommandSlot(playerId) != null) {
            return;
        }
        try {
            saveRewards(player, crate, inv, rows, blocked);
        } finally {
            plugin.getEditSessionManager().end(playerId);
        }
    }

    private void handleSession(UUID playerId, Crate crate, int rows) {
        EditSession.Session existing = plugin.getEditSessionManager().get(playerId);
        if (existing == null) {
            plugin.getEditSessionManager().start(playerId, crate, rows);
            seedSessionFromCrate(playerId, crate, rows);
        } else if (existing.crate() != crate || existing.rows() != rows) {
            Map<Integer, EditSession.RewardSettings> snapshot = new HashMap<>(existing.rewardSettings);
            plugin.getEditSessionManager().start(playerId, crate, rows);
            snapshot.forEach((slot, s) -> plugin.getEditSessionManager().putRewardSettings(playerId, slot, s.chance, s.commands, s.giveItem));
            seedSessionFromCrate(playerId, crate, rows);
        }
    }

    private void handleClick(InventoryClickEvent event, Player player, UUID playerId, Crate crate,
                             EditInvConfig config, int rows, Set<Integer> blockedSlots, Gui gui) {
        int raw = event.getRawSlot();
        boolean isEditAction = event.getClick() == ClickType.SWAP_OFFHAND || event.getClick().isRightClick();

        if (raw < 0 || raw >= rows * 9 || blockedSlots.contains(raw)) {
            if (isEditAction || blockedSlots.contains(raw)) event.setCancelled(true);
            return;
        }

        ItemStack clicked = event.getInventory().getItem(raw);
        if (clicked == null || clicked.getType().isAir()) {
            if (isEditAction) event.setCancelled(true);
            return;
        }

        if (event.getClick() == ClickType.SWAP_OFFHAND) {
            event.setCancelled(true);
            toggleItemMode(playerId, raw, player, crate, gui, config);
            return;
        }

        if (event.getClick().isRightClick()) {
            event.setCancelled(true);
            if (plugin.getEditSessionManager().getRewardSettings(playerId, raw) == null) {
                plugin.getEditSessionManager().putRewardSettings(playerId, raw, 100.0, List.of(), true);
            }
            EditSession.Session session = plugin.getEditSessionManager().get(playerId);
            if (session != null) session.setReopening(true);
            player.closeInventory();
            promptEdit(player, playerId, raw, event.getClick().isShiftClick());
        }
    }

    private void promptEdit(Player player, UUID playerId, int slot, boolean isCommand) {
        Lang lang = plugin.getConfigService().getLangConfig();
        if (isCommand) {
            plugin.getEditSessionManager().startCommandEdit(playerId, slot);
            lang.commandEditPrompt.send(player);
        } else {
            plugin.getEditSessionManager().startChanceEdit(playerId, slot);
            lang.chanceEditPrompt.send(player);
        }
    }

    private void toggleItemMode(UUID playerId, int slot, Player player, Crate crate, Gui gui, EditInvConfig config) {
        EditSession.RewardSettings before = plugin.getEditSessionManager().getRewardSettings(playerId, slot);
        double chance = before != null ? before.chance : 100.0;
        List<String> commands = before != null ? before.commands : List.of();
        boolean giveItem = before == null || before.giveItem;

        plugin.getEditSessionManager().putRewardSettings(playerId, slot, chance, commands, !giveItem);
        plugin.getConfigService().getLangConfig().giveItemToggled.send(player,
                Map.of("mode", !giveItem ? "Przedmiot" : "Komenda"));

        crate.getRewards().stream()
                .filter(r -> r instanceof SlottedCrateReward s && s.getSlot() == slot)
                .map(r -> (SlottedCrateReward) r)
                .findFirst()
                .ifPresent(base -> {
                    if (base.getDisplayItem() == null) return;
                    EditSession.RewardSettings s = plugin.getEditSessionManager().getRewardSettings(playerId, slot);
                    RewardType type = s != null ? (s.giveItem ? RewardType.ITEM : RewardType.COMMAND) : base.getType();
                    SlottedCrateReward view = s != null
                            ? new SlottedCrateReward(slot, base.getDisplayItem(), base.getRewardItem(), s.commands, s.chance, type)
                            : base;
                    gui.updateItem(slot, new GuiItem(prepareItem(base.getDisplayItem().clone(), view, config)));
                });
    }

    private void seedSessionFromCrate(UUID playerId, Crate crate, int rows) {
        int max = rows * 9;
        crate.getRewards().stream()
                .filter(r -> r instanceof SlottedCrateReward s && s.getSlot() >= 0 && s.getSlot() < max)
                .map(r -> (SlottedCrateReward) r)
                .filter(s -> plugin.getEditSessionManager().getRewardSettings(playerId, s.getSlot()) == null)
                .forEach(s -> plugin.getEditSessionManager().putRewardSettings(playerId, s.getSlot(), s.getChance(), s.getCommands(), s.shouldGiveItem()));
    }

    private void applyItems(Gui gui, UUID playerId, Crate crate, EditInvConfig config, int rows, Set<Integer> blockedSlots) {
        int size = rows * 9;
        config.items.values().forEach(item -> {
            if (item == null || item.slot < 0 || item.slot >= size) return;
            blockedSlots.add(item.slot);
            gui.setItem(item.slot, toGuiItem(item, crate));
        });

        Set<Integer> used = new HashSet<>(blockedSlots);
        int freeSlot = 0;

        for (CrateReward r : crate.getRewards()) {
            if (!(r instanceof SlottedCrateReward s)) continue;
            int slot = s.getSlot();
            if (slot < 0 || slot >= size || blockedSlots.contains(slot) || s.getDisplayItem() == null) continue;

            ItemStack existing = gui.getInventory().getItem(slot);
            if (existing != null && !existing.getType().isAir()) continue;

            EditSession.RewardSettings settings = plugin.getEditSessionManager().getRewardSettings(playerId, slot);
            SlottedCrateReward view = settings != null
                    ? new SlottedCrateReward(slot, s.getDisplayItem(), s.getRewardItem(), settings.commands, settings.chance, settings.giveItem ? RewardType.ITEM : RewardType.COMMAND)
                    : s;
            gui.setItem(slot, new GuiItem(prepareItem(s.getDisplayItem().clone(), view, config)));
            used.add(slot);
        }

        for (CrateReward r : crate.getRewards()) {
            if (r instanceof SlottedCrateReward || r.getDisplayItem() == null) continue;
            while (freeSlot < size && (used.contains(freeSlot) || isOccupied(gui, freeSlot))) freeSlot++;
            if (freeSlot >= size) break;
            SlottedCrateReward slotted = new SlottedCrateReward(freeSlot, r.getDisplayItem(), r.getRewardItem(), r.getCommands(), r.getChance(), r.getType());
            gui.setItem(freeSlot, new GuiItem(prepareItem(r.getDisplayItem().clone(), slotted, config)));
            used.add(freeSlot++);
        }
    }

    private boolean isOccupied(Gui gui, int slot) {
        ItemStack item = gui.getInventory().getItem(slot);
        return item != null && !item.getType().isAir();
    }

    private ItemStack prepareItem(ItemStack item, SlottedCrateReward reward, EditInvConfig config) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (config.rewardEditLore == null || config.rewardEditLore.isEmpty()) {
            return item;
        }

        String chanceStr = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US)).format(reward.getChance());
        String cmdStr = reward.getCommands().isEmpty() ? "Brak" : reward.getCommands().get(0);
        String mode = reward.shouldGiveItem() ? "Przedmiot" : "Komenda";

        List<Component> lore = new ArrayList<>(
                Objects.requireNonNullElse(meta.lore(), List.of()));

        int added = 0;
        for (String line : config.rewardEditLore) {
            if (line == null) continue;
            String resolved = line
                    .replace("{chance}", chanceStr)
                    .replace("{command}", cmdStr)
                    .replace("{mode}", mode);
            lore.add(TextUtil.toComponent(resolved));
            added++;
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return added > 0 ? ItemNbt.set(item, tempLoreKey, PersistentDataType.INTEGER, added) : item;
    }

    private GuiItem toGuiItem(EditInvConfig.GuiItem cfgItem, Crate crate) {
        ItemStack stack = ItemBuilder.of(Objects.requireNonNullElse(cfgItem.material, Material.BARRIER))
                .placeholders(Map.of("crate", crate.getDisplayName(), "type", crate.getAnimationType().getId()))
                .name(cfgItem.name)
                .lore(cfgItem.lore != null ? new ArrayList<>(cfgItem.lore) : new ArrayList<>())
                .build();
        return new GuiItem(stack, e -> {
            if ("CLOSE".equalsIgnoreCase(cfgItem.action)) e.getWhoClicked().closeInventory();
        });
    }

    private void saveRewards(Player player, Crate crate, Inventory inv, int rows, Set<Integer> blocked) {
        UUID uuid = player.getUniqueId();
        List<CrateReward> rewards = new ArrayList<>();

        for (int i = 0; i < rows * 9; i++) {
            if (blocked.contains(i)) continue;
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType().isAir()) continue;

            ItemStack cleaned = stripTempLore(item.clone());
            EditSession.RewardSettings s = plugin.getEditSessionManager().getRewardSettings(uuid, i);
            double chance = s != null ? s.chance : 100.0;
            List<String> commands = s != null ? s.commands : List.of();
            RewardType type = s != null ? (s.giveItem ? RewardType.ITEM : RewardType.COMMAND) : RewardType.ITEM;
            rewards.add(new SlottedCrateReward(i, cleaned, cleaned.clone(), commands, chance, type));
        }

        Crate updated = crate.withRewards(rewards);
        crateService.registerCrate(updated);
        crateLoader.saveCrate(updated);
    }

    private ItemStack stripTempLore(ItemStack item) {
        if (item == null) return null;
        Integer count = ItemNbt.get(item, tempLoreKey, PersistentDataType.INTEGER);
        if (count == null || count <= 0) return stripGuiTag(item);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<Component> lore = meta.lore();
        if (lore != null && lore.size() >= count) {
            meta.lore(lore.subList(0, lore.size() - count));
        }
        meta.getPersistentDataContainer().remove(tempLoreKey);
        item.setItemMeta(meta);
        return stripGuiTag(item);
    }

    private ItemStack stripGuiTag(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.getPersistentDataContainer().getKeys()
                .stream()
                .filter(key -> key.getKey().equals("mf-gui"))
                .forEach(key -> meta.getPersistentDataContainer().remove(key));
        item.setItemMeta(meta);
        return item;
    }
}

