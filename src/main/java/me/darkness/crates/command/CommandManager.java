package me.darkness.crates.command;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.admin.AdminCrateCommand;
import me.darkness.crates.command.player.BattleCommand;
import me.darkness.crates.configuration.ConfigService;
import me.darkness.crates.crate.CrateLoader;
import me.darkness.crates.crate.CrateService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class CommandManager implements CommandExecutor, TabCompleter {

    private final Map<String, BaseCommand> commands = new HashMap<>();

    public CommandManager(
            final CratesPlugin plugin,
            final CrateService crateService,
            final CrateLoader crateLoader,
            final ConfigService configService
    ) {
        this.register(plugin, "admincrate", new AdminCrateCommand(plugin, crateService, crateLoader, configService));
        this.register(plugin, "battle", new BattleCommand(plugin, plugin.getBattleService()));
    }

    private void register(final CratesPlugin plugin, final String label, final BaseCommand command) {
        this.commands.put(label.toLowerCase(Locale.ROOT), command);

        final PluginCommand pluginCommand = plugin.getCommand(label);
        if (pluginCommand == null) {
            throw new IllegalStateException("Komenda '" + label + "' nie jest dodana do plugin.yml");
        }

        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(
            final @NotNull CommandSender sender,
            final @NotNull Command command,
            final @NotNull String label,
            final @NotNull String[] args
    ) {
        final BaseCommand baseCommand = this.getBaseCommand(command);
        if (baseCommand == null) {
            return false;
        }

        return baseCommand.onCommand(sender, args);
    }

    @Override
    public List<String> onTabComplete(
            final @NotNull CommandSender sender,
            final @NotNull Command command,
            final @NotNull String alias,
            final @NotNull String[] args
    ) {
        final BaseCommand baseCommand = this.getBaseCommand(command);
        if (baseCommand == null) {
            return Collections.emptyList();
        }

        return baseCommand.onTabComplete(sender, args);
    }

    private BaseCommand getBaseCommand(final Command command) {
        return this.commands.get(command.getName().toLowerCase(Locale.ROOT));
    }
}
