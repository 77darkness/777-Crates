package me.darkness.crates.crate;

import me.darkness.crates.configuration.LangConfig;
import me.darkness.crates.crate.animation.AnimationType;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.utils.LocationUtil;
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
    private final Integer keyCustomModel;
    private final List<Location> locations;
    private List<CrateReward> rewards;
    private final List<String> hologramLines;
    private final double hologramHeight;
    private final boolean hologramEnabled;
    private final boolean rewardBroadcastEnabled;
    private final double rewardBroadcastMaxChance;
    private final LangConfig.MessageEntry rewardBroadcast;

    private Crate(Builder builder) {
        this.name = builder.name;
        this.displayName = builder.displayName;
        this.animationType = builder.animationType;
        this.key = builder.key;
        this.keyCustomModel = builder.keyCustomModel;
        this.locations = builder.locations;
        this.rewards = builder.rewards;
        this.hologramLines = builder.hologramLines;
        this.hologramHeight = builder.hologramHeight;
        this.hologramEnabled = builder.hologramEnabled;
        this.rewardBroadcastEnabled = builder.rewardBroadcastEnabled;
        this.rewardBroadcastMaxChance = builder.rewardBroadcastMaxChance;
        this.rewardBroadcast = builder.rewardBroadcast;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String name;
        private String displayName = "";
        private AnimationType animationType = AnimationType.ROULETTE;
        private ItemStack key;
        private Integer keyCustomModel;
        private List<Location> locations = new ArrayList<>();
        private List<CrateReward> rewards = new ArrayList<>();
        private List<String> hologramLines = new ArrayList<>();
        private double hologramHeight = 2.1;
        private boolean hologramEnabled = true;
        private boolean rewardBroadcastEnabled = false;
        private double rewardBroadcastMaxChance = 100.0;
        private LangConfig.MessageEntry rewardBroadcast;

        private Builder(String name) {
            this.name = name;
        }

        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        public Builder animationType(AnimationType animationType) { this.animationType = animationType; return this; }
        public Builder key(ItemStack key) { this.key = key; return this; }
        public Builder keyCustomModel(Integer keyCustomModel) { this.keyCustomModel = keyCustomModel; return this; }
        public Builder locations(List<Location> locations) { this.locations = locations; return this; }
        public Builder rewards(List<CrateReward> rewards) { this.rewards = rewards; return this; }
        public Builder hologramLines(List<String> hologramLines) { this.hologramLines = hologramLines; return this; }
        public Builder hologramHeight(double hologramHeight) { this.hologramHeight = hologramHeight; return this; }
        public Builder hologramEnabled(boolean hologramEnabled) { this.hologramEnabled = hologramEnabled; return this; }
        public Builder rewardBroadcastEnabled(boolean rewardBroadcastEnabled) { this.rewardBroadcastEnabled = rewardBroadcastEnabled; return this; }
        public Builder rewardBroadcastMaxChance(double rewardBroadcastMaxChance) { this.rewardBroadcastMaxChance = rewardBroadcastMaxChance; return this; }
        public Builder rewardBroadcast(LangConfig.MessageEntry rewardBroadcast) { this.rewardBroadcast = rewardBroadcast; return this; }

        public Crate build() {
            return new Crate(this);
        }
    }

    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public AnimationType getAnimationType() { return animationType; }
    public ItemStack getKey() { return key; }
    public Integer getKeyCustomModel() { return keyCustomModel; }
    public Location getLocation() { return locations.isEmpty() ? null : locations.get(0); }
    public List<Location> getLocations() { return locations; }
    public List<CrateReward> getRewards() { return rewards; }
    public List<String> getHologramLines() { return hologramLines; }
    public double getHologramHeight() { return hologramHeight; }
    public boolean isHologramEnabled() { return hologramEnabled; }
    public boolean isRewardBroadcastEnabled() { return rewardBroadcastEnabled; }
    public double getRewardBroadcastMaxChance() { return rewardBroadcastMaxChance; }
    public LangConfig.MessageEntry getRewardBroadcast() { return rewardBroadcast; }

    public void addLocation(Location location) {
        if (location == null) return;
        if (locations.stream().noneMatch(loc -> LocationUtil.isSameBlock(loc, location)))
            locations.add(location);
    }

    public void removeLocation(Location location) {
        if (location == null) return;
        locations.removeIf(loc -> LocationUtil.isSameBlock(loc, location));
    }

    public void setRewards(List<CrateReward> newRewards) {
        this.rewards = newRewards;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Crate other)) return false;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
