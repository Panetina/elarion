package panetina.elarion.addons.underworld.storage;

import panetina.elarion.addons.underworld.model.CorpseRecord;
import panetina.elarion.addons.underworld.model.BanishmentRecord;
import panetina.elarion.addons.underworld.model.SoulState;
import panetina.elarion.addons.underworld.model.StoredItemStack;
import panetina.elarion.addons.underworld.model.InventorySnapshot;
import panetina.elarion.addons.underworld.model.UnderworldSession;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class UnderworldState {
    public static final int CURRENT_SCHEMA_VERSION = 3;

    public int schemaVersion = CURRENT_SCHEMA_VERSION;
    public Map<String, CorpseRecord> corpses = new LinkedHashMap<>();
    public Map<String, UnderworldSession> sessions = new LinkedHashMap<>();
    public Map<String, SoulState> souls = new LinkedHashMap<>();
    public Map<String, List<StoredItemStack>> recoveryVaults = new LinkedHashMap<>();
    public Map<String, BanishmentRecord> banishments = new LinkedHashMap<>();
    /** Items usable only while the player is in an Underworld session or banishment. */
    public Map<String, InventorySnapshot> afterlifeInventories = new LinkedHashMap<>();
    /** Living inventory held while a non-death Underworld transfer is active. */
    public Map<String, InventorySnapshot> livingInventories = new LinkedHashMap<>();

    public UnderworldState normalized() {
        if (schemaVersion <= 0) schemaVersion = 1;
        if (schemaVersion == 1 || schemaVersion == 2) schemaVersion = CURRENT_SCHEMA_VERSION;
        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
            throw new IllegalStateException("Unsupported underworld state schema " + schemaVersion);
        }
        if (corpses == null) corpses = new LinkedHashMap<>();
        if (sessions == null) sessions = new LinkedHashMap<>();
        if (souls == null) souls = new LinkedHashMap<>();
        if (recoveryVaults == null) recoveryVaults = new LinkedHashMap<>();
        if (banishments == null) banishments = new LinkedHashMap<>();
        if (afterlifeInventories == null) afterlifeInventories = new LinkedHashMap<>();
        if (livingInventories == null) livingInventories = new LinkedHashMap<>();
        afterlifeInventories.values().removeIf(snapshot -> snapshot == null);
        livingInventories.values().removeIf(snapshot -> snapshot == null);
        afterlifeInventories.values().forEach(InventorySnapshot::normalized);
        livingInventories.values().forEach(InventorySnapshot::normalized);
        banishments.values().forEach(record -> {
            if (record != null) record.normalized();
        });
        return this;
    }
}
