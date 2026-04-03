package me.darkness.crates.crate.battle;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.crate.key.KeyService;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class BattleService {

    private final CratesPlugin plugin;
    private final CrateService crateService;
    private final BattleState state;
    private final MatchManager coordinator;

    public BattleService(CratesPlugin plugin, CrateService crateService, KeyService keyService) {
        this.plugin = plugin;
        this.crateService = crateService;
        this.state = new BattleState();
        this.coordinator = new MatchManager(plugin, this.state, keyService);
    }

    public Optional<Crate> findCrate(String name) {
        return name == null ? Optional.empty() : crateService.getCrate(name);
    }

    public Collection<Crate> getAllCrates() {
        return crateService.getAllCrates();
    }

    public BattleSession createSession(UUID owner, UUID opponent) {
        return state.createSession(owner, opponent);
    }

    public BattleSession getSession(UUID owner) {
        return state.getSession(owner);
    }

    public Optional<Challenge> getChallengeForTarget(UUID target) {
        return state.getChallengeForTarget(target);
    }

    public boolean createChallenge(Player challenger, Player target, String crateName, int amount) {
        if (challenger == null || target == null) return false;
        return state.createChallenge(challenger.getUniqueId(), target.getUniqueId(), crateName, amount);
    }

    public void cancelChallenge(Challenge challenge, Challenge.Status status) {
        if (challenge == null) return;
        challenge.setStatus(status);
        state.removeChallenge(challenge);
    }

    public boolean isExpired(Challenge challenge) {
        if (challenge == null) return true;
        int seconds = Math.max(1, plugin.getConfigService().getCrateConfig().challengeExpire);
        return System.currentTimeMillis() > challenge.getCreatedAtMillis() + (seconds * 1000L);
    }

    public void startMatch(Challenge challenge) {
        if (challenge == null) return;

        Optional<Crate> crateOpt = findCrate(challenge.getCrateName());
        if (crateOpt.isEmpty() || crateOpt.get().getRewards().isEmpty()) {
            notifyNoCrate(challenge);
            cancelChallenge(challenge, Challenge.Status.CANCELLED);
            return;
        }

        if (coordinator.startMatch(challenge, this::findCrate)) {
            cancelChallenge(challenge, Challenge.Status.ACCEPTED);
        }
    }

    public void rouletteFinished(UUID who) {
        coordinator.rouletteFinished(who, match -> coordinator.finishMassOpen(match, this::findCrate));
    }

    public void playerRouletteFinished(UUID viewer, UUID winner) {
        coordinator.playerRouletteFinished(viewer, winner);
    }

    public void noKeys(Player player, int have, int need) {
        coordinator.noKeys(player, have, need);
    }

    public void cleanupForPlayer(UUID playerId) {
        if (playerId == null) return;
        state.removeSession(playerId);
        state.removeChallengeForPlayer(playerId);
        coordinator.cancelForPlayer(playerId);
    }

    public void shutdown() {
        for (Map.Entry<UUID, Match> entry : state.getActiveMatchesCopy().entrySet()) {
            Match match = entry.getValue();
            if (match == null) continue;
            UUID winner = match.getWinnerUuid() != null ? match.getWinnerUuid() : match.getPlayerA();
            coordinator.playerRouletteFinished(entry.getKey(), winner);
        }
        state.clearAll();
    }

    private void notifyNoCrate(Challenge challenge) {
        var lang = plugin.getConfigService().getLangConfig();
        var ph = Map.of("crate", challenge.getCrateName() == null ? "" : challenge.getCrateName());

        Player a = plugin.getServer().getPlayer(challenge.getChallenger());
        Player b = plugin.getServer().getPlayer(challenge.getTarget());

        if (a != null) lang.crateNoRewards.send(a, ph);
        if (b != null) lang.crateNoRewards.send(b, ph);
    }
}
