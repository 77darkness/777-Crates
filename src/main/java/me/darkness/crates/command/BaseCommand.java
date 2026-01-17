package me.darkness.crates.command;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class BaseCommand implements ICommand {

    protected final CratesPlugin plugin;
    protected final Map<String, ICommand> subCommands;

    protected BaseCommand(final CratesPlugin plugin) {
        this.plugin = plugin;
        this.subCommands = new HashMap<>();
    }

    protected void registerSubCommand(final ICommand command) {
        this.subCommands.put(command.getName().toLowerCase(Locale.ROOT), command);
    }

    public boolean onCommand(final CommandSender sender, final String[] args) {
        if (args.length == 0) {
            return this.execute(sender, args);
        }

        final String subCommandLabel = args[0].toLowerCase(Locale.ROOT);
        final ICommand subCommand = this.subCommands.get(subCommandLabel);

        if (subCommand == null) {
            return this.execute(sender, args);
        }

        if (!this.canExecute(sender, subCommand)) {
            return true;
        }

        final String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);

        if (!this.validateArguments(sender, subCommand, remainingArgs)) {
            return true;
        }

        return subCommand.execute(sender, remainingArgs);
    }

    public List<String> onTabComplete(final CommandSender sender, final String[] args) {
        if (args.length <= 1) {
            return this.completeSubCommands(sender, args);
        }

        final ICommand subCommand = this.subCommands.get(args[0].toLowerCase(Locale.ROOT));
        if (subCommand == null) {
            return Collections.emptyList();
        }

        if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
            return Collections.emptyList();
        }

        final String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);
        return subCommand.tabComplete(sender, remainingArgs);
    }

    @Override
    public final List<String> tabComplete(final CommandSender sender, final String[] args) {
        return this.onTabComplete(sender, args);
    }

    private boolean canExecute(final CommandSender sender, final ICommand command) {
        if (!(sender instanceof Player) && !command.canExecuteFromConsole()) {
            return false;
        }

        if (command.getPermission() != null && !sender.hasPermission(command.getPermission())) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, sender,
                this.plugin.getConfigService().getLangConfig().noPermission
            );
            return false;
        }

        return true;
    }

    private boolean validateArguments(final CommandSender sender, final ICommand command, final String[] args) {
        final int length = args.length;

        if (length < command.getMinArgs()) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, sender,
                this.plugin.getConfigService().getLangConfig().commandUsage,
                Map.of("usage", command.getUsage())
            );
            return false;
        }

        if (command.getMaxArgs() != -1 && length > command.getMaxArgs()) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, sender,
                this.plugin.getConfigService().getLangConfig().commandUsage,
                Map.of("usage", command.getUsage())
            );
            return false;
        }

        return true;
    }

    private List<String> completeSubCommands(final CommandSender sender, final String[] args) {
        final String input = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        final List<String> completions = new ArrayList<>();

        for (ICommand command : this.subCommands.values()) {
            if (command.getPermission() != null && !sender.hasPermission(command.getPermission())) {
                continue;
            }

            if (command.getName().toLowerCase(Locale.ROOT).startsWith(input)) {
                completions.add(command.getName());
            }
        }

        return completions;
    }
}
