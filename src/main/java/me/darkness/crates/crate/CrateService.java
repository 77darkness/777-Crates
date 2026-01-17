package me.darkness.crates.crate;

import org.bukkit.Location;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CrateService {

    private final Map<String, Crate> crates;
    private final Map<Location, Crate> cratesByLocation;

    public CrateService() {
        this.crates = new HashMap<>();
        this.cratesByLocation = new HashMap<>();
    }

    public void registerCrate(Crate crate) {
        this.crates.put(crate.getName().toLowerCase(), crate);

        for (Location location : crate.getLocations()) {
            Location key = normalize(location);
            if (key != null) {
                this.cratesByLocation.put(key, crate);
            }
        }
    }

    public void unregisterCrate(String name) {
        Crate crate = this.crates.remove(name.toLowerCase());

        if (crate != null) {
            for (Location location : crate.getLocations()) {
                Location key = normalize(location);
                if (key != null) {
                    this.cratesByLocation.remove(key);
                }
            }
        }
    }

    public void addCrateLocation(String name, Location location) {
        if (location == null) {
            return;
        }

        Crate oldCrate = this.crates.get(name.toLowerCase());
        if (oldCrate == null) {
            return;
        }

        Crate newCrate = oldCrate.withAddedLocation(location);
        this.crates.put(name.toLowerCase(), newCrate);

        for (Location oldLoc : oldCrate.getLocations()) {
            Location key = normalize(oldLoc);
            if (key != null) {
                this.cratesByLocation.remove(key);
            }
        }
        for (Location newLoc : newCrate.getLocations()) {
            Location key = normalize(newLoc);
            if (key != null) {
                this.cratesByLocation.put(key, newCrate);
            }
        }
    }

    public void removeCrateLocation(String name, Location location) {
        if (location == null) {
            return;
        }

        Crate oldCrate = this.crates.get(name.toLowerCase());
        if (oldCrate == null) {
            return;
        }

        Crate newCrate = oldCrate.withRemovedLocation(location);
        this.crates.put(name.toLowerCase(), newCrate);

        for (Location oldLoc : oldCrate.getLocations()) {
            Location key = normalize(oldLoc);
            if (key != null) {
                this.cratesByLocation.remove(key);
            }
        }
        for (Location newLoc : newCrate.getLocations()) {
            Location key = normalize(newLoc);
            if (key != null) {
                this.cratesByLocation.put(key, newCrate);
            }
        }
    }

    public static boolean multiLocation(Location a, Location b) {
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


    public Optional<Crate> getCrate(String name) {
        return Optional.ofNullable(this.crates.get(name.toLowerCase()));
    }

    public Optional<Crate> getCrateByLocation(Location location) {
        return Optional.ofNullable(this.cratesByLocation.get(normalize(location)));
    }

    public Collection<Crate> getAllCrates() {
        return this.crates.values();
    }

    public boolean exists(String name) {
        return this.crates.containsKey(name.toLowerCase());
    }

    public void clear() {
        this.crates.clear();
        this.cratesByLocation.clear();
    }

    private Location normalize(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
