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

public final class RouletteAnimation extends CrateAnimation {

    private List<CrateReward> lane;
    private GuiItem[] laneItems;
    private Gui gui;

    private int localTick, frameDelay = 2, frameTick, index;
    private boolean stopping;

    public RouletteAnimation(CratesPlugin plugin, Player player, Crate crate, CrateReward reward) {
        super(plugin, player, crate, reward);
    }

    @Override
    public void start() {
        this.lane = RouletteUtil.buildLane(crate.getRewards(), 80);

        RouletteUtil.placeWinner(this.lane, reward, 13, 48);

        this.laneItems = new GuiItem[lane.size()];
        for (int i = 0; i < lane.size(); i++) {
            laneItems[i] = new GuiItem(lane.get(i).getDisplayItem());
        }

        RouletteInvConfig cfg = plugin.getConfigService().rouletteInv();
        this.gui = Gui.gui()
                .title(TextUtil.toComponent(cfg.title))
                .rows(cfg.rows > 0 ? cfg.rows : 3)
                .disableAllInteractions()
                .create();

        RouletteUtil.applyStaticItems(gui, cfg, gui.getRows(), 1);

        for (int s : new int[]{10, 11, 12, 13, 14, 15, 16}) {
            gui.setItem(s, new GuiItem(lane.get(s % lane.size()).getDisplayItem()));
        }

        gui.setCloseGuiAction(event -> cancelAnimation());

        this.gui.open(player);
    }

    @Override
    public void tick() {
        if (stopping) return;

        localTick++;
        frameTick++;

        if (frameTick >= frameDelay) {
            frameTick = 0;
            index = (index + 1) % lane.size();
            for (int s : new int[]{10, 11, 12, 13, 14, 15, 16}) {
                gui.updateItem(s, laneItems[(index + s) % lane.size()]);
            }
            float t = (float)(localTick - 50) / 110;
            float pitch = localTick <= 50 ? 1.5f : Math.max(0.5f, 1.5f - t);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.45f, pitch);
        }

        if (localTick <= 50) frameDelay = 2;
        else if (localTick <= 110) frameDelay = 2 + (int) Math.floor(4.0 * (localTick - 50) / 60);
        else frameDelay = 6 + (int) Math.floor(14.0 * (localTick - 110) / 50);

        if (localTick >= 160) stop();
    }

    private void stop() {
        stopping = true;
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.9f);
        SchedulerUtil.runLater(plugin, () -> {
            finish();
            player.closeInventory();
        }, 50);
    }

    @Override public boolean isAnimation(Inventory inv) { return gui != null && gui.getInventory().equals(inv); }
}