package me.darkness.crates.crate.battle;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Lang;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.impl.BattleRouletteAnimation;
import me.darkness.crates.crate.animation.impl.PlayerRouletteAnimation;
import me.darkness.crates.util.TextUtil;
import me.darkness.crates.crate.key.KeyService;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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

    boolean startMatch(Challenge challenge, Function<String, java.util.Optional<Crate>> crateFinder) {
        if (challenge == null) return false;

        Player playerA = Bukkit.getPlayer(challenge.getChallenger());
        Player playerB = Bukkit.getPlayer(challenge.getTarget());
        if (playerA == null || playerB == null) return false;

        if (hasActiveAnimation(playerA, playerB)) return false;

        java.util.Optional<Crate> crateOpt = crateFinder.apply(challenge.getCrateName());
        if (crateOpt.isEmpty() || crateOpt.get().getRewards().isEmpty()) return false;

        Crate crate = crateOpt.get();
        int amount = Math.max(1, Math.min(6, challenge.getAmount()));

        if (!tryTakeKeysBoth(playerA, playerB, crate, amount)) {
            return false;
        }

        Match match = state.createMatch(
                playerA.getUniqueId(),
                playerB.getUniqueId(),
                crate.getName(),
                amount,
                crate,
                () -> ThreadLocalRandom.current().nextBoolean() ? playerA.getUniqueId() : playerB.getUniqueId()
        );

        startBattleAnimations(playerA, playerB, crate, match);
        return true;
    }

    void rouletteFinished(UUID who, java.util.function.Consumer<Match> onBothFinished) {
        Match match = state.getMatchByPlayer(who);
        if (match == null) return;

        match.setFinished(who);
        if (!match.isFinished()) return;

        onBothFinished.accept(match);
    }

    void finishMassOpen(Match match, Function<String, Optional<Crate>> crateFinder) {
        Player a = Bukkit.getPlayer(match.getPlayerA());
        Player b = Bukkit.getPlayer(match.getPlayerB());

        UUID winner = match.getWinnerUuid() != null ? match.getWinnerUuid() : match.getPlayerA();

        if (a == null || b == null) {
            giveRewards(winner, match);
            state.cleanupMatch(match.getPlayerA());
            return;
        }

        plugin.getAnimationService().removeAnimation(a);
        plugin.getAnimationService().removeAnimation(b);

        Map<UUID, ItemStack> headCache = new HashMap<>();

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getAnimationService().startCustomAnimation(
                    a,
                    new PlayerRouletteAnimation(plugin, a,
                            crateFinder.apply(match.getCrateName()).orElse(null),
                            match.getPlayerA(),
                            match.getPlayerB(),
                            headCache,
                            "&0ʟᴏꜱᴏᴡᴀɴɪᴇ ᴢᴡʏᴄɪᴇᴢᴄʏ...",
                            winner
                    )
            );

            plugin.getAnimationService().startCustomAnimation(
                    b,
                    new PlayerRouletteAnimation(plugin, b,
                            crateFinder.apply(match.getCrateName()).orElse(null),
                            match.getPlayerA(),
                            match.getPlayerB(),
                            headCache,
                            "&0ʟᴏꜱᴏᴡᴀɴɪᴇ ᴢᴡʏᴄɪᴇᴢᴄʏ...",
                            winner
                    )
            );
        });
    }

    void playerRouletteFinished(UUID viewer, UUID winner) {
        Match match = state.getMatchByPlayer(viewer);
        if (match == null) return;

        if (!match.tryFinishWinner()) return;

        UUID finalWinner = match.getWinnerUuid() != null ? match.getWinnerUuid() : winner;
        giveRewards(finalWinner, match);

        UUID matchKey = state.getMatchKeyByPlayer(viewer);
        if (matchKey != null) {
            state.cleanupMatch(matchKey);
        }
    }

    void cancelForPlayer(UUID playerId) {
        if (playerId == null) return;

        Match match = state.getMatchByPlayer(playerId);
        if (match == null) return;

        UUID winner = match.getWinnerUuid() != null
                ? match.getWinnerUuid()
                : (match.getPlayerA().equals(playerId) ? match.getPlayerB() : match.getPlayerA());

        giveRewards(winner, match);

        UUID matchKey = state.getMatchKeyByPlayer(playerId);
        if (matchKey != null) {
            state.cleanupMatch(matchKey);
        }
    }

    private boolean hasActiveAnimation(Player a, Player b) {
        if (!plugin.getAnimationService().hasActiveAnimation(a)
                && !plugin.getAnimationService().hasActiveAnimation(b)) {
            return false;
        }

        a.playSound(a.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        b.playSound(b.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        return true;
    }

    private boolean tryTakeKeysBoth(Player a, Player b, Crate crate, int amount) {
        int keysA = keyService.countKeys(a, crate.getName());
        int keysB = keyService.countKeys(b, crate.getName());

        if (keysA < amount) {
            noKeys(a, keysA, amount);
            return false;
        }

        if (keysB < amount) {
            noKeys(b, keysB, amount);
            return false;
        }

        return keyService.takeKeys(a, crate.getName(), amount)
                && keyService.takeKeys(b, crate.getName(), amount);
    }

    private void startBattleAnimations(Player a, Player b, Crate crate, Match match) {
        a.closeInventory();
        b.closeInventory();

        plugin.getAnimationService().startCustomAnimation(
                a,
                new BattleRouletteAnimation(plugin, plugin.getBattleService(), a, crate, match.getRewardsA())
        );

        plugin.getAnimationService().startCustomAnimation(
                b,
                new BattleRouletteAnimation(plugin, plugin.getBattleService(), b, crate, match.getRewardsB())
        );
    }

    private void giveRewards(UUID winnerUuid, Match match) {
        Player winner = Bukkit.getPlayer(winnerUuid);
        if (winner == null) return;

        String crateName = match.getCrateName();

        match.getRewardsA().forEach(r -> plugin.getRewardExecutor().giveReward(winner, crateName, r));
        match.getRewardsB().forEach(r -> plugin.getRewardExecutor().giveReward(winner, crateName, r));
    }

    void noKeys(Player player, int have, int need) {
        Lang lang = plugin.getConfigService().getLangConfig();
        TextUtil.send(
                lang,
                plugin,
                player,
                lang.battleNoKeysToAccept,
                Map.of("have", String.valueOf(have), "need", String.valueOf(need))
        );
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }
}
