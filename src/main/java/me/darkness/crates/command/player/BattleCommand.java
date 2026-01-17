package me.darkness.crates.command.player;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.command.BaseCommand;
import me.darkness.crates.command.player.subcmd.AcceptCommand;
import me.darkness.crates.command.player.subcmd.ChallengeCommand;

public final class BattleCommand extends BaseCommand {

    public BattleCommand(CratesPlugin plugin, BattleService battleService) {
        super(plugin);

        this.registerSubCommand(new ChallengeCommand(plugin, battleService));
        this.registerSubCommand(new AcceptCommand(plugin, battleService));
    }

    @Override
    public boolean execute(org.bukkit.command.CommandSender sender, String[] args) {
        return true;
    }

    @Override
    public String getName() {
        return "battle";
    }

    @Override
    public String getUsage() {
        return "/bitwa <wyzwij|akceptuj>";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public int getMaxArgs() {
        return -1;
    }
}

