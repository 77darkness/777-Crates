package me.darkness.crates.command.admin.subcmd;

import dev.darkness.utilities.text.TextUtil;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.SubCommand;
import me.darkness.crates.configuration.Inv.SelectInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateLoader;
import me.darkness.crates.crate.CrateService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class CreateCommand extends SubCommand {

    private final CrateService crateService;
    private final CrateLoader crateLoader;

    public CreateCommand(CratesPlugin plugin, CrateService crateService, CrateLoader crateLoader) {
        super(plugin);
        this.crateService = crateService;
        this.crateLoader = crateLoader;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            TextUtil.send(sender, "&cTa komenda może byc uzyta tylko przez gracza!");
            return true;
        }

        if (args.length < 1) {
            lang().commandUsage.send(player, Map.of("usage", getUsage()));
            return true;
        }

        String crateName = args[0];

        if (this.crateService.exists(crateName)) {
            lang().crateAlreadyExists.send(player, Map.of("crate", crateName));
            return true;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            lang().notLookingAtBlock.send(player);
            return true;
        }

        Location location = targetBlock.getLocation();

        Crate crate = this.crateLoader.createDefaultCrate(crateName, location);
        this.crateService.registerCrate(crate);

        SelectInvConfig selectInvConfig = this.plugin.getConfigService().getBattleCrateSelectInv();
        int nextSlot = selectInvConfig.crates.size();
        SelectInvConfig.CrateItem crateItem = new SelectInvConfig.CrateItem(
                "&#00FF00{crate}",
                nextSlot,
                "CHEST",
                List.of("", " &8* &fᴋʟɪᴋɴɪᴊ &#00FF00ᴘᴘᴍ&f, ᴀʙʏ ᴡʏʙʀᴀᴄ")
        );
        selectInvConfig.crates.put(crateName, crateItem);
        selectInvConfig.save();

        if (this.plugin.getHologramHook().isEnabled()) {
            this.plugin.getHologramHook().createHologram(crate);
        }

        lang().crateCreated.send(player, Map.of("crate", crateName));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("<nazwa>");
        }
        return super.tabComplete(sender, args);
    }

    @Override public String getName() { return "create"; }
    @Override public String getUsage() { return "/admincrate create <nazwa>"; }
    @Override public String getPermission() { return "777crates.admin"; }
    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }
}
