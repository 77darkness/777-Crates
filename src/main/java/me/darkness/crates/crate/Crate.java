package me.darkness.crates.crate;

import me.darkness.crates.crate.animation.AnimationType;
import me.darkness.crates.crate.reward.CrateReward;
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
    private final Double hologramHeight;
    private final Boolean hologramEnabled;

    public Crate(String name, String displayName, AnimationType animationType, ItemStack key, Location location, List<CrateReward> rewards) {
        this(name, displayName, animationType, key, null, location == null ? new ArrayList<>() : java.util.List.of(location), rewards, new ArrayList<>(), null, null);
    }

    public Crate(
        String name,
        String displayName,
        AnimationType animationType,
        ItemStack key,
        Location location,
        List<CrateReward> rewards,
        List<String> hologramLines,
        Double hologramHeight,
        Boolean hologramEnabled
    ) {
        this(name, displayName, animationType, key, null, location == null ? new ArrayList<>() : java.util.List.of(location), rewards, hologramLines, hologramHeight, hologramEnabled);
    }

    public Crate(
        String name,
        String displayName,
        AnimationType animationType,
        ItemStack key,
        List<Location> locations,
        List<CrateReward> rewards,
        List<String> hologramLines,
        Double hologramHeight,
        Boolean hologramEnabled
    ) {
        this(name, displayName, animationType, key, null, locations, rewards, hologramLines, hologramHeight, hologramEnabled);
    }

    public Crate(
        String name,
        String displayName,
        AnimationType animationType,
        ItemStack key,
        Integer keyCustomModelData,
        List<Location> locations,
        List<CrateReward> rewards,
        List<String> hologramLines,
        Double hologramHeight,
        Boolean hologramEnabled
    ) {
        this.name = name;
        this.displayName = displayName;
        this.animationType = animationType;
        this.key = key;
        this.keyCustomModelData = keyCustomModelData;
        this.locations = locations == null ? new ArrayList<>() : new ArrayList<>(locations);
        this.rewards = new ArrayList<>(rewards);
        this.hologramLines = hologramLines == null ? new ArrayList<>() : new ArrayList<>(hologramLines);
        this.hologramHeight = hologramHeight;
        this.hologramEnabled = hologramEnabled;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public AnimationType getAnimationType() {
        return this.animationType;
    }

    public ItemStack getKey() {
        return this.key;
    }

    public Integer getKeyCustomModelData() {
        return this.keyCustomModelData;
    }

    public Location getLocation() {
        return this.locations.isEmpty() ? null : this.locations.get(0);
    }

    public List<Location> getLocations() {
        return new ArrayList<>(this.locations);
    }

    public List<CrateReward> getRewards() {
        return new ArrayList<>(this.rewards);
    }

    public List<String> getHologramLines() {
        return new ArrayList<>(this.hologramLines);
    }

    public Double getHologramHeight() {
        return this.hologramHeight;
    }

    public Boolean isHologramEnabled() {
        return this.hologramEnabled;
    }

    public Crate withLocations(List<Location> newLocations) {
        return new Crate(this.name, this.displayName, this.animationType, this.key, this.keyCustomModelData, newLocations, this.rewards, this.hologramLines, this.hologramHeight, this.hologramEnabled);
    }

    public Crate withAddedLocation(Location location) {
        if (location == null) {
            return this;
        }
        List<Location> copy = new ArrayList<>(this.locations);
        boolean exists = copy.stream().anyMatch(loc -> multiLocation(loc, location));
        if (!exists) {
            copy.add(location);
        }
        return withLocations(copy);
    }

    public Crate withRemovedLocation(Location location) {
        if (location == null || this.locations.isEmpty()) {
            return this;
        }
        List<Location> copy = new ArrayList<>();
        for (Location loc : this.locations) {
            if (!multiLocation(loc, location)) {
                copy.add(loc);
            }
        }
        return withLocations(copy);
    }

    public Crate withRewards(List<CrateReward> newRewards) {
        return new Crate(this.name, this.displayName, this.animationType, this.key, this.keyCustomModelData, this.locations, newRewards, this.hologramLines, this.hologramHeight, this.hologramEnabled);
    }

    private static boolean multiLocation(Location a, Location b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.getWorld() == null || b.getWorld() == null) {
            return false;
        }
        return a.getWorld().getUID().equals(b.getWorld().getUID())
            && a.getBlockX() == b.getBlockX()
            && a.getBlockY() == b.getBlockY()
            && a.getBlockZ() == b.getBlockZ();
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
