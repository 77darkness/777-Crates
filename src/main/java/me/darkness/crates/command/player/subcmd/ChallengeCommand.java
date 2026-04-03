package me.darkness.crates.command.player.subcmd;

import dev.darkness.utilities.text.TextUtil;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.SubCommand;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.inv.SelectInv;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class ChallengeCommand extends SubCommand {

    private final BattleService service;

    public ChallengeCommand(CratesPlugin plugin, BattleService service) {
        super(plugin);
        this.service = service;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            TextUtil.send(sender, "&cTa komenda może byc uzyta tylko przez gracza!");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            lang().playerNotFound.send(sender, Map.of("player", args[0]));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            lang().battleCantChallengeSelf.send(sender);
            return true;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> new SelectInv(plugin, service).open(player, target));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length != 1) return List.of();
        String input = args[0].toLowerCase(Locale.ROOT);
        UUID selfId = sender instanceof Player p ? p.getUniqueId() : null;
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> selfId == null || !p.getUniqueId().equals(selfId))
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input))
                .toList();
    }

    @Override public String getName() { return "wyzwij"; }
    @Override public String getUsage() { return "/bitwa wyzwij <gracz>"; }
    @Override public String getPermission() { return null; }
    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }
}
