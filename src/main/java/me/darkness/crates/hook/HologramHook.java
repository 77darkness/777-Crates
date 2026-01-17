package me.darkness.crates.hook;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.util.TextUtil;
import org.bukkit.Location;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public final class HologramHook {

    private final boolean enabled;
    private final Map<String, Hologram> holograms;

    public HologramHook(CratesPlugin plugin) {
        this.enabled = plugin.getServer().getPluginManager().getPlugin("DecentHolograms") != null;
        this.holograms = new HashMap<>();

        if (this.enabled) {
            getServer().getConsoleSender().sendMessage("§8[§a§l777-Crates§8] §fZaładowano hologramy§r. §8(§aDecentHolograms§8)");
        } else {
            getServer().getConsoleSender().sendMessage("§8[§4§l777-Crates§8] §cNie znaleziono pluginu §44DecentHolograms! §cHologramy zostały wyłączone!");
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void createHolograms(Collection<Crate> crates) {
        if (!this.enabled) {
            return;
        }

        for (Crate crate : crates) {
            if (!crate.getLocations().isEmpty()) {
                this.createHologram(crate);
            }
        }
    }

    public void createHologram(Crate crate) {
        if (!this.enabled) {
            return;
        }

        boolean enabledForCrate = crate.isHologramEnabled() == null || crate.isHologramEnabled();

        if (!enabledForCrate) {
            this.removeHologram(crate.getName());
            return;
        }

        List<Location> crateLocations = crate.getLocations();
        if (crateLocations.isEmpty()) {
            this.removeHologram(crate.getName());
            return;
        }

        List<String> sourceLines = crate.getHologramLines();
        if (sourceLines.isEmpty()) {
            this.removeHologram(crate.getName());
            return;
        }

        this.removeHologram(crate.getName());

        List<String> lines = new ArrayList<>();
        for (String line : sourceLines) {
            String processedLine = (line == null ? "" : line)
                .replace("{crate}", crate.getDisplayName());
            lines.add(TextUtil.color(processedLine));
        }

        double height = crate.getHologramHeight() != null ? crate.getHologramHeight() : 2.1;

        int index = 0;
        for (Location base : crateLocations) {
            if (base == null) {
                index++;
                continue;
            }

            Location location = base.clone();
            location.add(0.5, height, 0.5);

            String hologramId = "777case_" + crate.getName().toLowerCase(Locale.ROOT) + "_" + index;
            Hologram hologram = DHAPI.createHologram(hologramId, location, lines);

            this.holograms.put(mapKey(crate.getName(), index), hologram);
            index++;
        }
    }

    public void removeHologram(String crateName) {
        if (!this.enabled) {
            return;
        }

        String prefix = crateName.toLowerCase(Locale.ROOT) + "#";
        List<String> toRemove = new ArrayList<>();

        for (String key : this.holograms.keySet()) {
            if (key.startsWith(prefix)) {
                toRemove.add(key);
            }
        }

        for (String key : toRemove) {
            Hologram hologram = this.holograms.remove(key);
            if (hologram != null) {
                hologram.delete();
            }
        }
    }

    public void removeAll() {
        if (!this.enabled) {
            return;
        }

        for (Hologram hologram : this.holograms.values()) {
            hologram.delete();
        }
        this.holograms.clear();
    }

    private String mapKey(String crateName, int index) {
        return crateName.toLowerCase(Locale.ROOT) + "#" + index;
    }
}
