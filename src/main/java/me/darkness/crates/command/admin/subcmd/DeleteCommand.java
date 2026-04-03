package me.darkness.crates.command.admin.subcmd;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.SubCommand;
import me.darkness.crates.configuration.Inv.SelectInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateLoader;
import me.darkness.crates.crate.CrateService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class DeleteCommand extends SubCommand {

    private final CrateService crateService;
    private final CrateLoader crateLoader;

    public DeleteCommand(CratesPlugin plugin, CrateService crateService, CrateLoader crateLoader) {
        super(plugin);
        this.crateService = crateService;
        this.crateLoader = crateLoader;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length < 1) {
            lang().commandUsage.send(player, Map.of("usage", getUsage()));
            return true;
        }

        String crateName = args[0];

        if (!this.crateService.exists(crateName)) {
            lang().crateNotFound.send(player, Map.of("crate", crateName));
            return true;
        }

        this.plugin.getHologramHook().removeHologram(crateName);
        this.crateService.unregisterCrate(crateName);
        this.crateLoader.deleteCrate(crateName);

        SelectInvConfig selectInvConfig = this.plugin.getConfigService().getBattleCrateSelectInv();
        selectInvConfig.crates.remove(crateName);
        selectInvConfig.save();

        lang().crateDeleted.send(player, Map.of("crate", crateName));

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
        return super.tabComplete(sender, args);
    }

    @Override public String getName() { return "delete"; }
    @Override public String getUsage() { return "/admincrate delete <skrzynka>"; }
    @Override public String getPermission() { return "777crates.admin"; }
    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }
}
