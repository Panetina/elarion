package panetina.elarion.core.storage;

import panetina.elarion.core.model.CharacterArchiveRecord;
import panetina.elarion.core.model.CharacterLifecycleRecord;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class CharacterLifecycleState {
    public Map<String, CharacterLifecycleRecord> accounts = new LinkedHashMap<>();
    public Map<String, CharacterArchiveRecord> archives = new LinkedHashMap<>();
    public Set<String> reservedNames = new LinkedHashSet<>();

    public CharacterLifecycleState normalized() {
        LinkedHashMap<String, CharacterLifecycleRecord> normalizedAccounts = new LinkedHashMap<>();
        if (accounts != null) {
            accounts.forEach((rawAccountId, record) -> {
                String accountId = canonicalUuid(rawAccountId);
                if (accountId.isBlank() || record == null) return;
                record.accountId = accountId;
                record.activeCharacterId = clean(record.activeCharacterId);
                record.biography = clean(record.biography);
                record.nonce = clean(record.nonce);
                record.resetReason = clean(record.resetReason);
                record.completedResetSteps = cleanSet(record.completedResetSteps);
                normalizedAccounts.putIfAbsent(accountId, record);
            });
        }
        accounts = normalizedAccounts;

        LinkedHashMap<String, CharacterArchiveRecord> normalizedArchives = new LinkedHashMap<>();
        if (archives != null) {
            archives.forEach((rawCharacterId, archive) -> {
                String characterId = clean(rawCharacterId);
                if (characterId.isBlank() || archive == null) return;
                archive.characterId = characterId;
                archive.accountId = clean(archive.accountId);
                archive.displayName = clean(archive.displayName);
                archive.biography = clean(archive.biography);
                archive.realmId = clean(archive.realmId);
                archive.activeTitleId = clean(archive.activeTitleId);
                archive.reason = clean(archive.reason);
                archive.unlockedTitleIds = cleanList(archive.unlockedTitleIds);
                LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
                if (archive.metadata != null) {
                    archive.metadata.forEach((key, value) -> {
                        if (key != null && value != null) metadata.put(key, value);
                    });
                }
                archive.metadata = metadata;
                normalizedArchives.putIfAbsent(characterId, archive);
            });
        }
        archives = normalizedArchives;
        reservedNames = cleanSet(reservedNames);
        return this;
    }

    private static LinkedHashSet<String> cleanSet(Iterable<String> values) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (values == null) return normalized;
        for (String value : values) {
            String cleaned = clean(value);
            if (!cleaned.isBlank()) normalized.add(cleaned);
        }
        return normalized;
    }

    private static ArrayList<String> cleanList(Iterable<String> values) {
        ArrayList<String> normalized = new ArrayList<>();
        if (values == null) return normalized;
        for (String value : values) {
            String cleaned = clean(value);
            if (!cleaned.isBlank()) normalized.add(cleaned);
        }
        return normalized;
    }

    private static String canonicalUuid(String value) {
        try {
            return UUID.fromString(clean(value)).toString();
        } catch (IllegalArgumentException exception) {
            return "";
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
