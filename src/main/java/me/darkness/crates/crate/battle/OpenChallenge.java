package me.darkness.crates.crate.battle;

import java.util.UUID;

public final class OpenChallenge {

    private final UUID id;
    private final UUID creator;
    private final String crateName;
    private final int amount;
    private final long createdAtMillis;

    public OpenChallenge(UUID creator, String crateName, int amount) {
        this.id = UUID.randomUUID();
        this.creator = creator;
        this.crateName = crateName;
        this.amount = amount;
        this.createdAtMillis = System.currentTimeMillis();
    }

    public UUID getId() { return id; }
    public UUID getCreator() { return creator; }
    public String getCrateName() { return crateName; }
    public int getAmount() { return amount; }
    public long getCreatedAtMillis() { return createdAtMillis; }
}

