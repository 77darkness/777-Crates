package me.darkness.crates.crate.key;

import org.bukkit.plugin.Plugin;

public final class KeyServiceProvider {

    private final KeyService keyService;

    public KeyServiceProvider(Plugin plugin) {
        this.keyService = new KeyService(plugin);
    }

    public KeyService get() {
        return this.keyService;
    }
}
