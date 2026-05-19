package me.darkness.crates.commands;

import dev.darkness.commands.annotation.Arg;
import dev.darkness.commands.annotation.Command;
import dev.darkness.commands.annotation.Context;
import dev.darkness.commands.annotation.Execute;
import dev.darkness.utilities.task.SchedulerUtil;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.LangConfig;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.battle.Challenge;
import me.darkness.crates.inventories.ConfirmInv;
import me.darkness.crates.inventories.BattleInv;
import me.darkness.crates.inventories.SelectInv;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

@Command(
        name = "battle",
        aliases = {"casebattle", "bitwa", "bitwy"},
        playerOnly = true
)
public final class BattlePlayerCommand {

    private final CratesPlugin plugin;
    private final BattleService battleService;

    public BattlePlayerCommand(CratesPlugin plugin, BattleService battleService) {
        this.plugin = plugin;
        this.battleService = battleService;
    }

    private LangConfig lang() {
        return plugin.getConfigService().lang();
    }

    @Execute
    public void open(@Context Player player) {
        SchedulerUtil.run(plugin,
                () -> new BattleInv(plugin, battleService).open(player));
    }

    @Execute("wyzwij")
    public void challenge(@Context Player player, @Arg Player target) {
        if (target == null) {
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            lang().battleCantChallengeSelf.send(player);
            return;
        }

        SchedulerUtil.run(plugin,
                () -> new SelectInv(plugin, battleService).open(player, target));
    }

    @Execute("akceptuj")
    public void accept(@Context Player player, @Arg String challengerName) {
        Player challenger = Bukkit.getPlayerExact(challengerName);
        if (challenger == null) {
            lang().playerNotFound.send(player, Map.of("player", challengerName));
            return;
        }

        Optional<Challenge> challengeOpt = battleService.getChallengeForTarget(player.getUniqueId());
        if (challengeOpt.isEmpty() || !challengeOpt.get().getChallenger().equals(challenger.getUniqueId())) {
            lang().battleNoChallengeFromPlayer.send(player, Map.of("player", challenger.getName()));
            return;
        }

        Challenge challenge = challengeOpt.get();
        if (battleService.isExpired(challenge)) {
            battleService.cancelChallenge(challenge, Challenge.Status.EXPIRED);
            lang().battleChallengeExpired.send(player);
            return;
        }

        SchedulerUtil.run(plugin,
                () -> new ConfirmInv(plugin, battleService).openForAccept(player, challenger, challenge));
    }
}

