package me.darkness.crates.commands;

import dev.darkness.commands.annotation.Arg;
import dev.darkness.commands.annotation.Command;
import dev.darkness.commands.annotation.Context;
import dev.darkness.commands.annotation.Execute;
import dev.darkness.utilities.text.TextUtil;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.commands.resolvers.PlayerTargetResolver.PlayerTarget;
import me.darkness.crates.configuration.inventories.SelectInvConfig;
import me.darkness.crates.configuration.LangConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateLoader;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.inventories.EditInv;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

@Command(
        name = "admincrate",
        aliases = {"777crate", "crate"},
        permission = "777crates.admin"
)
public final class AdminCratesCommand {

    private final CratesPlugin plugin;
    private final CrateService crateService;
    private final CrateLoader crateLoader;

    public AdminCratesCommand(CratesPlugin plugin, CrateService crateService, CrateLoader crateLoader) {
        this.plugin = plugin;
        this.crateService = crateService;
        this.crateLoader = crateLoader;
    }

    private LangConfig lang() {
        return plugin.getConfigService().lang();
    }

    @Execute
    public void help(@Context CommandSender sender) {
        lang().adminHelp.forEach(line -> TextUtil.send(sender, line));
    }

    @Execute("create")
    public void create(@Context CommandSender sender, @Arg String name) {
        if (!(sender instanceof Player player)) {
            lang().onlyPlayer.send(sender);
            return;
        }

        if (crateService.exists(name)) {
            lang().crateAlreadyExists.send(player, Map.of("crate", name));
            return;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            lang().notLookingAtBlock.send(player);
            return;
        }

        Location location = targetBlock.getLocation();
        Crate crate = crateLoader.createDefaultCrate(name, location);
        crateService.registerCrate(crate);

        SelectInvConfig selectInvConfig = plugin.getConfigService().selectInv();
        int nextSlot = selectInvConfig.crates.size();
        SelectInvConfig.CrateItem crateItem = new SelectInvConfig.CrateItem(
                "&#00FF00{crate}", nextSlot, "CHEST",
                List.of("", " &8* &fᴋʟɪᴋɴɪᴊ &#00FF00ᴘᴘᴍ&f, ᴀʙʏ ᴡʏʙʀᴀᴄ")
        );
        selectInvConfig.crates.put(name, crateItem);
        selectInvConfig.save();

        if (plugin.getHologramHook().isEnabled()) {
            plugin.getHologramHook().createHologram(crate);
        }

        lang().crateCreated.send(player, Map.of("crate", name));
    }

    @Execute("delete")
    public void delete(@Context CommandSender sender, @Arg Crate crate) {
        if (!(sender instanceof Player player)) {
            lang().onlyPlayer.send(sender);
            return;
        }

        String name = crate.getName();
        plugin.getHologramHook().removeHologram(name);
        crateService.unregisterCrate(name);
        crateLoader.deleteCrate(name);

        SelectInvConfig selectInvConfig = plugin.getConfigService().selectInv();
        selectInvConfig.crates.remove(name);
        selectInvConfig.save();

        lang().crateDeleted.send(player, Map.of("crate", name));
    }

    @Execute("give")
    public void give(@Context CommandSender sender, @Arg PlayerTarget target, @Arg Crate crate, @Arg int amount) {
        ItemStack key = crate.getKey();
        if (key == null) return;

        key = plugin.getKeyService().tagKey(key.clone(), crate.getName());

        if (target.isAll()) {
            ItemStack finalKey = key;
            for (Player p : target.resolve()) {
                ItemStack toGive = finalKey.clone();
                toGive.setAmount(amount);
                p.getInventory().addItem(toGive);
                lang().keyReceived.send(p, Map.of("amount", String.valueOf(amount), "crate", crate.getDisplayName()));
            }
            lang().keyGivenAll.send(sender, Map.of("amount", String.valueOf(amount), "crate", crate.getDisplayName()));
        } else {
            Player targetPlayer = target.getPlayer();
            if (targetPlayer == null) {
                lang().playerNotFound.send(sender, Map.of("player", target.getRaw()));
                return;
            }
            ItemStack toGive = key.clone();
            toGive.setAmount(amount);
            targetPlayer.getInventory().addItem(toGive);
            lang().keyGiven.send(sender, Map.of("amount", String.valueOf(amount), "crate", crate.getDisplayName(), "player", targetPlayer.getName()));
            lang().keyReceived.send(targetPlayer, Map.of("amount", String.valueOf(amount), "crate", crate.getDisplayName()));
        }
    }

    @Execute("setlocation")
    public void setLocation(@Context CommandSender sender, @Arg Crate crate) {
        if (!(sender instanceof Player player)) {
            lang().onlyPlayer.send(sender);
            return;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            lang().notLookingAtBlock.send(player);
            return;
        }

        Location location = targetBlock.getLocation();
        String name = crate.getName();

        plugin.getHologramHook().removeHologram(name);
        crateService.addCrateLocation(name, location);

        Crate updated = crateService.getCrate(name).orElse(crate);
        crateLoader.saveCrate(updated);

        if (plugin.getHologramHook().isEnabled()) {
            plugin.getHologramHook().createHologram(updated);
        }

        lang().crateLocationSet.send(player, Map.of("crate", name));
    }

    @Execute("removelocation")
    public void removeLocation(@Context CommandSender sender, @Arg Crate crate) {
        if (!(sender instanceof Player player)) {
            lang().onlyPlayer.send(sender);
            return;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            lang().notLookingAtBlock.send(player);
            return;
        }

        Location location = targetBlock.getLocation();
        String name = crate.getName();

        boolean isThisCrateLocation = crate.getLocations().stream()
                .anyMatch(loc -> CrateService.multiLocation(loc, location));
        if (!isThisCrateLocation) {
            lang().notCrate.send(player, Map.of("crate", name));
            return;
        }

        plugin.getHologramHook().removeHologram(name);
        crateService.removeCrateLocation(name, location);

        Crate updated = crateService.getCrate(name).orElse(crate);
        crateLoader.saveCrate(updated);

        if (plugin.getHologramHook().isEnabled()) {
            plugin.getHologramHook().createHologram(updated);
        }

        lang().crateLocationRemoved.send(player, Map.of("crate", name));
    }

    @Execute("edit")
    public void edit(@Context CommandSender sender, @Arg Crate crate) {
        if (!(sender instanceof Player player)) {
            lang().onlyPlayer.send(sender);
            return;
        }

        new EditInv(plugin, crateService, crateLoader).open(player, crate);
    }

    @Execute("reload")
    public void reload(@Context CommandSender sender) {
        plugin.getConfigService().reload();
        plugin.getHologramHook().removeAll();
        crateService.clear();
        crateLoader.loadAll(crateService);

        if (plugin.getHologramHook().isEnabled()) {
            plugin.getHologramHook().createHolograms(crateService.getAllCrates());
        }

        lang().reloadSuccess.send(sender);
    }
}
