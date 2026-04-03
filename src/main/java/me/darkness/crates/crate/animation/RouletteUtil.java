package me.darkness.crates.crate.animation;

import dev.darkness.utilities.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.configuration.Inv.RouletteInvConfig;
import me.darkness.crates.crate.reward.CrateReward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RouletteUtil {

    private RouletteUtil() {}

    public static List<CrateReward> buildLane(List<CrateReward> pool, CrateReward winner, int minSize) {
        List<CrateReward> lane = new ArrayList<>(pool);
        Collections.shuffle(lane);
        lane.add(ThreadLocalRandom.current().nextInt(Math.max(1, lane.size())), winner);
        while (lane.size() < minSize) lane.addAll(pool);
        return lane;
    }

    public static int[] resolveLocalSlots(List<Integer> cfgSlots, List<Integer> fallback) {
        List<Integer> slots = (cfgSlots == null || cfgSlots.isEmpty()) ? fallback : cfgSlots;
        int minSlot = slots.stream().min(Integer::compare).orElse(0);
        int baseRowOffset = (minSlot / 9) * 9;
        return slots.stream().mapToInt(s -> s - baseRowOffset).toArray();
    }

    public static int rowsForCount(int count) {
        return switch (count) {
            case 1, 2 -> 3;
            case 3 -> 5;
            default -> 6;
        };
    }

    public static int[] rowMappingForCount(int count) {
        return switch (count) {
            case 1 -> new int[]{1};
            case 2 -> new int[]{1, 2};
            case 3 -> new int[]{1, 2, 3};
            case 4 -> new int[]{1, 2, 3, 4};
            case 5 -> new int[]{0, 1, 2, 3, 4};
            default -> new int[]{0, 1, 2, 3, 4, 5};
        };
    }

    public static void applyStaticItems(Gui gui, RouletteInvConfig cfg, int guiRows, int laneRows) {
        if (cfg.items == null) return;
        cfg.items.values().forEach(item -> {
            if (item == null) return;
            if (laneRows <= 1) {
                if (item.slot >= 0 && item.slot < guiRows * 9) {
                    gui.setItem(item.slot, buildGuiItem(item));
                }
            } else {
                for (int r = 0; r < laneRows; r++) {
                    int slot = item.slot + r * 9;
                    if (slot < guiRows * 9) {
                        gui.setItem(slot, buildGuiItem(item));
                    }
                }
            }
        });
    }

    private static GuiItem buildGuiItem(RouletteInvConfig.GuiItem item) {
        return new GuiItem(ItemBuilder.of(item.material).name(item.name).lore(item.lore).build());
    }
}
