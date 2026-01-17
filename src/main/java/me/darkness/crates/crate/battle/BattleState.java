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

    final Map<UUID, BattleSession> sessions = new HashMap<>();
    final Map<UUID, Challenge> challengesByTarget = new HashMap<>();
    final Map<UUID, Challenge> challengesByChallenger = new HashMap<>();

    final Map<UUID, Match> activeMatches = new HashMap<>();
    final Map<UUID, UUID> matchByPlayer = new HashMap<>();

    BattleSession createSession(UUID owner, UUID opponent) {
        BattleSession battleSession = new BattleSession(owner, opponent);
        sessions.put(owner, battleSession);
        return battleSession;
    }

    BattleSession getSession(UUID owner) {
        return sessions.get(owner);
    }

    Optional<Challenge> getChallengeForTarget(UUID target) {
        return Optional.ofNullable(challengesByTarget.get(target));
    }

    boolean createChallenge(UUID challengerId, UUID targetId, String crateName, int amount) {
        if (challengerId == null || targetId == null) {
            return false;
        }
        if (challengesByChallenger.containsKey(challengerId) || challengesByTarget.containsKey(targetId)) {
            return false;
        }

        Challenge challenge = new Challenge(
                challengerId,
                targetId,
                crateName,
                amount,
                System.currentTimeMillis()
        );

        challengesByChallenger.put(challengerId, challenge);
        challengesByTarget.put(targetId, challenge);
        return true;
    }

    void cancelChallenge(Challenge challenge) {
        if (challenge == null) return;
        challenge.setStatus();
        challengesByChallenger.remove(challenge.getChallenger());
        challengesByTarget.remove(challenge.getTarget());
    }

    void removeChallengeForPlayer(UUID playerId) {
        if (playerId == null) {
            return;
        }

        Challenge removed = challengesByTarget.remove(playerId);
        if (removed != null) {
            challengesByChallenger.remove(removed.getChallenger());
            removed.setStatus();
            return;
        }

        removed = challengesByChallenger.remove(playerId);
        if (removed != null) {
            challengesByTarget.remove(removed.getTarget());
            removed.setStatus();
        }

    }

    Match getMatchByPlayer(UUID playerId) {
        UUID key = matchByPlayer.get(playerId);
        return key == null ? null : activeMatches.get(key);
    }

    UUID getMatchKeyByPlayer(UUID playerId) {
        return matchByPlayer.get(playerId);
    }

    Match createMatch(UUID playerA, UUID playerB, String crateName, int amount, Crate crate,
                     Supplier<UUID> winnerSupplier) {
        Match match = new Match(playerA, playerB, crateName, amount);

        activeMatches.put(playerA, match);
        matchByPlayer.put(playerA, playerA);
        matchByPlayer.put(playerB, playerA);

        if (crate != null) {
            for (int i = 0; i < amount; i++) {
                CrateReward a = RewardRoller.roll(crate.getRewards());
                if (a != null) match.getRewardsA().add(a);

                CrateReward b = RewardRoller.roll(crate.getRewards());
                if (b != null) match.getRewardsB().add(b);
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

    void clearAll() {
        sessions.clear();
        challengesByTarget.clear();
        challengesByChallenger.clear();
        activeMatches.clear();
        matchByPlayer.clear();
    }
}
