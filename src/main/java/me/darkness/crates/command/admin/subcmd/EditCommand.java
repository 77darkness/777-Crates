package me.darkness.crates.command.admin.subcmd;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.SubCommand;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.inv.EditInv;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class EditCommand extends SubCommand {

    private final CrateService crateService;

    public EditCommand(CratesPlugin plugin, CrateService crateService) {
        super(plugin);
        this.crateService = crateService;
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

        Optional<Crate> crateOptional = this.crateService.getCrate(args[0]);
        if (crateOptional.isEmpty()) {
            lang().crateNotFound.send(player, Map.of("crate", args[0]));
            return true;
        }

        new EditInv(this.plugin, this.crateService, this.plugin.getCrateLoader()).open(player, crateOptional.get());
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

    @Override public String getName() { return "edit"; }
    @Override public String getUsage() { return "/admincase edit <nazwa>"; }
    @Override public String getPermission() { return "777crates.admin"; }
    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }
}
