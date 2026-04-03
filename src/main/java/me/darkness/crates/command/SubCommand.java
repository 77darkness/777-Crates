package me.darkness.crates.command;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Lang;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public abstract class SubCommand implements ICommand {

    protected final CratesPlugin plugin;

    protected SubCommand(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    protected Lang lang() {
        return this.plugin.getConfigService().getLangConfig();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

