package me.darkness.crates.crate.reward;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CrateReward {

    private final ItemStack displayItem;
    private final ItemStack rewardItem;
    private final List<String> commands;
    private final double chance;
    private final RewardType type;

    public CrateReward(ItemStack displayItem, ItemStack rewardItem, List<String> commands, double chance, RewardType type) {
        this.displayItem = displayItem != null ? displayItem.clone() : null;
        this.rewardItem = rewardItem != null ? rewardItem.clone() : null;
        this.commands = new ArrayList<>(commands == null ? List.of() : commands);
        this.chance = chance;
        this.type = type;
    }

    public ItemStack getDisplayItem() { return this.displayItem != null ? this.displayItem.clone() : null; }
    public ItemStack getRewardItem() { return this.rewardItem != null ? this.rewardItem.clone() : null; }
    public List<String> getCommands() { return new ArrayList<>(this.commands); }
    public double getChance() { return this.chance; }
    public RewardType getType() { return this.type; }
    public boolean shouldGiveItem() { return this.type == RewardType.ITEM; }
}
