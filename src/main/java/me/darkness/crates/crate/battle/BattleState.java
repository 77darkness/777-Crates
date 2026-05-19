package me.darkness.crates.crate.battle;

import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.reward.RewardRoller;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

final class BattleState {

    private final Map<UUID, BattleSession> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, Challenge> challengesByTarget = new ConcurrentHashMap<>();
    private final Map<UUID, Challenge> challengesByChallenger = new ConcurrentHashMap<>();
    private final Map<UUID, Match> activeMatches = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> matchByPlayer = new ConcurrentHashMap<>();
    private final Map<UUID, OpenChallenge> openChallenges = new ConcurrentHashMap<>();

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
        challengesByChallenger.remove(challenge.getChallenger());
        challengesByTarget.remove(challenge.getTarget());
    }

    void removeChallengeForPlayer(UUID playerId) {
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
        Match match = new Match(playerA, playerB, crateName, amount, crate);

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
        return new ConcurrentHashMap<>(activeMatches);
    }

    Collection<OpenChallenge> getOpenChallenges() {
        return java.util.Collections.unmodifiableCollection(openChallenges.values());
    }

    boolean addOpenChallenge(OpenChallenge challenge) {
        if (openChallenges.containsKey(challenge.getId())) return false;
        openChallenges.put(challenge.getId(), challenge);
        return true;
    }

    void removeOpenChallenge(UUID id) {
        openChallenges.remove(id);
    }

    void removeOpenChallengeByCreator(UUID creatorId) {
        openChallenges.values().removeIf(c -> c.getCreator().equals(creatorId));
    }

    void clearAll() {
        sessions.clear();
        challengesByTarget.clear();
        challengesByChallenger.clear();
        activeMatches.clear();
        matchByPlayer.clear();
        openChallenges.clear();
    }
}
