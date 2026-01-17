package me.darkness.crates.crate.animation.impl;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.RouletteInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.CrateAnimation;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.util.ItemBuilder;
import me.darkness.crates.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class PlayerRouletteAnimation extends CrateAnimation {

    private static final int ROULETTE_DURATION = 100;
    private static final int ROULETTE_SPEED = 2;
    private static final String TICK_SOUND = "UI_BUTTON_CLICK";
    private static final String WIN_SOUND = "ENTITY_PLAYER_LEVELUP";

    private final UUID playerA;
    private final UUID playerB;
    private final UUID forcedWinner;
    private final String titleOverride;

    private final int[] displaySlots;
    private final int guiRows;

    private int currentTick;
    private int tickDelay;

    private Inventory inv;
    private Gui gui;

    private final Map<UUID, ItemStack> playerItemCache = new HashMap<>();
    private int rouletteStep = 0;

    @SuppressWarnings("unused")
    public PlayerRouletteAnimation(CratesPlugin plugin,
                                  Player viewer,
                                  Crate crate,
                                  UUID playerA,
                                  UUID playerB,
                                  Map<UUID, ItemStack> unusedCache,
                                  String titleOverride,
                                  UUID forcedWinner) {
        super(plugin, viewer, crate, null);
        this.playerA = playerA;
        this.playerB = playerB;
        this.titleOverride = titleOverride;
        this.forcedWinner = (forcedWinner != null) ? forcedWinner : (playerA != null ? playerA : playerB);

        RouletteInvConfig cfg = this.plugin.getConfigService().getRouletteInv();

        List<Integer> parsedSlots = new ArrayList<>();
        List<Integer> rawSlots = (cfg != null ? cfg.displaySlots : null);
        if (rawSlots != null) {
            parsedSlots.addAll(rawSlots);
        }

        if (!parsedSlots.isEmpty()) {
            this.displaySlots = parsedSlots.stream().distinct().mapToInt(Integer::intValue).toArray();
        } else {
            this.displaySlots = new int[]{10, 11, 12, 13, 14, 15, 16};
        }

        this.guiRows = (cfg != null ? Math.max(1, Math.min(6, cfg.rows)) : 3);
        this.currentTick = 0;
        this.tickDelay = ROULETTE_SPEED;
    }

    @Override
    public void start() {
        this.createGui();
        this.gui.open(this.player);
        this.startAnimation();
    }

    private void createGui() {
        RouletteInvConfig cfg = this.plugin.getConfigService().getRouletteInv();
        final int rows = Math.max(1, Math.min(6, cfg.rows));

        String title = (this.titleOverride != null && !this.titleOverride.isBlank())
                ? this.titleOverride
                : cfg.title;

        Gui gui = Gui.gui()
                .title(Component.text(TextUtil.color(title)))
                .rows(rows)
                .disableAllInteractions()
                .create();

        this.gui = gui;
        this.inv = gui.getInventory();

        applyStaticItems(cfg);
        updateDisplay();
    }

    private void applyStaticItems(RouletteInvConfig cfg) {
        if (cfg == null || cfg.items == null) {
            return;
        }

        for (RouletteInvConfig.GuiItem item : cfg.items.values()) {
            if (item == null) continue;
            if (item.slot < 0 || item.slot >= this.guiRows * 9) continue;

            ItemStack stack = ItemBuilder.of(item.material == null ? Material.BARRIER : item.material)
                    .name(item.name)
                    .lore(item.lore)
                    .build();

            this.gui.setItem(item.slot, new GuiItem(stack, e -> e.setCancelled(true)));
        }
    }

    private ItemStack getDisplayItem(UUID uuid) {
        return playerItemCache.computeIfAbsent(uuid, id -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
            String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : "";
            return ItemBuilder.of(Material.NAME_TAG)
                    .name("&e" + name)
                    .build();
        }).clone();
    }


    private void updateDisplay() {
        UUID nextUuid = getNextUuidForSpin();
        if (nextUuid == null) {
            return;
        }

        ItemStack item = getDisplayItem(nextUuid);
        for (int slot : displaySlots) {
            gui.updateItem(slot, new GuiItem(item, e -> e.setCancelled(true)));
        }
    }

    private UUID getNextUuidForSpin() {
        List<UUID> playerUUIDs = new ArrayList<>(2);
        if (playerA != null) playerUUIDs.add(playerA);
        if (playerB != null && !playerB.equals(playerA)) playerUUIDs.add(playerB);

        if (playerUUIDs.isEmpty() && forcedWinner != null) {
            return forcedWinner;
        }

        if (playerUUIDs.isEmpty()) {
            return null;
        }

        UUID nextUuid = playerUUIDs.get(rouletteStep % playerUUIDs.size());
        rouletteStep++;
        return nextUuid;
    }

    private void showWinner() {
        if (this.forcedWinner == null || this.gui == null) {
            return;
        }

        ItemStack item = getDisplayItem(this.forcedWinner);
        for (int slot : displaySlots) {
            this.gui.updateItem(slot, new GuiItem(item, e -> e.setCancelled(true)));
        }
    }

    private void startAnimation() {
        int duration = ROULETTE_DURATION;

        this.task = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, () -> {
            this.currentTick++;

            if (this.currentTick % this.tickDelay == 0) {
                this.playTickSound();
                this.updateDisplay();

                if (this.currentTick > duration * 0.7 && (this.currentTick % 6 == 0)) {
                    this.tickDelay++;
                }
            }

            if (this.currentTick >= duration) {
                this.stopAnimation();
            }
        }, 0L, 1L);
    }

    private void stopAnimation() {
        this.task.cancel();
        this.playWinSound();

        this.showWinner();
        final UUID finalWinner = this.forcedWinner;

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            if (this.player.isOnline()) {
                this.player.closeInventory();
            }

            BattleService service = this.caseBattleService();
            if (service != null) {
                service.playerRouletteFinished(this.player.getUniqueId(), finalWinner);
            }
            this.finish();
        }, 60L);
    }

    private BattleService caseBattleService() {
        return this.plugin.getBattleService();
    }

    private void playTickSound() {
        Sound sound = parseSound(TICK_SOUND);
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 0.5f, 1.0f);
        }
    }

    private void playWinSound() {
        Sound sound = parseSound(WIN_SOUND);
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }

    private Sound parseSound(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return Sound.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override
    public boolean isAnimation(Inventory inventory) {
        return this.inv != null && this.inv.equals(inventory);
    }
}
