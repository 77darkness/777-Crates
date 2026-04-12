package me.darkness.crates.crate.battle;

import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.reward.CrateReward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Match {

    private final UUID playerA;
    private final UUID playerB;
    private final String crateName;
    private final int amount;
    private final Crate crate;

    private final List<CrateReward> rewardsA = new ArrayList<>();
    private final List<CrateReward> rewardsB = new ArrayList<>();

    private boolean finishedA;
    private boolean finishedB;

    private final AtomicBoolean winnerHandled = new AtomicBoolean(false);
    private UUID winnerUuid;

    public Match(UUID playerA, UUID playerB, String crateName, int amount, Crate crate) {
        this.playerA = playerA;
        this.playerB = playerB;
        this.crateName = crateName;
        this.amount = amount;
        this.crate = crate;
    }

    public UUID getPlayerA() { return playerA; }
    public UUID getPlayerB() { return playerB; }
    public String getCrateName() { return crateName; }
    public int getAmount() { return amount; }
    public Crate getCrate() { return crate; }

    public void addRewardA(CrateReward reward) { rewardsA.add(reward); }
    public void addRewardB(CrateReward reward) { rewardsB.add(reward); }

    public List<CrateReward> getRewardsA() { return Collections.unmodifiableList(rewardsA); }
    public List<CrateReward> getRewardsB() { return Collections.unmodifiableList(rewardsB); }

    public void setFinished(UUID who) {
        if (who == null) return;
        if (who.equals(playerA)) finishedA = true;
        if (who.equals(playerB)) finishedB = true;
    }

    public boolean isFinished() { return finishedA && finishedB; }

    public boolean tryFinishWinner() { return winnerHandled.compareAndSet(false, true); }

    public UUID getWinnerUuid() { return winnerUuid; }
    public void setWinnerUuid(UUID winnerUuid) { this.winnerUuid = winnerUuid; }
}
