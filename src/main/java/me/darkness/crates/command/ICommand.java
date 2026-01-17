package me.darkness.crates.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface ICommand {

    boolean execute(CommandSender sender, String[] args);

    List<String> tabComplete(CommandSender sender, String[] args);

    String getName();

    String getUsage();

    String getPermission();

    int getMinArgs();

    int getMaxArgs();

    default boolean canExecuteFromConsole() {
        return false;
    }
}