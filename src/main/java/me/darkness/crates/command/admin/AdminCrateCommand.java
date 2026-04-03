package me.darkness.crates.command.admin;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.BaseCommand;
import me.darkness.crates.command.admin.subcmd.*;
import me.darkness.crates.crate.CrateLoader;
import me.darkness.crates.crate.CrateService;
import org.bukkit.command.CommandSender;

public final class AdminCrateCommand extends BaseCommand {

    public AdminCrateCommand(CratesPlugin plugin, CrateService crateService, CrateLoader crateLoader) {
        super(plugin);

        this.registerSubCommand(new CreateCommand(plugin, crateService, crateLoader));
        this.registerSubCommand(new DeleteCommand(plugin, crateService, crateLoader));
        this.registerSubCommand(new GiveCommand(plugin, crateService));
        this.registerSubCommand(new SetLocationCommand(plugin, crateService, crateLoader));
        this.registerSubCommand(new RemoveLocationCommand(plugin, crateService, crateLoader));
        this.registerSubCommand(new EditCommand(plugin, crateService));
        this.registerSubCommand(new ReloadCommand(plugin, plugin.getConfigService(), crateService, crateLoader));
        this.registerSubCommand(new HelpCommand(plugin));
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            this.plugin.getConfigService().getLangConfig().noPermission.send(sender);
            return true;
        }

        return this.subCommands.get("help").execute(sender, args);
    }

    @Override
    public String getName() {
        return "admincrate";
    }

    @Override
    public String getUsage() {
        return "/admincrate <komenda>";
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
        return -1;
    }
}
