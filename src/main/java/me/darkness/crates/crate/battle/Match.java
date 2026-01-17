package me.darkness.crates.crate.battle;

import me.darkness.crates.crate.reward.CrateReward;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Match {

    private final UUID playerA;
    private final UUID playerB;

    private final String crateName;
    private final int amount;

    private final List<CrateReward> rewardsA = new ArrayList<>();
    private final List<CrateReward> rewardsB = new ArrayList<>();

    private boolean massFinishedA;
    private boolean massFinishedB;

    private final AtomicBoolean winnerHandled = new AtomicBoolean(false);

    private UUID winnerUuid;

    public Match(UUID playerA, UUID playerB, String crateName, int amount) {
        this.playerA = playerA;
        this.playerB = playerB;
        this.crateName = crateName;
        this.amount = amount;
    }

    public UUID getPlayerA() {
        return playerA;
    }

    public UUID getPlayerB() {
        return playerB;
    }

    public String getCrateName() {
        return crateName;
    }

    public int getAmount() {
        return amount;
    }

    public List<CrateReward> getRewardsA() {
        return rewardsA;
    }

    public List<CrateReward> getRewardsB() {
        return rewardsB;
    }

    public void setFinished(UUID who) {
        if (who == null) return;
        if (who.equals(playerA)) {
            massFinishedA = true;
        }
        if (who.equals(playerB)) {
            massFinishedB = true;
        }
    }

    public boolean isFinished() {
        return massFinishedA && massFinishedB;
    }

    public boolean tryFinishWinner() {
        return this.winnerHandled.compareAndSet(false, true);
    }

    public UUID getWinnerUuid() {
        return winnerUuid;
    }

    public void setWinnerUuid(UUID winnerUuid) {
        this.winnerUuid = winnerUuid;
    }
}
