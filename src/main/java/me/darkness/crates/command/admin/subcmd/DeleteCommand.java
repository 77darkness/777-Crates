package me.darkness.crates.command.admin.subcmd;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.ICommand;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateLoader;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.util.TextUtil;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class DeleteCommand implements ICommand {

    private final CratesPlugin plugin;
    private final CrateService crateService;
    private final CrateLoader crateLoader;

    public DeleteCommand(CratesPlugin plugin, CrateService crateService, CrateLoader crateLoader) {
        this.plugin = plugin;
        this.crateService = crateService;
        this.crateLoader = crateLoader;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof org.bukkit.entity.Player player)) {
            return true;
        }

        if (args.length < 1) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
                this.plugin.getConfigService().getLangConfig().commandUsage,
                java.util.Map.of("usage", getUsage())
            );
            return true;
        }

        String crateName = args[0];

        if (!this.crateService.exists(crateName)) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
                this.plugin.getConfigService().getLangConfig().crateNotFound,
                java.util.Map.of("crate", crateName)
            );
            return true;
        }

        this.plugin.getHologramHook().removeHologram(crateName);
        this.crateService.unregisterCrate(crateName);
        this.crateLoader.deleteCrate(crateName);

        TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
            this.plugin.getConfigService().getLangConfig().crateDeleted,
            java.util.Map.of("crate", crateName)
        );

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.crateService.getAllCrates().stream()
                .map(Crate::getName)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getUsage() {
        return "/admincrate delete <nazwa>";
    }

    @Override
    public String getPermission() {
        return "777crates.admin";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public boolean canExecuteFromConsole() {
        return true;
    }
}
