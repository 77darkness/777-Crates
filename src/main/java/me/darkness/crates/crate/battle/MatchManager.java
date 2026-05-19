package me.darkness.crates.crate.battle;

import dev.darkness.utilities.task.SchedulerUtil;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.LangConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.impl.BattleRouletteAnimation;
import me.darkness.crates.crate.animation.impl.PlayerRouletteAnimation;
import me.darkness.crates.crate.key.KeyService;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;

final class MatchManager {

    private final CratesPlugin plugin;
    private final BattleState state;
    private final KeyService keyService;

    MatchManager(CratesPlugin plugin, BattleState state, KeyService keyService) {
        this.plugin = plugin;
        this.state = state;
        this.keyService = keyService;
    }

    boolean startMatch(Challenge challenge, Function<String, Optional<Crate>> crateFinder) {
        Player playerA = Bukkit.getPlayer(challenge.getChallenger());
        Player playerB = Bukkit.getPlayer(challenge.getTarget());
        if (playerA == null || playerB == null) return false;

        if (bothHaveActiveAnimation(playerA, playerB)) return false;

        Optional<Crate> crateOpt = crateFinder.apply(challenge.getCrateName());
        if (crateOpt.isEmpty() || crateOpt.get().getRewards().isEmpty()) return false;

        Crate crate = crateOpt.get();
        int amount = Math.max(1, Math.min(6, challenge.getAmount()));

        if (!takeKeysFromBoth(playerA, playerB, crate, amount)) return false;

        Match match = state.createMatch(
                playerA.getUniqueId(), playerB.getUniqueId(),
                crate.getName(), amount, crate,
                () -> ThreadLocalRandom.current().nextBoolean() ? playerA.getUniqueId() : playerB.getUniqueId()
        );

        startBattleAnimations(playerA, playerB, crate, match);
        return true;
    }

    void rouletteFinished(UUID who, Consumer<Match> onBothFinished) {
        Match match = state.getMatchByPlayer(who);
        if (match == null) return;

        match.setFinished(who);
        if (match.isFinished()) {
            onBothFinished.accept(match);
        }
    }

    void finishMassOpen(Match match, Function<String, Optional<Crate>> crateFinder) {
        Player a = Bukkit.getPlayer(match.getPlayerA());
        Player b = Bukkit.getPlayer(match.getPlayerB());

        UUID winner = resolveWinner(match);

        if (a == null || b == null) {
            giveRewards(winner, match);
            state.cleanupMatch(match.getPlayerA());
            return;
        }

        plugin.getAnimationService().removeAnimation(a);
        plugin.getAnimationService().removeAnimation(b);

        Crate crate = crateFinder.apply(match.getCrateName()).orElse(null);

        SchedulerUtil.run(plugin, () -> {
            plugin.getAnimationService().startAnimation(a, winnerAnimation(a, crate, match, winner));
            plugin.getAnimationService().startAnimation(b, winnerAnimation(b, crate, match, winner));
        });
    }

    void playerRouletteFinished(UUID viewer, UUID winner) {
        Match match = state.getMatchByPlayer(viewer);
        if (match == null) return;

        if (!match.tryFinishWinner()) return;

        giveRewards(resolveWinner(match, winner), match);

        UUID matchKey = state.getMatchKeyByPlayer(viewer);
        if (matchKey != null) {
            state.cleanupMatch(matchKey);
        }
    }

    void cancelForPlayer(UUID playerId) {
        Match match = state.getMatchByPlayer(playerId);
        if (match == null) return;

        UUID fallback = match.getPlayerA().equals(playerId) ? match.getPlayerB() : match.getPlayerA();
        UUID winner = Objects.requireNonNullElse(match.getWinnerUuid(), fallback);

        giveRewards(winner, match);

        UUID matchKey = state.getMatchKeyByPlayer(playerId);
        if (matchKey != null) {
            state.cleanupMatch(matchKey);
        }
    }

    void noKeys(Player player, int have, int need) {
        LangConfig langConfig = plugin.getConfigService().lang();
        langConfig.noKey.send(player, Map.of("have", String.valueOf(have), "need", String.valueOf(need)));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }

    private boolean bothHaveActiveAnimation(Player a, Player b) {
        boolean active = plugin.getAnimationService().hasActiveAnimation(a)
                || plugin.getAnimationService().hasActiveAnimation(b);
        if (active) {
            a.playSound(a.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            b.playSound(b.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        }
        return active;
    }

    private boolean takeKeysFromBoth(Player a, Player b, Crate crate, int amount) {
        int keysA = keyService.countKeys(a, crate.getName());
        if (keysA < amount) { noKeys(a, keysA, amount); return false; }

        int keysB = keyService.countKeys(b, crate.getName());
        if (keysB < amount) { noKeys(b, keysB, amount); return false; }

        LangConfig langConfig = plugin.getConfigService().lang();
        int freeA = plugin.getRewardExecutor().countFreeSlots(a);
        if (freeA < amount) {
            a.playSound(a.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            langConfig.inventoryFull.send(a);
            return false;
        }

        int freeB = plugin.getRewardExecutor().countFreeSlots(b);
        if (freeB < amount) {
            b.playSound(b.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            langConfig.inventoryFull.send(b);
            return false;
        }

        return keyService.takeKeys(a, crate.getName(), amount)
                && keyService.takeKeys(b, crate.getName(), amount);
    }

    private void startBattleAnimations(Player a, Player b, Crate crate, Match match) {
        a.closeInventory();
        b.closeInventory();
        plugin.getAnimationService().startAnimation(a,
                new BattleRouletteAnimation(plugin, plugin.getBattleService(), a, crate, match.getRewardsA()));
        plugin.getAnimationService().startAnimation(b,
                new BattleRouletteAnimation(plugin, plugin.getBattleService(), b, crate, match.getRewardsB()));
    }

    private PlayerRouletteAnimation winnerAnimation(Player viewer, Crate crate, Match match, UUID winner) {
        String title = plugin.getConfigService().rouletteInv().winnerTitle;
        return new PlayerRouletteAnimation(plugin, viewer, crate,
                match.getPlayerA(), match.getPlayerB(),
                title, winner);
    }

    private void giveRewards(UUID winnerUuid, Match match) {
        Player winner = Bukkit.getPlayer(winnerUuid);
        if (winner == null) return;
        Crate crate = match.getCrate();
        match.getRewardsA().forEach(r -> plugin.getRewardExecutor().giveReward(winner, crate, r));
        match.getRewardsB().forEach(r -> plugin.getRewardExecutor().giveReward(winner, crate, r));
    }

    private UUID resolveWinner(Match match) {
        return Objects.requireNonNullElse(match.getWinnerUuid(), match.getPlayerA());
    }

    private UUID resolveWinner(Match match, UUID fallback) {
        return Objects.requireNonNullElse(match.getWinnerUuid(), fallback);
    }
}
