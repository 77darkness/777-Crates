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

import java.util.List;

public final class RouletteAnimation extends CrateAnimation {

    private final int[] slots;
    private List<CrateReward> lane;
    private Gui gui;
    private int tick = 0;
    private int frameDelay = 2;
    private int frameTick = 0;
    private int index = 0;

    public RouletteAnimation(CratesPlugin plugin, Player player, Crate crate, CrateReward reward) {
        super(plugin, player, crate, reward);
        List<Integer> cfgSlots = plugin.getConfigService().getRouletteInv().displaySlots;
        this.slots = (cfgSlots != null ? cfgSlots : List.of(10, 11, 12, 13, 14, 15, 16))
                .stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public void start() {
        this.lane = RouletteUtil.buildLane(crate.getRewards(), reward, 80);

        RouletteInvConfig cfg = plugin.getConfigService().getRouletteInv();
        this.gui = Gui.gui()
                .title(TextUtil.toComponent(cfg.title))
                .rows(cfg.rows > 0 ? cfg.rows : 3)
                .disableAllInteractions()
                .create();

        RouletteUtil.applyStaticItems(gui, cfg, gui.getRows(), 1);

        for (int s : slots) {
            gui.setItem(s, new GuiItem(lane.get(s % lane.size()).getDisplayItem()));
        }
        this.gui.open(player);

        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            tick++;
            frameTick++;

            if (frameTick >= frameDelay) {
                frameTick = 0;
                index = (index + 1) % lane.size();
                scroll();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.45f, getPitch());
            }

            frameDelay = getFrameDelay();
            if (tick >= 160) stop();
        }, 0L, 1L);
    }

    private int getFrameDelay() {
        if (tick <= 50) {
            return 2;
        } else if (tick <= 110) {
            int progress = tick - 50;
            return 2 + (int) Math.floor(4.0 * progress / (110 - 50));
        } else {
            int progress = tick - 110;
            return 6 + (int) Math.floor(14.0 * progress / (160 - 110));
        }
    }

    private float getPitch() {
        if (tick <= 50) return 1.5f;
        float t = (float)(tick - 50) / (160 - 50);
        return Math.max(0.5f, 1.5f - t);
    }

    private void scroll() {
        for (int s : slots) {
            gui.updateItem(s, new GuiItem(lane.get((index + s) % lane.size()).getDisplayItem()));
        }
    }

    private void stop() {
        task.cancel();
        gui.updateItem(slots[slots.length / 2], new GuiItem(reward.getDisplayItem()));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.9f);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.closeInventory();
            finish();
        }, 50);
    }

    @Override public boolean isAnimation(Inventory inv) { return gui != null && gui.getInventory().equals(inv); }
}