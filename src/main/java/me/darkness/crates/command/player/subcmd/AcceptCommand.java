package me.darkness.crates.command.player.subcmd;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.command.ICommand;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.battle.Challenge;
import me.darkness.crates.inv.ConfirmInv;
import me.darkness.crates.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AcceptCommand implements ICommand {

    private final CratesPlugin plugin;
    private final BattleService service;

    public AcceptCommand(CratesPlugin plugin, BattleService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.color("&cTa komenda może byc uzyta tylko przez gracza!"));
            return true;
        }

        Player challenger = Bukkit.getPlayerExact(args[0]);
        if (challenger == null) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, sender,
                    this.plugin.getConfigService().getLangConfig().playerNotFound,
                    Map.of("player", args[0])
            );
            return true;
        }

        Optional<Challenge> challengeOpt = service.getChallengeForTarget(player.getUniqueId());
        if (challengeOpt.isEmpty() || !challengeOpt.get().getChallenger().equals(challenger.getUniqueId())) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, sender,
                    this.plugin.getConfigService().getLangConfig().battleNoChallengeFromPlayer,
                    Map.of("player", challenger.getName())
            );
            return true;
        }

        Challenge challenge = challengeOpt.get();
        if (service.isExpired(challenge)) {
            service.cancelChallenge(challenge, Challenge.Status.EXPIRED);
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, sender,
                    this.plugin.getConfigService().getLangConfig().battleChallengeExpired
            );
            return true;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> new ConfirmInv(plugin, service)
                .openForAccept(player, challenger, challenge));

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
        return "akceptuj";
    }

    @Override
    public String getUsage() {
        return "/bitwa akceptuj <gracz>";
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
