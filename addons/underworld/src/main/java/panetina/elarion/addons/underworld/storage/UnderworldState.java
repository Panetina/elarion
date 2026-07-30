package panetina.elarion.addons.underworld.storage;

import panetina.elarion.addons.underworld.model.BanishmentRecord;
import panetina.elarion.addons.underworld.model.CorpseRecord;
import panetina.elarion.addons.underworld.model.ElarionDeathType;
import panetina.elarion.addons.underworld.model.InventorySnapshot;
import panetina.elarion.addons.underworld.model.SoulState;
import panetina.elarion.addons.underworld.model.StoredItemStack;
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
        corpses.entrySet().removeIf(entry -> !validKey(entry.getKey()) || entry.getValue() == null);
        corpses.forEach((corpseId, corpse) -> normalizeCorpse(corpseId, corpse));
        sessions.entrySet().removeIf(entry -> !validKey(entry.getKey()) || entry.getValue() == null);
        sessions.forEach((playerId, session) -> {
            session.playerId = valueOrKey(session.playerId, playerId);
            session.corpseId = clean(session.corpseId);
            if (session.deathType == null) {
                session.deathType = ElarionDeathType.UNKNOWN;
            }
        });
        souls.entrySet().removeIf(entry -> !validKey(entry.getKey()) || entry.getValue() == null);
        souls.forEach((playerId, soul) -> soul.playerId = valueOrKey(soul.playerId, playerId));
        recoveryVaults.entrySet().removeIf(entry -> !validKey(entry.getKey()) || entry.getValue() == null);
        recoveryVaults.values().forEach(items -> items.removeIf(item -> item == null));
        banishments.entrySet().removeIf(entry -> !validKey(entry.getKey()) || entry.getValue() == null);
        banishments.forEach((playerId, record) -> {
            record.normalized();
            record.playerId = valueOrKey(record.playerId, playerId);
        });
        afterlifeInventories.entrySet().removeIf(entry -> !validKey(entry.getKey()) || entry.getValue() == null);
        livingInventories.entrySet().removeIf(entry -> !validKey(entry.getKey()) || entry.getValue() == null);
        afterlifeInventories.values().forEach(InventorySnapshot::normalized);
        livingInventories.values().forEach(InventorySnapshot::normalized);
        return this;
    }

    private static void normalizeCorpse(String corpseId, CorpseRecord corpse) {
        corpse.corpseId = valueOrKey(corpse.corpseId, corpseId);
        corpse.victimId = clean(corpse.victimId);
        corpse.victimName = clean(corpse.victimName);
        corpse.killerId = clean(corpse.killerId);
        corpse.worldId = clean(corpse.worldId);
        corpse.victimRealmId = clean(corpse.victimRealmId);
        corpse.tombstoneVariant = clean(corpse.tombstoneVariant);
        if (corpse.deathType == null) {
            corpse.deathType = ElarionDeathType.UNKNOWN;
        }
        if (corpse.protectedVictimItems == null) corpse.protectedVictimItems = new ArrayList<>();
        else corpse.protectedVictimItems.removeIf(item -> item == null);
        if (corpse.pvpLootItems == null) corpse.pvpLootItems = new ArrayList<>();
        else corpse.pvpLootItems.removeIf(item -> item == null);
    }

    private static String valueOrKey(String value, String key) {
        String clean = clean(value);
        return clean.isBlank() ? key : clean;
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }

    private static boolean validKey(String key) {
        return key != null && !key.isBlank();
    }
}
