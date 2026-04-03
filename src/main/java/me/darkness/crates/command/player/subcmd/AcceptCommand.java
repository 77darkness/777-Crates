package me.darkness.crates.command.player.subcmd;

import dev.darkness.utilities.text.TextUtil;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.SubCommand;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.battle.Challenge;
import me.darkness.crates.inv.ConfirmInv;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class AcceptCommand extends SubCommand {

    private final BattleService service;

    public AcceptCommand(CratesPlugin plugin, BattleService service) {
        super(plugin);
        this.service = service;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            TextUtil.send(sender, "&cTa komenda może byc uzyta tylko przez gracza!");
            return true;
        }

        Player challenger = Bukkit.getPlayerExact(args[0]);
        if (challenger == null) {
            lang().playerNotFound.send(sender, Map.of("player", args[0]));
            return true;
        }

        Optional<Challenge> challengeOpt = service.getChallengeForTarget(player.getUniqueId());
        if (challengeOpt.isEmpty() || !challengeOpt.get().getChallenger().equals(challenger.getUniqueId())) {
            lang().battleNoChallengeFromPlayer.send(sender, Map.of("player", challenger.getName()));
            return true;
        }

        Challenge challenge = challengeOpt.get();
        if (service.isExpired(challenge)) {
            service.cancelChallenge(challenge, Challenge.Status.EXPIRED);
            lang().battleChallengeExpired.send(sender);
            return true;
        }

        plugin.getServer().getScheduler().runTask(plugin, () ->
            new ConfirmInv(plugin, service).openForAccept(player, challenger, challenge));

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length != 1 || !(sender instanceof Player player)) return List.of();
        String input = args[0].toLowerCase(Locale.ROOT);

        Optional<Challenge> challenge = service.getChallengeForTarget(player.getUniqueId());
        if (challenge.isEmpty()) return List.of();

        UUID challengerUuid = challenge.get().getChallenger();
        Player challenger = Bukkit.getPlayer(challengerUuid);
        if (challenger == null) return List.of();

        return challenger.getName().toLowerCase(Locale.ROOT).startsWith(input)
                ? List.of(challenger.getName())
                : List.of();
    }

    @Override public String getName() { return "akceptuj"; }
    @Override public String getUsage() { return "/bitwa akceptuj <gracz>"; }
    @Override public String getPermission() { return null; }
    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }
}
