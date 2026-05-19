package me.darkness.crates.crate.edit;

import me.darkness.crates.crate.Crate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class EditSession {

    private EditSession() {}

    public enum EditType { CHANCE, COMMAND }

    public record EditTarget(int slot, EditType type) {}

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
        private volatile EditTarget activeEdit;
        private volatile boolean reopening = false;
        private final Map<Integer, RewardSettings> rewardSettings = new ConcurrentHashMap<>();
        private final Set<Integer> blockedSlots = ConcurrentHashMap.newKeySet();

        public Session(Crate crate, int rows) {
            this.crate = crate;
            this.rows = rows;
        }

        public Crate crate() { return crate; }
        public int rows() { return rows; }

        public void startEdit(int slot, EditType type) {
            if (isValidSlot(slot)) activeEdit = new EditTarget(slot, type);
        }
        public void endEdit() { activeEdit = null; }
        public EditTarget getActiveEdit() { return activeEdit; }

        public boolean isReopening() { return reopening; }
        public void setReopening(boolean reopening) { this.reopening = reopening; }

        public void putRewardSettings(int slot, double chance, List<String> commands, boolean giveItem) {
            if (isValidSlot(slot))
                rewardSettings.put(slot, new RewardSettings(chance, commands, giveItem));
        }
        public RewardSettings getRewardSettings(int slot) { return rewardSettings.get(slot); }

        public void addBlockedSlot(int slot) { blockedSlots.add(slot); }
        public boolean isBlockedSlot(int slot) { return blockedSlots.contains(slot); }
        public Set<Integer> getBlockedSlots() { return Set.copyOf(blockedSlots); }

        public boolean isValidSlot(int slot) { return slot >= 0 && slot < rows * 9; }
        public Map<Integer, RewardSettings> snapshotRewardSettings() { return Map.copyOf(rewardSettings); }
    }
}