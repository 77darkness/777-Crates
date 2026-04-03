package me.darkness.crates.crate.battle;

import java.util.UUID;

public final class Challenge {

    public enum Status { PENDING, ACCEPTED, CANCELLED, EXPIRED }

    private final UUID challenger;
    private final UUID target;
    private final String crateName;
    private final int amount;
    private final long createdAtMillis;
    private Status status;

    public Challenge(UUID challenger, UUID target, String crateName, int amount, long createdAtMillis) {
        this.challenger = challenger;
        this.target = target;
        this.crateName = crateName;
        this.amount = amount;
        this.createdAtMillis = createdAtMillis;
        this.status = Status.PENDING;
    }

    public UUID getChallenger() { return challenger; }
    public UUID getTarget() { return target; }
    public String getCrateName() { return crateName; }
    public int getAmount() { return amount; }
    public long getCreatedAtMillis() { return createdAtMillis; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
