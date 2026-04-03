package me.darkness.crates.command.admin.subcmd;

import dev.darkness.utilities.text.TextUtil;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.SubCommand;
import org.bukkit.command.CommandSender;

public final class HelpCommand extends SubCommand {

    public HelpCommand(CratesPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        lang().adminHelp.forEach(line -> TextUtil.send(sender, line));
        return true;
    }

    @Override public String getName() { return "help"; }
    @Override public String getUsage() { return "/admincrate help"; }
    @Override public String getPermission() { return "777crates.admin"; }
    @Override public int getMinArgs() { return 0; }
    @Override public int getMaxArgs() { return 0; }
    @Override public boolean canExecuteFromConsole() { return true; }
}
