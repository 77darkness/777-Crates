package me.darkness.crates.command.admin.subcmd;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.ICommand;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.inv.EditInv;
import me.darkness.crates.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class EditCommand implements ICommand {

    private final CratesPlugin plugin;
    private final CrateService crateService;
    private final me.darkness.crates.crate.CrateLoader crateLoader;

    public EditCommand(CratesPlugin plugin, CrateService crateService) {
        this.plugin = plugin;
        this.crateService = crateService;
        this.crateLoader = plugin.getCrateLoader();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
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
        Optional<Crate> crateOptional = this.crateService.getCrate(crateName);

        if (crateOptional.isEmpty()) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
                this.plugin.getConfigService().getLangConfig().crateNotFound,
                java.util.Map.of("crate", crateName)
            );
            return true;
        }

        Crate crate = crateOptional.get();
        new EditInv(this.plugin, this.crateService, this.crateLoader).open(player, crate);
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
        return "edit";
    }

    @Override
    public String getUsage() {
        return "/admincase edit <nazwa>";
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
}
