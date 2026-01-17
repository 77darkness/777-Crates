package me.darkness.crates.crate.reward;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CrateReward {

    private final ItemStack displayItem;
    private final ItemStack rewardItem;
    private final List<String> commands;
    private final double chance;
    private final boolean giveItem;

    public CrateReward(ItemStack displayItem, ItemStack rewardItem, List<String> commands, double chance, boolean giveItem) {
        this.displayItem = displayItem;
        this.rewardItem = rewardItem;
        this.commands = new ArrayList<>(commands == null ? java.util.List.of() : commands);
        this.chance = chance;
        this.giveItem = giveItem;
    }

    public ItemStack getDisplayItem() {
        return this.displayItem;
    }

    public ItemStack getRewardItem() {
        return this.rewardItem;
    }

    public List<String> getCommands() {
        return new ArrayList<>(this.commands);
    }

    public double getChance() {
        return this.chance;
    }

    public boolean shouldGiveItem() {
        return this.giveItem;
    }
}
