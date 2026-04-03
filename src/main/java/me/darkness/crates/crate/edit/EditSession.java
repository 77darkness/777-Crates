package me.darkness.crates.crate.edit;

import me.darkness.crates.crate.Crate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EditSession {

    private EditSession() {}

    public static final class RewardSettings {
        public final double chance;
        public final List<String> commands;
        public final boolean giveItem;

        public RewardSettings(double chance, List<String> commands, boolean giveItem) {
            this.chance = chance;
            this.commands = commands == null ? List.of() : List.copyOf(commands);
            this.giveItem = giveItem;
        }
    }

    public static final class Session {
        private final Crate crate;
        private final int rows;
        private volatile Integer editingChanceSlot;
        private volatile Integer editingCommandSlot;
        private volatile boolean reopening = false;
        public final Map<Integer, RewardSettings> rewardSettings = new ConcurrentHashMap<>();

        public Session(Crate crate, int rows) {
            this.crate = crate;
            this.rows = rows;
        }

        public Crate crate() { return crate; }
        public int rows() { return rows; }
        public Integer getEditingChanceSlot() { return editingChanceSlot; }
        public void setEditingChanceSlot(Integer slot) { this.editingChanceSlot = slot; }
        public Integer getEditingCommandSlot() { return editingCommandSlot; }
        public void setEditingCommandSlot(Integer slot) { this.editingCommandSlot = slot; }
        public boolean isReopening() { return reopening; }
        public void setReopening(boolean reopening) { this.reopening = reopening; }
    }
}