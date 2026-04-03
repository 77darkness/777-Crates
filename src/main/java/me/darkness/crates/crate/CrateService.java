package me.darkness.crates.crate;

import me.darkness.crates.util.LocationUtil;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CrateService {

    private final Map<String, Crate> crates = new HashMap<>();
    private final Map<Location, Crate> cratesByLocation = new HashMap<>();

    public void registerCrate(Crate crate) {
        this.crates.put(crate.getName().toLowerCase(), crate);
        rebuildLocationIndex(crate);
    }

    public void unregisterCrate(String name) {
        Crate crate = this.crates.remove(name.toLowerCase());
        if (crate != null) crate.getLocations().forEach(loc -> cratesByLocation.remove(normalize(loc)));
    }

    public void addCrateLocation(String name, Location location) {
        updateCrateLocations(name, location, true);
    }

    public void removeCrateLocation(String name, Location location) {
        updateCrateLocations(name, location, false);
    }

    public Optional<Crate> getCrate(String name) {
        return Optional.ofNullable(this.crates.get(name.toLowerCase()));
    }

    public Optional<Crate> getCrateByLocation(Location location) {
        return Optional.ofNullable(this.cratesByLocation.get(normalize(location)));
    }

    public Collection<Crate> getAllCrates() {
        return Collections.unmodifiableCollection(this.crates.values());
    }

    public boolean exists(String name) {
        return this.crates.containsKey(name.toLowerCase());
    }

    public void clear() {
        this.crates.clear();
        this.cratesByLocation.clear();
    }

    public static boolean multiLocation(Location a, Location b) {
        return LocationUtil.isSameBlock(a, b);
    }

    private void updateCrateLocations(String name, Location location, boolean add) {
        if (location == null) return;
        Crate old = this.crates.get(name.toLowerCase());
        if (old == null) return;

        Crate updated = add ? old.withAddedLocation(location) : old.withRemovedLocation(location);
        this.crates.put(name.toLowerCase(), updated);

        old.getLocations().forEach(loc -> cratesByLocation.remove(normalize(loc)));
        rebuildLocationIndex(updated);
    }

    private void rebuildLocationIndex(Crate crate) {
        crate.getLocations().forEach(loc -> {
            Location key = normalize(loc);
            if (key != null) cratesByLocation.put(key, crate);
        });
    }

    private Location normalize(Location location) {
        if (location == null || location.getWorld() == null) return null;
        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
