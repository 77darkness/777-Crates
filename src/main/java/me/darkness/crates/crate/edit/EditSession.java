package me.darkness.crates.crate.edit;

import me.darkness.crates.crate.Crate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EditSession {

    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    private EditSession() {}

    public static void start(UUID playerId, Crate crate, int rows) {
        SESSIONS.put(playerId, new Session(crate, rows));
    }

    public static Session get(UUID playerId) {
        return SESSIONS.get(playerId);
    }

    public static void end(UUID playerId) {
        SESSIONS.remove(playerId);
    }

    public static void startChanceEdit(UUID playerId, int slot) {
        Session s = get(playerId);
        if (isValidSlot(s, slot)) {
            s.setEditingChanceSlot(slot);
        }
    }

    public static void endChanceEdit(UUID playerId) {
        Session s = get(playerId);
        if (s != null) {
            s.setEditingChanceSlot(null);
        }
    }

    public static Integer getEditingChanceSlot(UUID playerId) {
        Session s = get(playerId);
        return s != null ? s.getEditingChanceSlot() : null;
    }

    public static void startCommandEdit(UUID playerId, int slot) {
        Session s = get(playerId);
        if (isValidSlot(s, slot)) {
            s.setEditingCommandSlot(slot);
        }
    }

    public static void endCommandEdit(UUID playerId) {
        Session s = get(playerId);
        if (s != null) {
            s.setEditingCommandSlot(null);
        }
    }

    public static Integer getEditingCommandSlot(UUID playerId) {
        Session s = get(playerId);
        return s != null ? s.getEditingCommandSlot() : null;
    }

    public static void putRewardSettings(UUID playerId, int slot, double chance, List<String> commands, boolean giveItem) {
        Session s = get(playerId);
        if (isValidSlot(s, slot)) {
            s.rewardSettings.put(slot, new RewardSettings(chance, commands, giveItem));
        }
    }

    public static RewardSettings getRewardSettings(UUID playerId, int slot) {
        Session s = get(playerId);
        return isValidSlot(s, slot) ? s.rewardSettings.get(slot) : null;
    }

    private static boolean isValidSlot(Session session, int slot) {
        return session != null && slot >= 0 && slot < session.rows * 9;
    }

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
    }
}