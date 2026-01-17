package me.darkness.crates.command.admin.subcmd;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.ICommand;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.crate.key.KeyService;
import me.darkness.crates.util.ItemBuilder;
import me.darkness.crates.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public final class GiveCommand implements ICommand {

    private final CratesPlugin plugin;
    private final CrateService crateService;

    public GiveCommand(CratesPlugin plugin, CrateService crateService) {
        this.plugin = plugin;
        this.crateService = crateService;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length < 3) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
                this.plugin.getConfigService().getLangConfig().commandUsage,
                Map.of("usage", getUsage())
            );
            return true;
        }

        String targetName = args[0];
        String crateName = args[1];

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
                this.plugin.getConfigService().getLangConfig().commandUsage,
                Map.of("usage", getUsage())
            );
            return true;
        }

        Optional<Crate> crateOptional = this.crateService.getCrate(crateName);
        if (crateOptional.isEmpty()) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
                this.plugin.getConfigService().getLangConfig().crateNotFound,
                Map.of("crate", crateName)
            );
            return true;
        }

        Crate crate = crateOptional.get();
        ItemStack key = crate.getKey();
        if (key == null) {
            return true;
        }

        key = key.clone();

        if (crate.getKeyCustomModelData() != null) {
            key = ItemBuilder.of(key)
                    .customModelData(crate.getKeyCustomModelData())
                    .build();
        }

        KeyService keyService = this.plugin.getKeyServiceProvider().get();
        key = keyService.tagKey(key, crate.getName());

        if (targetName.equalsIgnoreCase("all")) {
            this.giveAll(player, crate, key, amount);
        } else {
            this.give(player, targetName, crate, key, amount);
        }

        return true;
    }

    private void give(Player sender, String targetName, Crate crate, ItemStack key, int amount) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, sender,
                this.plugin.getConfigService().getLangConfig().playerNotFound,
                Map.of("player", targetName)
            );
            return;
        }

        ItemStack keyToGive = key.clone();
        keyToGive.setAmount(amount);
        target.getInventory().addItem(keyToGive);

        TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, sender,
            this.plugin.getConfigService().getLangConfig().keyGiven,
            Map.of(
                "amount", String.valueOf(amount),
                "crate", crate.getDisplayName(),
                "player", target.getName()
            )
        );

        TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, target,
            this.plugin.getConfigService().getLangConfig().keyReceived,
            Map.of(
                "amount", String.valueOf(amount),
                "crate", crate.getDisplayName()
            )
        );
    }

    private void giveAll(Player sender, Crate crate, ItemStack key, int amount) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack keyToGive = key.clone();
            keyToGive.setAmount(amount);
            player.getInventory().addItem(keyToGive);

            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
                this.plugin.getConfigService().getLangConfig().keyReceived,
                Map.of(
                    "amount", String.valueOf(amount),
                    "crate", crate.getDisplayName()
                )
            );
        }

        TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, sender,
            this.plugin.getConfigService().getLangConfig().keyGivenAll,
            Map.of(
                "amount", String.valueOf(amount),
                "crate", crate.getDisplayName()
            )
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("all");
            suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList());
            return suggestions;
        }

        if (args.length == 2) {
            return this.crateService.getAllCrates().stream()
                .map(Crate::getName)
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 3) {
            return Arrays.asList("1", "2", "3", "4", "5");
        }

        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getUsage() {
        return "/admincrate give <gracz|all> <skrzynka> <ilosc>";
    }

    @Override
    public String getPermission() {
        return "777crates.admin";
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canExecuteFromConsole() {
        return true;
    }
}
