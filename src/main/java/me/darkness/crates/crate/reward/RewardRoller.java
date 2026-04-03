package me.darkness.crates.crate.reward;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RewardRoller {

    private RewardRoller() {}

    public static CrateReward roll(List<CrateReward> rewards) {
        if (rewards == null || rewards.isEmpty()) return null;

        double total = rewards.stream()
                .filter(r -> r != null && r.getChance() > 0.0)
                .mapToDouble(CrateReward::getChance)
                .sum();

        if (total <= 0.0) {
            return rewards.get(ThreadLocalRandom.current().nextInt(rewards.size()));
        }

        double roll = ThreadLocalRandom.current().nextDouble(total);
        double cursor = 0.0;

        for (CrateReward reward : rewards) {
            if (reward == null || reward.getChance() <= 0.0) continue;
            cursor += reward.getChance();
            if (roll < cursor) return reward;
        }

        return rewards.stream()
                .filter(r -> r != null && r.getChance() > 0.0)
                .reduce((a, b) -> b)
                .orElse(rewards.get(0));
    }
}
