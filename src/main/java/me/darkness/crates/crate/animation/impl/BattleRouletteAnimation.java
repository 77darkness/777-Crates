package me.darkness.crates.crate.animation.impl;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.RouletteInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.CrateAnimation;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.util.ItemBuilder;
import me.darkness.crates.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class BattleRouletteAnimation extends CrateAnimation {

    private static final int ROULETTE_DURATION = 100;
    private static final int ROULETTE_SPEED = 2;
    private static final String TICK_SOUND = "UI_BUTTON_CLICK";
    private static final String WIN_SOUND = "ENTITY_PLAYER_LEVELUP";

    private final BattleService battleService;
    private final UUID owner;

    private final List<CrateReward> winners;
    private final int massCount;

    private final int[] localDisplaySlots;

    private final int guiRows;

    private Gui gui;
    private Inventory inv;

    private List<List<CrateReward>> lanes;
    private int[] laneStartIndex;

    private int currentTick;
    private int tickDelay;
    private int currentIndex;
    private int spinSteps;

    public BattleRouletteAnimation(CratesPlugin plugin, BattleService battleService, Player player, Crate crate, List<CrateReward> winners) {
        super(plugin, player, crate, (winners != null && !winners.isEmpty()) ? winners.get(0) : null);
        this.battleService = battleService;
        this.owner = player.getUniqueId();

        this.winners = winners == null ? List.of() : new ArrayList<>(winners);
        this.massCount = Math.max(1, Math.min(6, this.winners.size()));

        this.currentTick = 0;
        this.tickDelay = ROULETTE_SPEED;
        this.currentIndex = 0;

        RouletteInvConfig cfg = this.plugin.getConfigService().getRouletteInv();

        List<Integer> parsedSlots = new ArrayList<>();
        List<Integer> rawSlots = (cfg != null ? cfg.displaySlots : null);
        if (rawSlots != null) {
            parsedSlots.addAll(rawSlots);
        }

        int[] baseDisplaySlots;
        if (!parsedSlots.isEmpty()) {
            baseDisplaySlots = parsedSlots.stream().distinct().mapToInt(Integer::intValue).toArray();
        } else {
            baseDisplaySlots = new int[]{10, 11, 12, 13, 14, 15, 16};
        }

        int min = Integer.MAX_VALUE;
        for (int s : baseDisplaySlots) {
            if (s >= 0) min = Math.min(min, s);
        }
        int baseRowOffset = (min == Integer.MAX_VALUE) ? 0 : (min / 9) * 9;
        this.localDisplaySlots = shiftSlots(baseDisplaySlots, -baseRowOffset);

        this.guiRows = guiRowsForCount(this.massCount);
    }

    @Override
    public void start() {
        this.prepareRewards();
        this.createGui();
        this.gui.open(this.player);
        this.startAnimation();
    }

    @Override
    protected void onFinish() {
        if (battleService != null) {
            battleService.rouletteFinished(owner);
        }
    }

    @Override
    protected void onCancel() {
        if (battleService != null) {
            battleService.rouletteFinished(owner);
        }
    }

    private int guiRowsForCount(int count) {
        return switch (count) {
            case 2 -> 4;
            case 3 -> 5;
            case 4, 5, 6 -> 6;
            default -> 3;
        };
    }

    private int[] rouletteRows(int count) {
        return switch (count) {
            case 2 -> new int[]{1, 2};
            case 3 -> new int[]{1, 2, 3};
            case 4 -> new int[]{1, 2, 3, 4};
            case 5 -> new int[]{1, 2, 3, 4, 5};
            case 6 -> new int[]{0, 1, 2, 3, 4, 5};
            default -> new int[]{1};
        };
    }

    private void prepareRewards() {
        this.lanes = new ArrayList<>();
        this.laneStartIndex = new int[this.massCount];

        int usable = Math.min(this.massCount, this.winners.size());

        for (int laneIdx = 0; laneIdx < usable; laneIdx++) {
            CrateReward winner = this.winners.get(laneIdx);

            List<CrateReward> shuffled = new ArrayList<>(this.crate.getRewards());
            Collections.shuffle(shuffled);

            int winnerIndex = ThreadLocalRandom.current().nextInt(Math.max(1, shuffled.size()));
            shuffled.add(winnerIndex, winner);

            while (shuffled.size() < 50) {
                shuffled.addAll(new ArrayList<>(this.crate.getRewards()));
            }

            this.lanes.add(shuffled);
            this.laneStartIndex[laneIdx] = ThreadLocalRandom.current().nextInt(shuffled.size());
        }
    }

    private void createGui() {
        RouletteInvConfig cfg = this.plugin.getConfigService().getRouletteInv();
        final int rows = Math.max(1, Math.min(6, this.guiRows));

        Gui gui = Gui.gui()
                .title(Component.text(TextUtil.color(cfg.title)))
                .rows(rows)
                .disableAllInteractions()
                .create();

        this.gui = gui;
        this.inv = gui.getInventory();

        this.applyStaticItems(cfg);
        this.updateDisplay();
    }

    private void applyStaticItems(RouletteInvConfig cfg) {
        if (cfg == null || cfg.items == null) {
            return;
        }

        int size = this.guiRows * 9;

        for (RouletteInvConfig.GuiItem item : cfg.items.values()) {
            if (item == null) continue;

            int baseSlot = item.slot;

            for (int row = 0; row < this.guiRows; row++) {
                int slot = baseSlot + (row * 9);
                if (slot < 0 || slot >= size) continue;

                ItemStack stack = ItemBuilder.of(item.material == null ? Material.BARRIER : item.material)
                        .placeholders(Map.of("crate", this.crate.getDisplayName()))
                        .name(item.name)
                        .lore(item.lore)
                        .build();

                this.gui.setItem(slot, new GuiItem(stack, e -> e.setCancelled(true)));
            }
        }
    }

    private void updateDisplay() {
        boolean showWinners = this.currentTick >= (ROULETTE_DURATION - 5);

        int guiSize = this.guiRows * 9;
        int[] rows = rouletteRows(this.massCount);
        int laneCount = Math.min(this.massCount, this.lanes != null ? this.lanes.size() : 0);

        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++) {
            if (laneIndex >= rows.length) {
                break;
            }

            int targetRow = rows[laneIndex];
            int shift = targetRow * 9;

            List<CrateReward> lane = this.lanes.get(laneIndex);
            CrateReward winner = this.winners.get(laneIndex);

            int[] laneSlots = shiftSlots(this.localDisplaySlots, shift);
            int winnerSlot = laneSlots.length > 0 ? laneSlots[laneSlots.length / 2] : (13 + shift);

            for (int i = 0; i < laneSlots.length; i++) {
                int guiSlot = laneSlots[i];
                if (guiSlot < 0 || guiSlot >= guiSize) {
                    continue;
                }

                if (showWinners && guiSlot == winnerSlot) {
                    if (winner != null && winner.getDisplayItem() != null) {
                        this.gui.updateItem(guiSlot, new GuiItem(winner.getDisplayItem().clone()));
                    }
                    continue;
                }

                int base = (this.laneStartIndex != null && laneIndex < this.laneStartIndex.length)
                        ? this.laneStartIndex[laneIndex]
                        : 0;

                int rewardIndex = (base + this.currentIndex + i) % lane.size();
                CrateReward r = lane.get(rewardIndex);
                if (r == null || r.getDisplayItem() == null) {
                    continue;
                }

                this.gui.updateItem(guiSlot, new GuiItem(r.getDisplayItem().clone()));
            }
        }
    }

    private int[] shiftSlots(int[] base, int shift) {
        int[] out = new int[base.length];
        for (int i = 0; i < base.length; i++) {
            out[i] = base[i] + shift;
        }
        return out;
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
        return this.inv != null && this.inv.equals(inventory);
    }
}
