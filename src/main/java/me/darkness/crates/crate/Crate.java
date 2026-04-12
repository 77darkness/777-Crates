package me.darkness.crates.crate;

import me.darkness.crates.crate.animation.AnimationType;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.configuration.Lang;
import me.darkness.crates.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Crate {

    private final String name;
    private final String displayName;
    private final AnimationType animationType;
    private final ItemStack key;
    private final Integer keyCustomModelData;
    private final List<Location> locations;
    private final List<CrateReward> rewards;
    private final List<String> hologramLines;
    private final double hologramHeight;
    private final boolean hologramEnabled;
    private final boolean rewardBroadcastEnabled;
    private final double rewardBroadcastMaxChance;
    private final Lang.MessageEntry rewardBroadcast;

    public Crate(
            String name,
            String displayName,
            AnimationType animationType,
            ItemStack key,
            Integer keyCustomModelData,
            List<Location> locations,
            List<CrateReward> rewards,
            List<String> hologramLines,
            double hologramHeight,
            boolean hologramEnabled,
            boolean rewardBroadcastEnabled,
            double rewardBroadcastMaxChance,
            Lang.MessageEntry rewardBroadcast
    ) {
        this.name = name;
        this.displayName = displayName;
        this.animationType = animationType;
        this.key = key;
        this.keyCustomModelData = keyCustomModelData;
        this.locations = locations == null ? new ArrayList<>() : new ArrayList<>(locations);
        this.rewards = rewards == null ? new ArrayList<>() : new ArrayList<>(rewards);
        this.hologramLines = hologramLines == null ? new ArrayList<>() : new ArrayList<>(hologramLines);
        this.hologramHeight = hologramHeight;
        this.hologramEnabled = hologramEnabled;
        this.rewardBroadcastEnabled = rewardBroadcastEnabled;
        this.rewardBroadcastMaxChance = rewardBroadcastMaxChance;
        this.rewardBroadcast = rewardBroadcast;
    }

    public String getName() { return this.name; }
    public String getDisplayName() { return this.displayName; }
    public AnimationType getAnimationType() { return this.animationType; }
    public ItemStack getKey() { return this.key; }
    public Integer getKeyCustomModelData() { return this.keyCustomModelData; }
    public Location getLocation() { return this.locations.isEmpty() ? null : this.locations.get(0); }
    public List<Location> getLocations() { return new ArrayList<>(this.locations); }
    public List<CrateReward> getRewards() { return new ArrayList<>(this.rewards); }
    public List<String> getHologramLines() { return new ArrayList<>(this.hologramLines); }
    public double getHologramHeight() { return this.hologramHeight; }
    public boolean isHologramEnabled() { return this.hologramEnabled; }
    public boolean isRewardBroadcastEnabled() { return this.rewardBroadcastEnabled; }
    public double getRewardBroadcastMaxChance() { return this.rewardBroadcastMaxChance; }
    public Lang.MessageEntry getRewardBroadcast() { return this.rewardBroadcast; }

    public Crate withLocations(List<Location> newLocations) {
        return new Crate(name, displayName, animationType, key, keyCustomModelData, newLocations, rewards, hologramLines, hologramHeight, hologramEnabled, rewardBroadcastEnabled, rewardBroadcastMaxChance, rewardBroadcast);
    }

    public Crate withAddedLocation(Location location) {
        if (location == null) return this;
        List<Location> copy = new ArrayList<>(this.locations);
        if (copy.stream().noneMatch(loc -> LocationUtil.isSameBlock(loc, location))) {
            copy.add(location);
        }
        return withLocations(copy);
    }

    public Crate withRemovedLocation(Location location) {
        if (location == null || this.locations.isEmpty()) return this;
        List<Location> copy = new ArrayList<>(this.locations);
        copy.removeIf(loc -> LocationUtil.isSameBlock(loc, location));
        return withLocations(copy);
    }

    public Crate withRewards(List<CrateReward> newRewards) {
        return new Crate(name, displayName, animationType, key, keyCustomModelData, locations, newRewards, hologramLines, hologramHeight, hologramEnabled, rewardBroadcastEnabled, rewardBroadcastMaxChance, rewardBroadcast);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Crate crate)) return false;
        return Objects.equals(this.name, crate.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }
}
