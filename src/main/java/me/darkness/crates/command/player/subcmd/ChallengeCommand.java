package me.darkness.crates.command.player.subcmd;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.ICommand;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.inv.SelectInv;
import me.darkness.crates.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class ChallengeCommand implements ICommand {

    private final CratesPlugin plugin;
    private final BattleService service;

    public ChallengeCommand(CratesPlugin plugin, BattleService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.color("&cTa komenda może byc uzyta tylko przez gracza!"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, sender,
                    this.plugin.getConfigService().getLangConfig().playerNotFound,
                    Map.of("player", args[0])
            );
            return true;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, sender,
                    this.plugin.getConfigService().getLangConfig().battleCantChallengeSelf
            );
            return true;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> new SelectInv(plugin, service).open(player, target));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return null;
        }
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "wyzwij";
    }

    @Override
    public String getUsage() {
        return "/bitwa wyzwij <gracz>";
    }

    @Override
    public String getPermission() {
        return null;
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
