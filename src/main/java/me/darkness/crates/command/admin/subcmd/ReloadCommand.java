package me.darkness.crates.command.admin.subcmd;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.SubCommand;
import me.darkness.crates.configuration.ConfigService;
import me.darkness.crates.crate.CrateLoader;
import me.darkness.crates.crate.CrateService;
import org.bukkit.command.CommandSender;

public final class ReloadCommand extends SubCommand {

    private final ConfigService configService;
    private final CrateService crateService;
    private final CrateLoader crateLoader;

    public ReloadCommand(CratesPlugin plugin, ConfigService configService, CrateService crateService, CrateLoader crateLoader) {
        super(plugin);
        this.configService = configService;
        this.crateService = crateService;
        this.crateLoader = crateLoader;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        this.configService.reload();
        this.plugin.getHologramHook().removeAll();
        this.crateService.clear();
        this.crateLoader.loadAll(this.crateService);

        if (this.plugin.getHologramHook().isEnabled()) {
            this.plugin.getHologramHook().createHolograms(this.crateService.getAllCrates());
        }

        lang().reloadSuccess.send(sender);
        return true;
    }

    @Override public String getName() { return "reload"; }
    @Override public String getUsage() { return "/admincrate reload"; }
    @Override public String getPermission() { return "777crates.admin"; }
    @Override public int getMinArgs() { return 0; }
    @Override public int getMaxArgs() { return 0; }
    @Override public boolean canExecuteFromConsole() { return true; }
}
