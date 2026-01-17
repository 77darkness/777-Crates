package me.darkness.crates.crate.battle;

import java.util.UUID;

public final class BattleSession {

    private final UUID owner;
    private final UUID opponent;

    private String crateName;
    private int amount;

    public BattleSession(UUID owner, UUID opponent) {
        this.owner = owner;
        this.opponent = opponent;
    }

    public UUID getOwner() {
        return owner;
    }

    public UUID getOpponent() {
        return opponent;
    }

    public String getCrateName() {
        return crateName;
    }

    public void setCrateName(String crateName) {
        this.crateName = crateName;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}

