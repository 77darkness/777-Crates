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
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.reward.CrateReward;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class BattleRouletteAnimation extends CrateAnimation {

    private final BattleService battleService;
    private final UUID owner;
    private final List<CrateReward> winners;
    private final int massCount;
    private final int guiRows;

    private Gui gui;
    private List<List<CrateReward>> lanes;
    private GuiItem[][] laneItems;
    private int[] laneOffsets;
    private final int[] rowMapping;

    private int localTick, delay = 2;
    private boolean stopping;

    public BattleRouletteAnimation(CratesPlugin plugin, BattleService battleService, Player player, Crate crate, List<CrateReward> winners) {
        super(plugin, player, crate, winners != null && !winners.isEmpty() ? winners.get(0) : null);
        this.battleService = battleService;
        this.owner = player.getUniqueId();
        this.winners = winners != null ? List.copyOf(winners) : List.of();
        this.massCount = Math.max(1, Math.min(6, this.winners.size()));
        this.guiRows = switch (this.massCount) {
            case 2 -> 4;
            case 3 -> 5;
            default -> this.massCount >= 4 ? 6 : 3;
        };
        this.rowMapping = RouletteUtil.rowMappingForCount(this.massCount);
    }

    @Override
    public void start() {
        this.lanes = winners.stream()
                .map(w -> RouletteUtil.buildLane(crate.getRewards(), 50))
                .toList();

        this.laneItems = new GuiItem[lanes.size()][];
        for (int i = 0; i < lanes.size(); i++) {
            List<CrateReward> lane = lanes.get(i);
            GuiItem[] items = new GuiItem[lane.size()];
            for (int j = 0; j < lane.size(); j++) {
                items[j] = new GuiItem(lane.get(j).getDisplayItem());
            }
            laneItems[i] = items;
        }

        this.laneOffsets = new int[lanes.size()];
        for (int i = 0; i < laneOffsets.length; i++) {
            laneOffsets[i] = ThreadLocalRandom.current().nextInt(lanes.get(i).size());
        }

        RouletteInvConfig cfg = plugin.getConfigService().rouletteInv();
        this.gui = Gui.gui()
                .title(TextUtil.toComponent(cfg.title))
                .rows(guiRows)
                .disableAllInteractions()
                .create();

        RouletteUtil.applyStaticItems(gui, cfg, guiRows, massCount);

        for (int i = 0; i < lanes.size(); i++) {
            int rowBase = rowMapping[i] * 9;
            List<CrateReward> lane = lanes.get(i);
            for (int slot : new int[]{1, 2, 3, 4, 5, 6, 7}) {
                gui.setItem(slot + rowBase, new GuiItem(lane.get(slot % lane.size()).getDisplayItem()));
            }
        }

        gui.setCloseGuiAction(event -> {
            if (isFinished()) return;
            SchedulerUtil.run(plugin, () -> {
                if (player.isOnline()) player.openInventory(gui.getInventory());
            });
        });

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
            updateDisplay(localTick >= 96);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            if (localTick > 75 && localTick % 4 == 0) delay++;
        }
        if (localTick >= 100) stop();
    }

    private void updateDisplay(boolean isFinal) {
        for (int i = 0; i < lanes.size(); i++) {
            int rowBase = rowMapping[i] * 9;
            int laneSize = lanes.get(i).size();
            GuiItem[] items = laneItems[i];
            for (int slot : new int[]{1, 2, 3, 4, 5, 6, 7}) {
                GuiItem item = (isFinal && slot == 4)
                        ? new GuiItem(winners.get(i).getDisplayItem())
                        : items[(laneOffsets[i] + slot) % laneSize];
                gui.updateItem(slot + rowBase, item);
            }
        }
    }

    private void stop() {
        stopping = true;
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        SchedulerUtil.runLater(plugin, () -> {
            battleService.rouletteFinished(owner);
            finish();
            player.closeInventory();
        }, 40L);
    }


    @Override public boolean isAnimation(Inventory inv) { return gui != null && gui.getInventory().equals(inv); }
    @Override public boolean isLocked() { return true; }
    @Override protected void onFinish() {}
    @Override protected void onCancel() {}
}