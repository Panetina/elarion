package panetina.elarion.addons.underworld.storage;

import panetina.elarion.addons.underworld.model.CorpseRecord;
import panetina.elarion.addons.underworld.model.SoulState;
import panetina.elarion.addons.underworld.model.StoredItemStack;
import panetina.elarion.addons.underworld.model.UnderworldSession;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class UnderworldState {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public int schemaVersion = CURRENT_SCHEMA_VERSION;
    public Map<String, CorpseRecord> corpses = new LinkedHashMap<>();
    public Map<String, UnderworldSession> sessions = new LinkedHashMap<>();
    public Map<String, SoulState> souls = new LinkedHashMap<>();
    public Map<String, List<StoredItemStack>> recoveryVaults = new LinkedHashMap<>();

    public UnderworldState normalized() {
        if (schemaVersion <= 0) schemaVersion = CURRENT_SCHEMA_VERSION;
        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
            throw new IllegalStateException("Unsupported underworld state schema " + schemaVersion);
        }
        if (corpses == null) corpses = new LinkedHashMap<>();
        if (sessions == null) sessions = new LinkedHashMap<>();
        if (souls == null) souls = new LinkedHashMap<>();
        if (recoveryVaults == null) recoveryVaults = new LinkedHashMap<>();
        return this;
    }
}
