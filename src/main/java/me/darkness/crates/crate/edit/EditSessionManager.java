package me.darkness.crates.crate.edit;

import me.darkness.crates.crate.Crate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EditSessionManager {

    private final Map<UUID, EditSession.Session> sessions = new ConcurrentHashMap<>();

    public void start(UUID playerId, Crate crate, int rows) {
        sessions.put(playerId, new EditSession.Session(crate, rows));
    }

    public EditSession.Session get(UUID playerId) {
        return sessions.get(playerId);
    }

    public void end(UUID playerId) {
        sessions.remove(playerId);
    }

    public void startChanceEdit(UUID playerId, int slot) {
        withValidSession(playerId, slot, s -> s.setEditingChanceSlot(slot));
    }

    public void endChanceEdit(UUID playerId) {
        EditSession.Session s = get(playerId);
        if (s != null) s.setEditingChanceSlot(null);
    }

    public Integer getEditingChanceSlot(UUID playerId) {
        EditSession.Session s = get(playerId);
        return s != null ? s.getEditingChanceSlot() : null;
    }

    public void startCommandEdit(UUID playerId, int slot) {
        withValidSession(playerId, slot, s -> s.setEditingCommandSlot(slot));
    }

    public void endCommandEdit(UUID playerId) {
        EditSession.Session s = get(playerId);
        if (s != null) s.setEditingCommandSlot(null);
    }

    public Integer getEditingCommandSlot(UUID playerId) {
        EditSession.Session s = get(playerId);
        return s != null ? s.getEditingCommandSlot() : null;
    }

    public void putRewardSettings(UUID playerId, int slot, double chance, List<String> commands, boolean giveItem) {
        withValidSession(playerId, slot, s -> s.rewardSettings.put(slot, new EditSession.RewardSettings(chance, commands, giveItem)));
    }

    public EditSession.RewardSettings getRewardSettings(UUID playerId, int slot) {
        EditSession.Session s = get(playerId);
        return isValid(s, slot) ? s.rewardSettings.get(slot) : null;
    }

    private void withValidSession(UUID playerId, int slot, java.util.function.Consumer<EditSession.Session> action) {
        EditSession.Session s = get(playerId);
        if (isValid(s, slot)) action.accept(s);
    }

    private boolean isValid(EditSession.Session session, int slot) {
        return session != null && slot >= 0 && slot < session.rows() * 9;
    }
}
