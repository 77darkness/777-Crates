package me.darkness.crates.crate.animation.impl;

import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.RouletteInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.CrateAnimation;
import me.darkness.crates.crate.animation.RouletteUtil;
import me.darkness.crates.crate.reward.CrateReward;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MassRouletteAnimation extends CrateAnimation {

    private final List<CrateReward> winners;
    private final int massCount;
    private final int rows;
    private final int[] localSlots;

    private Gui gui;
    private List<List<CrateReward>> lanes;
    private int[] laneOffsets;
    private int tick = 0, delay = 2;

    public MassRouletteAnimation(CratesPlugin plugin, Player player, Crate crate, List<CrateReward> winners) {
        super(plugin, player, crate, winners != null && !winners.isEmpty() ? winners.get(0) : null);
        this.winners = winners != null ? List.copyOf(winners) : List.of();
        this.massCount = Math.min(6, this.winners.size());
        this.rows = RouletteUtil.rowsForCount(massCount);
        this.localSlots = RouletteUtil.resolveLocalSlots(
                plugin.getConfigService().getRouletteInv().displaySlots,
                List.of(10, 11, 12, 13, 14, 15, 16));
    }

    @Override
    public void start() {
        int centerLocalSlot = localSlots[localSlots.length / 2];
        int simulatedFinalOffset = simulateFinalOffset();

        List<List<CrateReward>> mutableLanes = new java.util.ArrayList<>();
        for (int i = 0; i < winners.size(); i++) {
            List<CrateReward> lane = RouletteUtil.buildLane(crate.getRewards(), winners.get(i), 60);
            RouletteUtil.placeWinnerForCenter(lane, winners.get(i), centerLocalSlot, simulatedFinalOffset);
            mutableLanes.add(lane);
        }
        this.lanes = java.util.Collections.unmodifiableList(mutableLanes);

        this.laneOffsets = new int[lanes.size()];
        for (int i = 0; i < laneOffsets.length; i++) {
            laneOffsets[i] = ThreadLocalRandom.current().nextInt(lanes.get(i).size());
        }

        RouletteInvConfig cfg = plugin.getConfigService().getRouletteInv();
        this.gui = Gui.gui()
                .title(TextUtil.toComponent(cfg.title))
                .rows(rows)
                .disableAllInteractions()
                .create();

        RouletteUtil.applyStaticItems(gui, cfg, rows, massCount);

        int[] rowOffsets = RouletteUtil.rowMappingForCount(massCount);
        for (int i = 0; i < lanes.size(); i++) {
            int rowBase = rowOffsets[i] * 9;
            List<CrateReward> lane = lanes.get(i);
            for (int slot : localSlots) {
                gui.setItem(slot + rowBase, new GuiItem(lane.get(slot % lane.size()).getDisplayItem()));
            }
        }
        this.gui.open(player);

        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            tick++;
            if (tick % delay == 0) {
                for (int i = 0; i < laneOffsets.length; i++) {
                    laneOffsets[i] = (laneOffsets[i] + 1) % lanes.get(i).size();
                }
                updateDisplay();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.1f);
                if (tick > 70 && tick % 5 == 0) delay++;
            }
            if (tick >= 105) stop();
        }, 0L, 1L);
    }

    private int simulateFinalOffset() {
        int simTick = 0, simDelay = 2, simOffset = 0;
        while (simTick < 105) {
            simTick++;
            if (simTick % simDelay == 0) {
                simOffset++;
                if (simTick > 70 && simTick % 5 == 0) simDelay++;
            }
        }
        return simOffset;
    }

    private void updateDisplay() {
        int[] rowOffsets = RouletteUtil.rowMappingForCount(massCount);

        for (int i = 0; i < lanes.size(); i++) {
            int rowBase = rowOffsets[i] * 9;
            List<CrateReward> lane = lanes.get(i);
            for (int slot : localSlots) {
                ItemStack item = lane.get((laneOffsets[i] + slot) % lane.size()).getDisplayItem();
                gui.updateItem(slot + rowBase, new GuiItem(item));
            }
        }
    }

    private void stop() {
        task.cancel();
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.closeInventory();
            winners.forEach(r -> plugin.getRewardExecutor().giveReward(player, crate, r));
            finish();
        }, 40L);
    }

    @Override
    protected void onFinish() {}

    @Override
    protected void onCancel() {
        winners.forEach(r -> plugin.getRewardExecutor().giveReward(player, crate, r));
    }

    @Override public boolean isAnimation(Inventory inv) { return gui != null && gui.getInventory().equals(inv); }
}