package me.darkness.crates.crate.edit;

import me.darkness.crates.crate.Crate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EditSessionManager {

    private final Map<UUID, EditSession.Session> sessions = new ConcurrentHashMap<>();

    public EditSession.Session start(UUID playerId, Crate crate, int rows) {
        EditSession.Session session = new EditSession.Session(crate, rows);
        sessions.put(playerId, session);
        return session;
    }

    public EditSession.Session get(UUID playerId) {
        return sessions.get(playerId);
    }

    public void end(UUID playerId) {
        sessions.remove(playerId);
    }
}
