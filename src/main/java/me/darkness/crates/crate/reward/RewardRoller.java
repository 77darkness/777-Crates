package me.darkness.crates.crate.reward;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RewardRoller {

    public static List<CrateReward> normalizeAll(List<CrateReward> rewards) {
        if (rewards == null || rewards.isEmpty()) return rewards;

        double total = 0;
        for (CrateReward r : rewards) {
            if (r != null && r.getChance() > 0) total += r.getChance();
        }

        if (total <= 0) return rewards;

        List<CrateReward> result = new ArrayList<>(rewards.size());
        double assigned = 0;

        for (int i = 0; i < rewards.size(); i++) {
            CrateReward r = rewards.get(i);
            if (r == null) { result.add(null); continue; }

            double normalized;
            if (i == rewards.size() - 1) {
                normalized = Math.max(0.01, Math.round((100.0 - assigned) * 100.0) / 100.0);
            } else {
                normalized = Math.round((r.getChance() / total) * 10000.0) / 100.0;
                assigned += normalized;
            }
            result.add(r.withChance(normalized));
        }
        return result;
    }

    public static CrateReward roll(List<CrateReward> rewards) {
        if (rewards == null || rewards.isEmpty()) return null;

        double total = 0;
        for (CrateReward r : rewards) {
            if (r != null && r.getChance() > 0) total += r.getChance();
        }

        if (total <= 0)
            return rewards.get(ThreadLocalRandom.current().nextInt(rewards.size()));

        double roll = ThreadLocalRandom.current().nextDouble(total);
        double cursor = 0;

        for (CrateReward reward : rewards) {
            if (reward == null || reward.getChance() <= 0) continue;
            cursor += reward.getChance();
            if (roll < cursor) return reward;
        }

        return rewards.get(rewards.size() - 1);
    }
}
