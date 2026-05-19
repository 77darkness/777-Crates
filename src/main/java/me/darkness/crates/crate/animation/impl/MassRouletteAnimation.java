package me.darkness.crates.crate.animation.impl;

import dev.darkness.utilities.task.SchedulerUtil;
import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.inventories.RouletteInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.CrateAnimation;
import me.darkness.crates.utils.RouletteUtil;
import me.darkness.crates.crate.reward.CrateReward;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MassRouletteAnimation extends CrateAnimation {

    private final List<CrateReward> winners;
    private final int massCount;
    private final int rows;

    private Gui gui;
    private List<List<CrateReward>> lanes;
    private GuiItem[][] laneItems;
    private int[] laneOffsets;
    private final int[] rowOffsets;

    private int localTick, delay = 2;
    private boolean stopping;

    public MassRouletteAnimation(CratesPlugin plugin, Player player, Crate crate, List<CrateReward> winners) {
        super(plugin, player, crate, winners != null && !winners.isEmpty() ? winners.get(0) : null);
        this.winners = winners != null ? List.copyOf(winners) : List.of();
        this.massCount = Math.min(6, this.winners.size());
        this.rows = RouletteUtil.rowsForCount(massCount);
        this.rowOffsets = RouletteUtil.rowMappingForCount(massCount);
    }

    @Override
    public void start() {
        List<List<CrateReward>> mutableLanes = new java.util.ArrayList<>();
        for (int i = 0; i < winners.size(); i++) {
            List<CrateReward> lane = RouletteUtil.buildLane(crate.getRewards(), 60);
            RouletteUtil.placeWinner(lane, winners.get(i), 4, 48);
            mutableLanes.add(lane);
        }
        this.lanes = java.util.Collections.unmodifiableList(mutableLanes);

        this.laneItems = new GuiItem[this.lanes.size()][];
        for (int i = 0; i < this.lanes.size(); i++) {
            List<CrateReward> lane = this.lanes.get(i);
            GuiItem[] items = new GuiItem[lane.size()];
            for (int j = 0; j < lane.size(); j++) {
                items[j] = new GuiItem(lane.get(j).getDisplayItem());
            }
            this.laneItems[i] = items;
        }

        this.laneOffsets = new int[lanes.size()];
        for (int i = 0; i < laneOffsets.length; i++) {
            laneOffsets[i] = ThreadLocalRandom.current().nextInt(lanes.get(i).size());
        }

        RouletteInvConfig cfg = plugin.getConfigService().rouletteInv();
        this.gui = Gui.gui()
                .title(TextUtil.toComponent(cfg.title))
                .rows(rows)
                .disableAllInteractions()
                .create();

        RouletteUtil.applyStaticItems(gui, cfg, rows, massCount);

        for (int i = 0; i < lanes.size(); i++) {
            int rowBase = rowOffsets[i] * 9;
            List<CrateReward> lane = lanes.get(i);
            for (int slot : new int[]{1, 2, 3, 4, 5, 6, 7}) {
                gui.setItem(slot + rowBase, new GuiItem(lane.get(slot % lane.size()).getDisplayItem()));
            }
        }

        gui.setCloseGuiAction(event -> cancelAnimation());

        this.gui.open(player);
    }

    @Override
    public void tick() {
        if (stopping) return;

        localTick++;
        if (localTick % delay == 0) {
            for (int i = 0; i < laneOffsets.length; i++) {
                laneOffsets[i] = (laneOffsets[i] + 1) % lanes.get(i).size();
            }
            updateDisplay();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.1f);
            if (localTick > 70 && localTick % 5 == 0) delay++;
        }
        if (localTick >= 105) stop();
    }

    private void updateDisplay() {
        for (int i = 0; i < lanes.size(); i++) {
            int rowBase = rowOffsets[i] * 9;
            int laneSize = lanes.get(i).size();
            GuiItem[] items = laneItems[i];
            for (int slot : new int[]{1, 2, 3, 4, 5, 6, 7}) {
                gui.updateItem(slot + rowBase, items[(laneOffsets[i] + slot) % laneSize]);
            }
        }
    }

    private void stop() {
        stopping = true;
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        SchedulerUtil.runLater(plugin, () -> {
            finish();
            player.closeInventory();
        }, 40L);
    }

    @Override
    protected void onFinish() {
        winners.forEach(r -> plugin.getRewardExecutor().giveReward(player, crate, r));
    }

    @Override
    protected void onCancel() {
        winners.forEach(r -> plugin.getRewardExecutor().giveReward(player, crate, r));
    }

    @Override public boolean isAnimation(Inventory inv) { return gui != null && gui.getInventory().equals(inv); }
}