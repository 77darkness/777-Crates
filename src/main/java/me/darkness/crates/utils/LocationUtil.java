package me.darkness.crates.utils;

import org.bukkit.Location;

public final class LocationUtil {

    private LocationUtil() {}

    public static boolean isSameBlock(Location a, Location b) {
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
}

