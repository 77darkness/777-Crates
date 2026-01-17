package me.darkness.crates.command.admin.subcmd;

import me.darkness.crates.command.ICommand;
import me.darkness.crates.configuration.ConfigService;
import me.darkness.crates.util.TextUtil;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class HelpCommand implements ICommand {

    private final ConfigService configService;

    public HelpCommand(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        for (String line : this.configService.getLangConfig().adminHelp) {
            sender.sendMessage(TextUtil.color(line));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getUsage() {
        return "/admincrate help";
    }

    @Override
    public String getPermission() {
        return "777crates.admin";
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public int getMaxArgs() {
        return 0;
    }

    @Override
    public boolean canExecuteFromConsole() {
        return true;
    }
}
