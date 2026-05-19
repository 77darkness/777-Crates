package me.darkness.crates.hooks;

import dev.darkness.utilities.misc.Logger;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.Crate;
import org.bukkit.Location;

import java.util.*;

public final class HologramHook {

    private final boolean enabled;
    private final Map<String, Hologram> holograms;

    public HologramHook(CratesPlugin plugin) {
        Logger logger = new Logger(plugin);
        enabled = plugin.getServer().getPluginManager().getPlugin("DecentHolograms") != null;
        holograms = new HashMap<>();

        if (enabled) {
            logger.success("&fZaładowano hologramy. &8(&aDecentHolograms&8)");
        } else {
            logger.error("&cNie znaleziono pluginu &4DecentHolograms! &cHologramy zostały wyłączone!");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void createHolograms(Collection<Crate> crates) {
        if (!enabled) {
            return;
        }

        for (Crate crate : crates) {
            if (!crate.getLocations().isEmpty()) {
                createHologram(crate);
            }
        }
    }

    public void createHologram(Crate crate) {
        if (!enabled) {
            return;
        }

        boolean enabledForCrate = crate.isHologramEnabled();

        if (!enabledForCrate) {
            removeHologram(crate.getName());
            return;
        }

        List<Location> crateLocations = crate.getLocations();
        if (crateLocations.isEmpty()) {
            removeHologram(crate.getName());
            return;
        }

        List<String> sourceLines = crate.getHologramLines();
        if (sourceLines.isEmpty()) {
            removeHologram(crate.getName());
            return;
        }

        removeHologram(crate.getName());

        List<String> lines = new ArrayList<>();
        for (String line : sourceLines) {
            String processedLine = (line == null ? "" : line)
                    .replace("{crate}", crate.getDisplayName())
                    .replace('&', '§');
            lines.add(processedLine);
        }

        double height = crate.getHologramHeight();

        int index = 0;
        for (Location base : crateLocations) {
            if (base == null) {
                index++;
                continue;
            }

            Location location = base.clone();
            location.add(0.5, height, 0.5);

            String hologramId = "777crate_" + crate.getName().toLowerCase(Locale.ROOT) + "_" + index;
            Hologram hologram = DHAPI.createHologram(hologramId, location, lines);

            holograms.put(mapKey(crate.getName(), index), hologram);
            index++;
        }
    }

    public void removeHologram(String crateName) {
        if (!enabled) {
            return;
        }

        String prefix = crateName.toLowerCase(Locale.ROOT) + "#";
        List<String> toRemove = new ArrayList<>();

        for (String key : holograms.keySet()) {
            if (key.startsWith(prefix)) {
                toRemove.add(key);
            }
        }

        for (String key : toRemove) {
            Hologram hologram = holograms.remove(key);
            if (hologram != null) {
                hologram.delete();
            }
        }
    }

    public void removeAll() {
        if (!enabled) {
            return;
        }

        for (Hologram hologram : holograms.values()) {
            hologram.delete();
        }
        holograms.clear();
    }

    private String mapKey(String crateName, int index) {
        return crateName.toLowerCase(Locale.ROOT) + "#" + index;
    }
}
