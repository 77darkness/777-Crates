package me.darkness.crates.crate.battle;

import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.reward.RewardRoller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

final class BattleState {

    private final Map<UUID, BattleSession> sessions = new HashMap<>();
    private final Map<UUID, Challenge> challengesByTarget = new HashMap<>();
    private final Map<UUID, Challenge> challengesByChallenger = new HashMap<>();
    private final Map<UUID, Match> activeMatches = new HashMap<>();
    private final Map<UUID, UUID> matchByPlayer = new HashMap<>();

    BattleSession createSession(UUID owner, UUID opponent) {
        BattleSession session = new BattleSession(owner, opponent);
        sessions.put(owner, session);
        return session;
    }

    BattleSession getSession(UUID owner) {
        return sessions.get(owner);
    }

    void removeSession(UUID playerId) {
        sessions.remove(playerId);
    }

    Optional<Challenge> getChallengeForTarget(UUID target) {
        return Optional.ofNullable(challengesByTarget.get(target));
    }

    boolean createChallenge(UUID challengerId, UUID targetId, String crateName, int amount) {
        if (challengesByChallenger.containsKey(challengerId) || challengesByTarget.containsKey(targetId)) {
            return false;
        }
        Challenge challenge = new Challenge(challengerId, targetId, crateName, amount, System.currentTimeMillis());
        challengesByChallenger.put(challengerId, challenge);
        challengesByTarget.put(targetId, challenge);
        return true;
    }

    void removeChallenge(Challenge challenge) {
        if (challenge == null) return;
        challengesByChallenger.remove(challenge.getChallenger());
        challengesByTarget.remove(challenge.getTarget());
    }

    void removeChallengeForPlayer(UUID playerId) {
        if (playerId == null) return;

        Challenge byTarget = challengesByTarget.remove(playerId);
        if (byTarget != null) {
            challengesByChallenger.remove(byTarget.getChallenger());
            byTarget.setStatus(Challenge.Status.CANCELLED);
            return;
        }

        Challenge byChallenger = challengesByChallenger.remove(playerId);
        if (byChallenger != null) {
            challengesByTarget.remove(byChallenger.getTarget());
            byChallenger.setStatus(Challenge.Status.CANCELLED);
        }
    }

    Match getMatchByPlayer(UUID playerId) {
        UUID key = matchByPlayer.get(playerId);
        return key == null ? null : activeMatches.get(key);
    }

    UUID getMatchKeyByPlayer(UUID playerId) {
        return matchByPlayer.get(playerId);
    }

    Match createMatch(UUID playerA, UUID playerB, String crateName, int amount, Crate crate, Supplier<UUID> winnerSupplier) {
        Match match = new Match(playerA, playerB, crateName, amount);

        activeMatches.put(playerA, match);
        matchByPlayer.put(playerA, playerA);
        matchByPlayer.put(playerB, playerA);

        if (crate != null) {
            for (int i = 0; i < amount; i++) {
                CrateReward a = RewardRoller.roll(crate.getRewards());
                if (a != null) match.addRewardA(a);

                CrateReward b = RewardRoller.roll(crate.getRewards());
                if (b != null) match.addRewardB(b);
            }
        }

        if (winnerSupplier != null) {
            match.setWinnerUuid(winnerSupplier.get());
        }

        return match;
    }

    void cleanupMatch(UUID matchKey) {
        Match match = activeMatches.remove(matchKey);
        if (match == null) return;
        matchByPlayer.remove(match.getPlayerA());
        matchByPlayer.remove(match.getPlayerB());
    }

    boolean hasActiveMatch(UUID matchKey) {
        return activeMatches.containsKey(matchKey);
    }

    Map<UUID, Match> getActiveMatchesCopy() {
        return new HashMap<>(activeMatches);
    }

    void clearAll() {
        sessions.clear();
        challengesByTarget.clear();
        challengesByChallenger.clear();
        activeMatches.clear();
        matchByPlayer.clear();
    }
}
