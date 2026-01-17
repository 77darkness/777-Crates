package me.darkness.crates.crate.reward;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class SlottedCrateReward extends CrateReward {

    private final int slot;

    public SlottedCrateReward(int slot, ItemStack displayItem, ItemStack rewardItem, List<String> commands, double chance, boolean giveItem) {
        super(displayItem, rewardItem, commands, chance, giveItem);
        this.slot = slot;
    }

    public int getSlot() {
        return this.slot;
    }
}

