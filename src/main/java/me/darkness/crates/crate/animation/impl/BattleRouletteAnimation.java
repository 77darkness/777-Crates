package me.darkness.crates.crate.animation.impl;

import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.RouletteInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.CrateAnimation;
import me.darkness.crates.crate.animation.RouletteUtil;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.reward.CrateReward;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class BattleRouletteAnimation extends CrateAnimation {

    private final BattleService battleService;
    private final UUID owner;
    private final List<CrateReward> winners;
    private final int massCount;
    private final int[] localSlots;
    private final int guiRows;

    private Gui gui;
    private List<List<CrateReward>> lanes;
    private int[] laneOffsets;
    private int tick = 0, delay = 2;

    public BattleRouletteAnimation(CratesPlugin plugin, BattleService battleService, Player player, Crate crate, List<CrateReward> winners) {
        super(plugin, player, crate, winners != null && !winners.isEmpty() ? winners.get(0) : null);
        this.battleService = battleService;
        this.owner = player.getUniqueId();
        this.winners = winners != null ? List.copyOf(winners) : List.of();
        this.massCount = Math.max(1, Math.min(6, this.winners.size()));

        this.localSlots = RouletteUtil.resolveLocalSlots(
                plugin.getConfigService().getRouletteInv().displaySlots,
                List.of(10, 11, 12, 13, 14, 15, 16));

        this.guiRows = switch (this.massCount) {
            case 2 -> 4;
            case 3 -> 5;
            default -> this.massCount >= 4 ? 6 : 3;
        };
    }

    @Override
    public void start() {
        this.lanes = winners.stream()
                .map(w -> RouletteUtil.buildLane(crate.getRewards(), w, 50))
                .toList();

        this.laneOffsets = new int[lanes.size()];
        for (int i = 0; i < laneOffsets.length; i++) {
            laneOffsets[i] = ThreadLocalRandom.current().nextInt(lanes.get(i).size());
        }

        RouletteInvConfig cfg = plugin.getConfigService().getRouletteInv();
        this.gui = Gui.gui()
                .title(TextUtil.toComponent(cfg.title))
                .rows(guiRows)
                .disableAllInteractions()
                .create();

        RouletteUtil.applyStaticItems(gui, cfg, guiRows, massCount);

        int[] rowMapping = RouletteUtil.rowMappingForCount(massCount);
        for (int i = 0; i < lanes.size(); i++) {
            int rowBase = rowMapping[i] * 9;
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
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                if (tick > 75 && tick % 4 == 0) delay++;
            }
            if (tick >= 100) stop();
        }, 0L, 1L);
    }

    private void updateDisplay() {
        int[] rowMapping = RouletteUtil.rowMappingForCount(massCount);
        boolean isFinal = tick >= 96;
        int centerSlot = localSlots[localSlots.length / 2];

        for (int i = 0; i < lanes.size(); i++) {
            int rowBase = rowMapping[i] * 9;
            List<CrateReward> lane = lanes.get(i);
            for (int slot : localSlots) {
                ItemStack item = (isFinal && slot == centerSlot)
                        ? winners.get(i).getDisplayItem()
                        : lane.get((laneOffsets[i] + slot) % lane.size()).getDisplayItem();
                gui.updateItem(slot + rowBase, new GuiItem(item));
            }
        }
    }

    private void stop() {
        task.cancel();
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.closeInventory();
            battleService.rouletteFinished(owner);
            finish();
        }, 40L);
    }


    @Override public boolean isAnimation(Inventory inv) { return gui != null && gui.getInventory().equals(inv); }
    @Override public boolean isLocked() { return true; }
    @Override protected void onFinish() {}
    @Override protected void onCancel() {}
}