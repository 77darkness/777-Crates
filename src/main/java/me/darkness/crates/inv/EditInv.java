package me.darkness.crates.inv;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.EditInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateLoader;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.crate.edit.EditSession;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.reward.SlottedCrateReward;
import me.darkness.crates.util.ItemBuilder;
import me.darkness.crates.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public final class EditInv {

    private final CratesPlugin plugin;
    private final CrateService crateService;
    private final CrateLoader crateLoader;
    private final NamespacedKey EDIT_TEMP_LORE_KEY;

    public EditInv(final CratesPlugin plugin, final CrateService crateService, final CrateLoader crateLoader) {
        this.plugin = plugin;
        this.crateService = crateService;
        this.crateLoader = crateLoader;
        this.EDIT_TEMP_LORE_KEY = ItemBuilder.key(plugin, "edit_temp_lore");
    }

    public void open(final Player player, Crate crate) {
        final var config = plugin.getConfigService().getEditInv();
        final int rows = Math.max(1, Math.min(6, config.rows));

        final Gui gui = Gui.gui()
                .title(Component.text(TextUtil.color(config.title.replace("{crate}", crate.getDisplayName()))))
                .rows(rows)
                .create();

        final Set<Integer> blockedSlots = new HashSet<>();

        final UUID playerId = player.getUniqueId();

        EditSession.Session existing = EditSession.get(playerId);
        if (existing == null) {
            EditSession.start(playerId, crate, rows);
            seedSessionFromCrate(playerId, crate, rows);
        } else {
            if (existing.crate() != crate || existing.rows() != rows) {
                Map<Integer, EditSession.RewardSettings> snapshot = new HashMap<>(existing.rewardSettings);
                EditSession.start(playerId, crate, rows);
                snapshot.forEach((slot, settings) -> EditSession.putRewardSettings(playerId, slot, settings.chance, settings.commands, settings.giveItem));
                seedSessionFromCrate(playerId, crate, rows);
            }
        }

        applyItems(gui, playerId, crate, config, rows, blockedSlots);

        gui.setDefaultClickAction(event -> {
            int raw = event.getRawSlot();

            boolean isEditAction = event.getClick() == ClickType.SWAP_OFFHAND
                    || event.getClick().isRightClick();

            if (raw < 0 || raw >= rows * 9) {
                if (isEditAction) {
                    event.setCancelled(true);
                }
                return;
            }

            if (blockedSlots.contains(raw)) {
                event.setCancelled(true);
                return;
            }

            ItemStack clicked = event.getInventory().getItem(raw);
            boolean isAir = clicked == null || clicked.getType().isAir();

            if (isAir) {
                if (isEditAction) {
                    event.setCancelled(true);
                }
                return;
            }

            if (event.getClick() == ClickType.SWAP_OFFHAND) {
                event.setCancelled(true);

                EditSession.RewardSettings before = EditSession.getRewardSettings(playerId, raw);
                double chance = before != null ? before.chance : 100.0;
                List<String> commands = before != null ? before.commands : List.of();
                boolean giveItem = before == null || before.giveItem;

                EditSession.putRewardSettings(playerId, raw, chance, commands, !giveItem);

                TextUtil.send(
                        plugin.getConfigService().getLangConfig(),
                        plugin,
                        player,
                        plugin.getConfigService().getLangConfig().giveItemToggled,
                        Map.of("mode", !giveItem ? "Przedmiot" : "Komenda")
                );

                SlottedCrateReward base = null;
                for (CrateReward r : crate.getRewards()) {
                    if (r instanceof SlottedCrateReward s && s.getSlot() == raw) {
                        base = s;
                        break;
                    }
                }
                if (base != null && base.getDisplayItem() != null) {
                    EditSession.RewardSettings s = EditSession.getRewardSettings(playerId, raw);
                    SlottedCrateReward view = base;
                    if (s != null) {
                        view = new SlottedCrateReward(raw, base.getDisplayItem(), base.getRewardItem(), s.commands, s.chance, s.giveItem);
                    }
                    gui.updateItem(raw, new GuiItem(prepareItem(base.getDisplayItem().clone(), view, config)));
                }
                return;
            }

            if (event.getClick().isShiftClick() && event.getClick().isRightClick()) {
                event.setCancelled(true);

                if (EditSession.getRewardSettings(playerId, raw) == null) {
                    EditSession.putRewardSettings(playerId, raw, 100.0, List.of(), true);
                }

                EditSession.startCommandEdit(playerId, raw);
                player.closeInventory();
                TextUtil.send(
                        plugin.getConfigService().getLangConfig(),
                        plugin,
                        player,
                        plugin.getConfigService().getLangConfig().commandEditPrompt
                );
                return;
            }

            if (event.getClick().isRightClick()) {
                event.setCancelled(true);

                if (EditSession.getRewardSettings(playerId, raw) == null) {
                    EditSession.putRewardSettings(playerId, raw, 100.0, List.of(), true);
                }

                EditSession.startChanceEdit(playerId, raw);
                player.closeInventory();
                TextUtil.send(
                        plugin.getConfigService().getLangConfig(),
                        plugin,
                        player,
                        plugin.getConfigService().getLangConfig().chanceEditPrompt
                );
            }
        });

        gui.setCloseGuiAction(event -> {
            if (EditSession.getEditingChanceSlot(playerId) != null ||
                    EditSession.getEditingCommandSlot(playerId) != null) {
                return;
            }

            try {
                saveRewards(player, crate, event.getInventory(), rows, blockedSlots);
            } finally {
                EditSession.end(playerId);
            }
        });

        gui.open(player);
    }

    private void seedSessionFromCrate(UUID playerId, Crate crate, int rows) {
        int max = rows * 9;

        for (CrateReward reward : crate.getRewards()) {
            if (!(reward instanceof SlottedCrateReward slotted)) continue;

            int slot = slotted.getSlot();
            if (slot < 0 || slot >= max) continue;

            if (EditSession.getRewardSettings(playerId, slot) == null) {
                EditSession.putRewardSettings(
                        playerId, slot,
                        slotted.getChance(),
                        slotted.getCommands(),
                        slotted.shouldGiveItem()
                );
            }
        }
    }

    private void applyItems(Gui gui, UUID playerId, Crate crate, EditInvConfig config,
                            int rows, Set<Integer> blockedSlots) {

        for (var entry : config.items.entrySet()) {
            var item = entry.getValue();
            if (item == null || item.slot < 0 || item.slot >= rows * 9) continue;
            blockedSlots.add(item.slot);
            gui.setItem(item.slot, toGuiItem(item, crate));
        }

        Set<Integer> used = new HashSet<>(blockedSlots);

        for (CrateReward r : crate.getRewards()) {
            if (!(r instanceof SlottedCrateReward s)) continue;
            int slot = s.getSlot();
            if (slot < 0 || slot >= rows * 9 || blockedSlots.contains(slot)) continue;

            ItemStack existingItem = gui.getInventory().getItem(slot);
            if (existingItem != null && !existingItem.getType().isAir()) {
                continue;
            }

            ItemStack display = s.getDisplayItem();
            if (display != null) {
                EditSession.RewardSettings settings = EditSession.getRewardSettings(playerId, slot);
                SlottedCrateReward viewReward = s;
                if (settings != null) {
                    viewReward = new SlottedCrateReward(
                            slot,
                            s.getDisplayItem(),
                            s.getRewardItem(),
                            settings.commands,
                            settings.chance,
                            settings.giveItem
                    );
                }

                gui.setItem(slot, new GuiItem(prepareItem(display.clone(), viewReward, config)));
                used.add(slot);
            }
        }

        int freeSlot = 0;
        for (CrateReward r : crate.getRewards()) {
            if (r instanceof SlottedCrateReward) continue;

            while (freeSlot < rows * 9 && used.contains(freeSlot)) freeSlot++;
            if (freeSlot >= rows * 9) break;

            ItemStack existingItem = gui.getInventory().getItem(freeSlot);
            if (existingItem != null && !existingItem.getType().isAir()) {
                freeSlot++;
                continue;
            }

            ItemStack display = r.getDisplayItem();
            if (display == null) {
                continue;
            }

            SlottedCrateReward newSlotted = new SlottedCrateReward(
                    freeSlot,
                    display,
                    r.getRewardItem(),
                    r.getCommands(),
                    r.getChance(),
                    r.shouldGiveItem()
            );

            gui.setItem(freeSlot, new GuiItem(prepareItem(display.clone(), newSlotted, config)));
            used.add(freeSlot);
            freeSlot++;
        }
    }

    private ItemStack prepareItem(ItemStack item, SlottedCrateReward reward,
                                  EditInvConfig config) {
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<String> lore = new ArrayList<>();
        List<String> oldLore = meta.getLore();
        if (oldLore != null) lore.addAll(oldLore);

        int addedLines = 0;
        if (config.rewardEditLore != null && !config.rewardEditLore.isEmpty()) {
            String chanceStr = formatChance(reward.getChance());
            String cmdStr = reward.getCommands().isEmpty() ? "Brak" : reward.getCommands().get(0);
            String mode = reward.shouldGiveItem() ? "Przedmiot" : "Komenda";

            for (String line : config.rewardEditLore) {
                if (line == null) continue;
                String processed = line
                        .replace("{chance}", chanceStr)
                        .replace("{command}", cmdStr)
                        .replace("{mode}", mode);
                lore.add(TextUtil.color(processed));
                addedLines++;
            }
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        ItemBuilder builder = ItemBuilder.of(item);
        if (addedLines > 0) {
            builder.tagInt(EDIT_TEMP_LORE_KEY, addedLines);
        } else {
            builder.removeTag(EDIT_TEMP_LORE_KEY);
        }

        return builder.build();
    }

    private GuiItem toGuiItem(EditInvConfig.GuiItem cfgItem, Crate crate) {
        ItemStack stack = toItemStack(cfgItem, crate);
        return new GuiItem(stack, e -> {
            String action = cfgItem.action == null ? "NONE" : cfgItem.action.toUpperCase();
            if ("CLOSE".equals(action)) {
                e.getWhoClicked().closeInventory();
            }
        });
    }

    private ItemStack toItemStack(EditInvConfig.GuiItem cfgItem, Crate crate) {
        var mat = cfgItem.material != null ? cfgItem.material : Material.BARRIER;

        return ItemBuilder.of(mat)
                .placeholders(Map.of(
                        "crate", crate.getDisplayName(),
                        "type", crate.getAnimationType().getId()
                ))
                .name(cfgItem.name)
                .lore(cfgItem.lore == null ? null : List.copyOf(cfgItem.lore))
                .build();
    }

    private void saveRewards(Player player, Crate crate, Inventory inv, int rows, Set<Integer> blocked) {
        UUID uuid = player.getUniqueId();
        List<CrateReward> rewards = new ArrayList<>();

        for (int i = 0; i < rows * 9; i++) {
            if (blocked.contains(i)) continue;

            ItemStack item = inv.getItem(i);
            if (item == null || item.getType().isAir()) continue;

            ItemStack cleaned = tempLore(item.clone());

            var settings = EditSession.getRewardSettings(uuid, i);
            double chance = settings != null ? settings.chance : 100.0;
            List<String> commands = settings != null ? settings.commands : List.of();
            boolean giveItem = settings == null || settings.giveItem;

            rewards.add(new SlottedCrateReward(i, cleaned, cleaned.clone(), commands, chance, giveItem));
        }

        Crate updated = crate.withRewards(rewards);
        crateService.registerCrate(updated);
        crateLoader.saveCrate(updated);
    }

    private ItemStack tempLore(ItemStack item) {
        if (item == null) return null;

        int count = ItemBuilder.of(item).getIntOrDefault(EDIT_TEMP_LORE_KEY, 0);
        if (count <= 0) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        List<String> lore = meta.getLore();
        if (lore != null && lore.size() >= count) {
            meta.setLore(lore.subList(0, lore.size() - count));
        }
        item.setItemMeta(meta);

        return ItemBuilder.of(item)
                .removeTag(EDIT_TEMP_LORE_KEY)
                .build();
    }

    private static final DecimalFormat CHANCE_FORMAT = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));

    private String formatChance(double value) {
        return CHANCE_FORMAT.format(value);
    }
}
