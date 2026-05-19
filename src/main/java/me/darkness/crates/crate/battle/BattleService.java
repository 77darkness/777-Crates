package me.darkness.crates.crate.battle;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.configuration.LangConfig;
import me.darkness.crates.crate.key.KeyService;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BattleService {

    private final CratesPlugin plugin;
    private final CrateService crateService;
    private final BattleState state;
    private final MatchManager matchManager;
    private final Map<UUID, BattleCountdown> activeCountdowns = new ConcurrentHashMap<>();

    public BattleService(CratesPlugin plugin, CrateService crateService, KeyService keyService) {
        this.plugin = plugin;
        this.crateService = crateService;
        this.state = new BattleState();
        this.matchManager = new MatchManager(plugin, this.state, keyService);
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
        return state.createChallenge(challenger.getUniqueId(), target.getUniqueId(), crateName, amount);
    }

    public void cancelChallenge(Challenge challenge, Challenge.Status status) {
        challenge.setStatus(status);
        state.removeChallenge(challenge);
    }

    public boolean isExpired(Challenge challenge) {
        int seconds = Math.max(1, plugin.getConfigService().config().challengeExpire);
        return System.currentTimeMillis() > challenge.getCreatedAt() + (seconds * 1000L);
    }

    public void startMatch(Challenge challenge) {
        Optional<Crate> crateOpt = crateService.getCrate(challenge.getCrateName());
        if (crateOpt.isEmpty() || crateOpt.get().getRewards().isEmpty()) {
            notifyNoCrate(challenge);
            cancelChallenge(challenge, Challenge.Status.CANCELLED);
            return;
        }

        if (matchManager.startMatch(challenge, crateService::getCrate)) {
            cancelChallenge(challenge, Challenge.Status.ACCEPTED);
        }
    }

    public void startCountdown(Challenge challenge) {
        int seconds = plugin.getConfigService().config().battleCountdownSeconds;
        if (seconds <= 0) {
            startMatch(challenge);
            return;
        }

        BattleCountdown countdown = new BattleCountdown(plugin, challenge, seconds, () -> {
            activeCountdowns.remove(challenge.getChallenger());
            activeCountdowns.remove(challenge.getTarget());
            startMatch(challenge);
        });
        activeCountdowns.put(challenge.getChallenger(), countdown);
        activeCountdowns.put(challenge.getTarget(), countdown);
        countdown.start();
    }

    public BattleCountdown getCountdown(UUID playerId) {
        return activeCountdowns.get(playerId);
    }

    public void removeCountdown(UUID a, UUID b) {
        activeCountdowns.remove(a);
        activeCountdowns.remove(b);
    }

    public void rouletteFinished(UUID who) {
        matchManager.rouletteFinished(who, match -> matchManager.finishMassOpen(match, crateService::getCrate));
    }

    public void playerRouletteFinished(UUID viewer, UUID winner) {
        matchManager.playerRouletteFinished(viewer, winner);
    }

    public void noKeys(Player player, int have, int need) {
        matchManager.noKeys(player, have, need);
    }

    public void cleanupForPlayer(UUID playerId) {
        state.removeSession(playerId);
        state.removeChallengeForPlayer(playerId);
        state.removeOpenChallengeByCreator(playerId);
        matchManager.cancelForPlayer(playerId);
    }

    public Collection<OpenChallenge> getOpenChallenges() {
        return state.getOpenChallenges();
    }

    public void addOpenChallenge(OpenChallenge challenge) {
        state.addOpenChallenge(challenge);
    }

    public void removeOpenChallenge(UUID id) {
        state.removeOpenChallenge(id);
    }

    public void startMatchFromOpen(OpenChallenge open, Player joiner) {
        Challenge challenge = new Challenge(open.getCreator(), joiner.getUniqueId(),
                open.getCrateName(), open.getAmount(), System.currentTimeMillis());
        state.removeOpenChallenge(open.getId());
        startCountdown(challenge);
    }

    public void shutdown() {
        for (Map.Entry<UUID, Match> entry : state.getActiveMatchesCopy().entrySet()) {
            Match match = entry.getValue();
            UUID winner = Objects.requireNonNullElse(match.getWinnerUuid(), match.getPlayerA());
            matchManager.playerRouletteFinished(entry.getKey(), winner);
        }
        state.clearAll();
    }

    private void notifyNoCrate(Challenge challenge) {
        LangConfig lang = plugin.getConfigService().lang();
        Map<String, String> ph = Map.of("crate", challenge.getCrateName());

        Player a = plugin.getServer().getPlayer(challenge.getChallenger());
        Player b = plugin.getServer().getPlayer(challenge.getTarget());

        if (a != null) lang.crateNoRewards.send(a, ph);
        if (b != null) lang.crateNoRewards.send(b, ph);
    }
}
