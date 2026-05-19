package me.darkness.crates.crate;

import me.darkness.crates.utils.LocationUtil;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CrateService {

    private record LocKey(UUID world, int x, int y, int z) {}

    private final Map<String, Crate> crates = new ConcurrentHashMap<>();
    private final Map<LocKey, Crate> byLoc = new ConcurrentHashMap<>();

    public void registerCrate(Crate crate) {
        crates.put(crate.getName().toLowerCase(), crate);
        indexLocs(crate);
    }

    public void unregisterCrate(String name) {
        Crate crate = crates.remove(name.toLowerCase());
        if (crate != null) crate.getLocations().forEach(loc -> byLoc.remove(toKey(loc)));
    }

    public void addCrateLocation(String name, Location location) {
        Crate crate = crates.get(name.toLowerCase());
        if (crate == null || location == null) return;
        crate.getLocations().forEach(loc -> byLoc.remove(toKey(loc)));
        crate.addLocation(location);
        indexLocs(crate);
    }

    public void removeCrateLocation(String name, Location location) {
        Crate crate = crates.get(name.toLowerCase());
        if (crate == null || location == null) return;
        crate.getLocations().forEach(loc -> byLoc.remove(toKey(loc)));
        crate.removeLocation(location);
        indexLocs(crate);
    }

    public Optional<Crate> getCrate(String name) {
        return Optional.ofNullable(crates.get(name.toLowerCase()));
    }

    public Optional<Crate> getCrateByLocation(Location location) {
        LocKey key = toKey(location);
        return key == null ? Optional.empty() : Optional.ofNullable(byLoc.get(key));
    }

    public Collection<Crate> getAllCrates() {
        return Collections.unmodifiableCollection(crates.values());
    }

    public boolean exists(String name) {
        return crates.containsKey(name.toLowerCase());
    }

    public void updateCrate(Crate crate) {
        Crate old = crates.get(crate.getName().toLowerCase());
        if (old != null) old.getLocations().forEach(loc -> byLoc.remove(toKey(loc)));
        crates.put(crate.getName().toLowerCase(), crate);
        indexLocs(crate);
    }

    public void clear() {
        crates.clear();
        byLoc.clear();
    }

    public static boolean multiLocation(Location a, Location b) {
        return LocationUtil.isSameBlock(a, b);
    }

    private void indexLocs(Crate crate) {
        for (Location loc : crate.getLocations()) {
            LocKey key = toKey(loc);
            if (key != null) byLoc.put(key, crate);
        }
    }

    private LocKey toKey(Location location) {
        if (location == null || location.getWorld() == null) return null;
        return new LocKey(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
