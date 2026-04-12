package me.darkness.crates.command.player;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.BaseCommand;
import me.darkness.crates.command.player.subcmd.AcceptCommand;
import me.darkness.crates.command.player.subcmd.ChallengeCommand;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.inv.OpenBattleInv;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BattleCommand extends BaseCommand {

    public BattleCommand(CratesPlugin plugin, BattleService battleService) {
        super(plugin);

        this.registerSubCommand(new ChallengeCommand(plugin, battleService));
        this.registerSubCommand(new AcceptCommand(plugin, battleService));
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0 && sender instanceof Player player) {
            plugin.getServer().getScheduler().runTask(plugin, () ->
                    new OpenBattleInv(plugin, plugin.getBattleService()).open(player));
        }
        return true;
    }

    @Override public String getName() { return "battle"; }
    @Override public String getUsage() { return "/bitwa [wyzwij|akceptuj]"; }
    @Override public String getPermission() { return null; }
    @Override public int getMinArgs() { return 0; }
    @Override public int getMaxArgs() { return -1; }
}
