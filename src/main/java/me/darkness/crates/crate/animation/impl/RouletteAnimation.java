package me.darkness.crates.crate.animation.impl;

import me.darkness.crates.util.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.RouletteInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.CrateAnimation;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class RouletteAnimation extends CrateAnimation {

    private static final int ROULETTE_DURATION = 100;
    private static final int ROULETTE_SPEED = 2;

    private static final String TICK_SOUND = "UI_BUTTON_CLICK";
    private static final String WIN_SOUND = "ENTITY_PLAYER_LEVELUP";

    private final int[] displaySlots;
    private final int guiRows;
    private final int winnerSlot;

    private Inventory rouletteInventory;
    private Gui gui;
    private List<CrateReward> shuffledRewards;
    private int currentTick;
    private int tickDelay;
    private int currentIndex;
    private int spinSteps;

    public RouletteAnimation(CratesPlugin plugin, Player player, Crate crate, CrateReward reward) {
        super(plugin, player, crate, reward);
        this.currentTick = 0;
        this.tickDelay = ROULETTE_SPEED;
        this.currentIndex = 0;

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

        this.winnerSlot = (this.displaySlots.length > 0) ? this.displaySlots[this.displaySlots.length / 2] : 13;
    }

    @Override
    public void start() {
        this.prepareRewards();
        this.createGui();
        this.gui.open(this.player);
        this.startAnimation();
    }

    private void prepareRewards() {
        this.shuffledRewards = new ArrayList<>(this.crate.getRewards());
        Collections.shuffle(this.shuffledRewards);

        int winnerIndex = ThreadLocalRandom.current().nextInt(Math.max(1, this.shuffledRewards.size()));
        this.shuffledRewards.add(winnerIndex, this.reward);

        while (this.shuffledRewards.size() < 50) {
            this.shuffledRewards.addAll(new ArrayList<>(this.crate.getRewards()));
        }

        this.currentIndex = ThreadLocalRandom.current().nextInt(this.shuffledRewards.size());
    }

    private void createGui() {
        RouletteInvConfig cfg = this.plugin.getConfigService().getRouletteInv();
        final int rows = Math.max(1, Math.min(6, cfg.rows));

        Gui gui = Gui.gui()
                .title(Component.text(TextUtil.color(cfg.title)))
                .rows(rows)
            .disableAllInteractions()
            .create();

        this.gui = gui;
        this.rouletteInventory = gui.getInventory();

        this.initializeStaticItems();
        this.updateDisplay();
    }

    private void initializeStaticItems() {
        RouletteInvConfig cfg = this.plugin.getConfigService().getRouletteInv();
        if (cfg == null || cfg.items == null) {
            return;
        }

        for (RouletteInvConfig.GuiItem item : cfg.items.values()) {
            if (item == null) continue;
            if (item.slot < 0 || item.slot >= this.guiRows * 9) continue;

            ItemStack stack = ItemBuilder.of(item.material == null ? Material.BARRIER : item.material)
                    .placeholders(Map.of("crate", this.crate.getDisplayName()))
                    .name(item.name)
                    .lore(item.lore)
                    .build();

            this.gui.setItem(item.slot, new GuiItem(stack, e -> e.setCancelled(true)));
        }
    }

    private void updateDisplay() {
        for (int i = 0; i < this.displaySlots.length; i++) {
            int guiSlot = this.displaySlots[i];

            if (guiSlot == this.winnerSlot && this.currentTick >= (ROULETTE_DURATION - 5)) {
                ItemStack displayItem = this.reward.getDisplayItem().clone();
                this.gui.updateItem(guiSlot, new GuiItem(displayItem));
                continue;
            }

            int rewardIndex = (this.currentIndex + i) % this.shuffledRewards.size();
            CrateReward reward = this.shuffledRewards.get(rewardIndex);

            ItemStack displayItem = reward.getDisplayItem().clone();
            this.gui.updateItem(guiSlot, new GuiItem(displayItem));
        }
    }

    private void startAnimation() {
        int duration = ROULETTE_DURATION;

        this.task = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, () -> {
            this.currentTick++;

            if (this.currentTick % this.tickDelay == 0) {
                this.spinSteps++;
                this.currentIndex++;
                this.updateDisplay();
                this.playTickSound();

                if (this.currentTick > duration * 0.7 && (this.spinSteps % 3 == 0)) {
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

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            this.player.closeInventory();
            this.finish();
        }, 40L);
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
        return this.rouletteInventory != null && this.rouletteInventory.equals(inventory);
    }
}
