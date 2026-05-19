package me.darkness.crates.inventories;

import dev.darkness.utilities.item.ItemBuilder;
import dev.darkness.utilities.math.NumberUtil;
import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.StorageGui;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.inventories.EditInvConfig;
import me.darkness.crates.configuration.LangConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateLoader;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.crate.edit.EditSession;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.reward.RewardRoller;
import me.darkness.crates.crate.reward.RewardType;
import me.darkness.crates.crate.reward.SlottedCrateReward;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

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
        EditInvConfig config = plugin.getConfigService().editInv();
        int rows = Math.max(1, Math.min(6, config.rows));
        UUID playerId = player.getUniqueId();

        StorageGui gui = Gui.storage()
                .title(TextUtil.toComponent(config.title.replace("{crate}", crate.getDisplayName())))
                .rows(rows)
                .create();

        EditSession.Session session = resolveSession(playerId, crate, rows);

        applyItems(gui, session, crate, config, rows);

        gui.setDefaultClickAction(event -> {
            if (!(event.getWhoClicked() instanceof Player p)) return;
            if (session.isBlockedSlot(event.getRawSlot())) {
                event.setCancelled(true);
                return;
            }
            handleClick(event, p, session, config, rows, gui);
        });

        gui.setCloseGuiAction(event -> {
            if (!(event.getPlayer() instanceof Player p)) return;
            if (session.isReopening()) {
                session.setReopening(false);
                return;
            }
            saveFromInventory(p, session, gui, crate, rows);
        });

        gui.open(player);
    }

    private EditSession.Session resolveSession(UUID playerId, Crate crate, int rows) {
        EditSession.Session existing = plugin.getEditSessionManager().get(playerId);
        if (existing == null) {
            EditSession.Session s = plugin.getEditSessionManager().start(playerId, crate, rows);
            seedFromCrate(s, crate);
            return s;
        }
        if (existing.crate() != crate || existing.rows() != rows) {
            Map<Integer, EditSession.RewardSettings> snapshot = existing.snapshotRewardSettings();
            EditSession.Session s = plugin.getEditSessionManager().start(playerId, crate, rows);
            snapshot.forEach((slot, rs) -> s.putRewardSettings(slot, rs.chance, rs.commands, rs.giveItem));
            seedFromCrate(s, crate);
            return s;
        }
        return existing;
    }

    private void handleClick(InventoryClickEvent event, Player player, EditSession.Session session,
                             EditInvConfig config, int rows, StorageGui gui) {
        int raw = event.getRawSlot();
        if (raw < 0 || raw >= rows * 9) return;

        ItemStack clicked = event.getInventory().getItem(raw);

        if (event.getClick() == ClickType.SWAP_OFFHAND) {
            if (clicked != null && !clicked.getType().isAir()) {
                event.setCancelled(true);
                toggleItemMode(session, raw, player, gui, config);
            }
            return;
        }

        if (event.getClick() == ClickType.DROP && clicked != null && !clicked.getType().isAir()) {
            event.setCancelled(true);
            if (session.getRewardSettings(raw) == null) {
                session.putRewardSettings(raw, 100.0, List.of(), true);
            }
            session.setReopening(true);
            player.closeInventory();
            promptEdit(player, session, raw, false);
            return;
        }

        if (event.getClick().isRightClick() && clicked != null && !clicked.getType().isAir()) {
            event.setCancelled(true);
            if (session.getRewardSettings(raw) == null) {
                session.putRewardSettings(raw, 100.0, List.of(), true);
            }
            session.setReopening(true);
            player.closeInventory();
            promptEdit(player, session, raw, event.getClick().isShiftClick());
        }
    }

    private void promptEdit(Player player, EditSession.Session session, int slot, boolean isCommand) {
        LangConfig lang = plugin.getConfigService().lang();
        if (isCommand) {
            session.startEdit(slot, EditSession.EditType.COMMAND);
            lang.commandEditPrompt.send(player);
        } else {
            session.startEdit(slot, EditSession.EditType.CHANCE);
            lang.chanceEditPrompt.send(player);
        }
    }

    private void toggleItemMode(EditSession.Session session, int slot, Player player, StorageGui gui, EditInvConfig config) {
        EditSession.RewardSettings before = session.getRewardSettings(slot);
        double chance = before != null ? before.chance : 100.0;
        List<String> commands = before != null ? before.commands : List.of();
        boolean newGiveItem = before == null || !before.giveItem;

        session.putRewardSettings(slot, chance, commands, newGiveItem);
        plugin.getConfigService().lang().giveItemToggled.send(player,
                Map.of("mode", newGiveItem ? "Przedmiot" : "Komenda"));

        ItemStack currentItem = gui.getInventory().getItem(slot);
        if (currentItem != null && !currentItem.getType().isAir()) {
            EditSession.RewardSettings s = session.getRewardSettings(slot);
            RewardType type = s != null ? (s.giveItem ? RewardType.ITEM : RewardType.COMMAND) : RewardType.ITEM;
            SlottedCrateReward view = new SlottedCrateReward(slot, currentItem, currentItem,
                    s != null ? s.commands : List.of(), s != null ? s.chance : chance, type);
            ItemStack cleanItem = removeTempLore(currentItem.clone());
            gui.updateItem(slot, new GuiItem(prepareItem(cleanItem, view, config)));
        }
    }

    private void seedFromCrate(EditSession.Session session, Crate crate) {
        crate.getRewards().stream()
                .filter(r -> r instanceof SlottedCrateReward s && session.isValidSlot(s.getSlot()))
                .map(r -> (SlottedCrateReward) r)
                .filter(s -> session.getRewardSettings(s.getSlot()) == null)
                .forEach(s -> session.putRewardSettings(s.getSlot(), s.getChance(), s.getCommands(), s.shouldGiveItem()));
    }

    private void applyItems(StorageGui gui, EditSession.Session session, Crate crate, EditInvConfig config, int rows) {
        int size = rows * 9;

        config.items.values().forEach(item -> {
            if (item == null || item.slot < 0 || item.slot >= size) return;
            session.addBlockedSlot(item.slot);
            gui.setItem(item.slot, toGuiItem(item, crate));
        });

        Set<Integer> used = new HashSet<>(session.getBlockedSlots());
        int freeSlot = 0;

        for (CrateReward r : crate.getRewards()) {
            if (!(r instanceof SlottedCrateReward s)) continue;
            int slot = s.getSlot();
            if (!session.isValidSlot(slot) || session.isBlockedSlot(slot) || s.getDisplayItem() == null) continue;
            if (isOccupied(gui, slot)) continue;

            EditSession.RewardSettings settings = session.getRewardSettings(slot);
            SlottedCrateReward view = settings != null
                    ? new SlottedCrateReward(slot, s.getDisplayItem(), s.getRewardItem(), settings.commands, settings.chance, settings.giveItem ? RewardType.ITEM : RewardType.COMMAND)
                    : s;
            gui.getInventory().setItem(slot, prepareItem(s.getDisplayItem().clone(), view, config));
            used.add(slot);
        }

        for (CrateReward r : crate.getRewards()) {
            if (r instanceof SlottedCrateReward || r.getDisplayItem() == null) continue;
            while (freeSlot < size && (used.contains(freeSlot) || isOccupied(gui, freeSlot))) freeSlot++;
            if (freeSlot >= size) break;
            SlottedCrateReward slotted = new SlottedCrateReward(freeSlot, r.getDisplayItem(), r.getRewardItem(), r.getCommands(), r.getChance(), r.getType());
            gui.getInventory().setItem(freeSlot, prepareItem(r.getDisplayItem().clone(), slotted, config));
            used.add(freeSlot++);
        }
    }

    private boolean isOccupied(StorageGui gui, int slot) {
        ItemStack item = gui.getInventory().getItem(slot);
        return item != null && !item.getType().isAir();
    }

    private ItemStack prepareItem(ItemStack item, SlottedCrateReward reward, EditInvConfig config) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        if (config.rewardEditLore == null || config.rewardEditLore.isEmpty()) return item;

        String chanceStr = NumberUtil.formatCompact(reward.getChance());
        String cmdStr = reward.getCommands().isEmpty() ? "Brak" : reward.getCommands().get(0);
        String mode = reward.shouldGiveItem() ? "Przedmiot" : "Komenda";

        List<Component> lore = new ArrayList<>(Objects.requireNonNullElse(meta.lore(), List.of()));
        int added = 0;
        for (String line : config.rewardEditLore) {
            if (line == null) continue;
            lore.add(TextUtil.toComponent(line
                    .replace("{chance}", chanceStr)
                    .replace("{command}", cmdStr)
                    .replace("{mode}", mode)));
            added++;
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return added > 0 ? ItemBuilder.of(item).tag(tempLoreKey, PersistentDataType.INTEGER, added).build() : item;
    }

    private void saveFromInventory(Player player, EditSession.Session session, StorageGui gui, Crate crate, int rows) {
        int size = rows * 9;
        List<CrateReward> updated = new ArrayList<>();

        for (int slot = 0; slot < size; slot++) {
            if (session.isBlockedSlot(slot)) continue;
            ItemStack item = gui.getInventory().getItem(slot);
            if (item == null || item.getType().isAir()) continue;

            ItemStack clean = removeTempLore(item.clone());
            EditSession.RewardSettings settings = session.getRewardSettings(slot);
            double chance = settings != null ? settings.chance : 100.0;
            List<String> commands = settings != null ? settings.commands : List.of();
            RewardType type = settings != null ? (settings.giveItem ? RewardType.ITEM : RewardType.COMMAND) : RewardType.ITEM;

            updated.add(new SlottedCrateReward(slot, clean, clean, commands, chance, type));
        }

        List<CrateReward> normalized = RewardRoller.normalizeAll(updated);
        crate.setRewards(normalized);
        crateService.updateCrate(crate);
        crateLoader.saveCrate(crate);
        plugin.getEditSessionManager().end(player.getUniqueId());
        plugin.getConfigService().lang().crateSaved.send(player, Map.of("crate", crate.getDisplayName()));
    }

    private ItemStack removeTempLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        Integer added = meta.getPersistentDataContainer().get(tempLoreKey, PersistentDataType.INTEGER);
        if (added == null || added <= 0) return item;
        List<Component> lore = meta.lore();
        if (lore != null && lore.size() >= added) {
            meta.lore(new ArrayList<>(lore.subList(0, lore.size() - added)));
        }
        meta.getPersistentDataContainer().remove(tempLoreKey);
        item.setItemMeta(meta);
        return item;
    }

    private GuiItem toGuiItem(EditInvConfig.GuiItem cfgItem, Crate crate) {
        ItemStack stack = ItemBuilder.of(cfgItem.material)
                .placeholders(Map.of("crate", crate.getDisplayName(), "type", crate.getAnimationType().getId()))
                .name(cfgItem.name)
                .lore(cfgItem.lore != null ? new ArrayList<>(cfgItem.lore) : new ArrayList<>())
                .build();
        return new GuiItem(stack, e -> {
            if ("CLOSE".equalsIgnoreCase(cfgItem.action)) e.getWhoClicked().closeInventory();
        });
    }
}
