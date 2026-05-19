package me.darkness.crates.crate.reward;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CrateReward {

    private final ItemStack displayItem;
    private final ItemStack rewardItem;
    private final List<String> commands;
    private double chance;
    private final RewardType type;

    public CrateReward(ItemStack displayItem, ItemStack rewardItem, List<String> commands, double chance, RewardType type) {
        this.displayItem = displayItem;
        this.rewardItem = rewardItem;
        this.commands = commands;
        this.chance = chance;
        this.type = type;
    }

    public ItemStack getDisplayItem() { return displayItem; }
    public ItemStack getRewardItem() { return rewardItem; }
    public List<String> getCommands() { return commands; }
    public double getChance() { return chance; }
    public RewardType getType() { return type; }
    public boolean shouldGiveItem() { return type == RewardType.ITEM; }

    public CrateReward withChance(double newChance) {
        return new CrateReward(displayItem, rewardItem, commands, newChance, type);
    }
}
