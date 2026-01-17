package me.darkness.crates.crate.reward;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RewardRoller {

    private RewardRoller() {
    }

    public static CrateReward roll(List<CrateReward> rewards) {
        if (rewards == null || rewards.isEmpty()) {
            return null;
        }

        double total = 0.0;
        for (CrateReward reward : rewards) {
            if (reward == null) {
                continue;
            }
            double chance = reward.getChance();
            if (chance > 0.0) {
                total += chance;
            }
        }

        if (total <= 0.0) {
            return rewards.get(ThreadLocalRandom.current().nextInt(rewards.size()));
        }

        double r = ThreadLocalRandom.current().nextDouble(total);
        double cursor = 0.0;

        for (CrateReward reward : rewards) {
            if (reward == null) {
                continue;
            }
            double chance = reward.getChance();
            if (chance <= 0.0) {
                continue;
            }
            cursor += chance;
            if (r < cursor) {
                return reward;
            }
        }

        for (int i = rewards.size() - 1; i >= 0; i--) {
            CrateReward reward = rewards.get(i);
            if (reward != null && reward.getChance() > 0.0) {
                return reward;
            }
        }

        return rewards.get(0);
    }
}

