package me.darkness.crates.crate.battle;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.crate.key.KeyService;
import me.darkness.crates.util.TextUtil;
import org.bukkit.entity.Player;

import java.util.Collection;
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

        UUID challengerId = challenger.getUniqueId();
        UUID targetId = target.getUniqueId();

        return state.createChallenge(challengerId, targetId, crateName, amount);
    }

    public void cancelChallenge(Challenge challenge, Challenge.Status status) {
        state.cancelChallenge(challenge);
    }

    public boolean isExpired(Challenge challenge) {
        if (challenge == null) return true;

        int seconds;
        try {
            seconds = Math.max(
                    1,
                    plugin.getConfigService()
                            .getCrateConfig()
                            .challengeExpire
            );
        } catch (Exception e) {
            seconds = 60;
        }

        long expireAt = challenge.getCreatedAtMillis() + (seconds * 1000L);
        return System.currentTimeMillis() > expireAt;
    }

    public void startMatch(Challenge challenge) {
        if (challenge == null) return;

        var crateOpt = findCrate(challenge.getCrateName());
        if (crateOpt.isEmpty() || crateOpt.get().getRewards().isEmpty()) {
            var lang = plugin.getConfigService().getLangConfig();
            var ph = java.util.Map.of("crate", challenge.getCrateName() == null ? "" : challenge.getCrateName());

            Player a = plugin.getServer().getPlayer(challenge.getChallenger());
            Player b = plugin.getServer().getPlayer(challenge.getTarget());
            if (a != null) {
                TextUtil.send(lang, plugin, a, lang.crateNoRewards, ph);
            }
            if (b != null) {
                TextUtil.send(lang, plugin, b, lang.crateNoRewards, ph);
            }

            cancelChallenge(challenge, Challenge.Status.CANCELLED);
            return;
        }

        boolean started = coordinator.startMatch(challenge, this::findCrate);
        if (!started) {
            return;
        }

        cancelChallenge(challenge, Challenge.Status.ACCEPTED);
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
        if (playerId == null) {
            return;
        }

        state.sessions.remove(playerId);
        state.removeChallengeForPlayer(playerId);
        coordinator.cancelForPlayer(playerId);
    }

    public void shutdown() {
        for (UUID matchKey : java.util.List.copyOf(state.activeMatches.keySet())) {
            Match match = state.activeMatches.get(matchKey);
            if (match != null) {
                UUID winner = match.getWinnerUuid() != null ? match.getWinnerUuid() : match.getPlayerA();
                coordinator.playerRouletteFinished(matchKey, winner);
            }
        }
        state.clearAll();
    }
}
