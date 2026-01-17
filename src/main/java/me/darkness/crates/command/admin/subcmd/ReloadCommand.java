package me.darkness.crates.command.admin.subcmd;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.ICommand;
import me.darkness.crates.configuration.ConfigService;
import me.darkness.crates.crate.CrateLoader;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.util.TextUtil;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class ReloadCommand implements ICommand {

    private final CratesPlugin plugin;
    private final ConfigService configService;
    private final CrateService crateService;
    private final CrateLoader crateLoader;

    public ReloadCommand(CratesPlugin plugin, ConfigService configService, CrateService crateService, CrateLoader crateLoader) {
        this.plugin = plugin;
        this.configService = configService;
        this.crateService = crateService;
        this.crateLoader = crateLoader;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof org.bukkit.entity.Player player)) {
            return true;
        }

        this.configService.reload();
        this.plugin.getHologramHook().removeAll();
        this.crateService.clear();
        this.crateLoader.loadAll(this.crateService);

        if (this.plugin.getHologramHook().isEnabled()) {
            this.plugin.getHologramHook().createHolograms(this.crateService.getAllCrates());
        }

        TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
            this.plugin.getConfigService().getLangConfig().reloadSuccess
        );

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getUsage() {
        return "/admincrate reload";
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
