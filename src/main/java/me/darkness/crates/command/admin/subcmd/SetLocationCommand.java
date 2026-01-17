package me.darkness.crates.command.admin.subcmd;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.ICommand;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateLoader;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class SetLocationCommand implements ICommand {

    private final CratesPlugin plugin;
    private final CrateService crateService;
    private final CrateLoader crateLoader;

    public SetLocationCommand(CratesPlugin plugin, CrateService crateService, CrateLoader crateLoader) {
        this.plugin = plugin;
        this.crateService = crateService;
        this.crateLoader = crateLoader;
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

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
                this.plugin.getConfigService().getLangConfig().notLookingAtBlock
            );
            return true;
        }

        Location location = targetBlock.getLocation();

        this.plugin.getHologramHook().removeHologram(crateName);
        this.crateService.addCrateLocation(crateName, location);

        Crate updatedCrate = this.crateService.getCrate(crateName).orElse(crateOptional.get());
        this.crateLoader.saveCrate(updatedCrate);

        if (this.plugin.getHologramHook().isEnabled()) {
            this.plugin.getHologramHook().createHologram(updatedCrate);
        }

        TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
            this.plugin.getConfigService().getLangConfig().crateLocationSet,
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
        return "setlocation";
    }

    @Override
    public String getUsage() {
        return "/admincrate setlocation <nazwa>";
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
