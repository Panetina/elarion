package panetina.elarion.core.model;

import java.util.UUID;
import java.util.LinkedHashSet;
import java.util.Set;

public final class CharacterLifecycleRecord {
    public String accountId = "";
    public String activeCharacterId = "";
    public int generation = 1;
    public String biography = "";
    public CharacterLifecycleStatus status = CharacterLifecycleStatus.MIGRATION_REQUIRED;
    public long eligibleAt;
    public String nonce = "";
    public String resetReason = "";
    public Set<String> completedResetSteps = new LinkedHashSet<>();
    public long updatedAt;

    public static CharacterLifecycleRecord migration(UUID accountId) {
        CharacterLifecycleRecord record = new CharacterLifecycleRecord();
        record.accountId = accountId.toString();
        record.activeCharacterId = UUID.randomUUID().toString();
        record.generation = 1;
        record.status = CharacterLifecycleStatus.MIGRATION_REQUIRED;
        record.nonce = UUID.randomUUID().toString();
        record.updatedAt = System.currentTimeMillis();
        return record;
    }

    public static CharacterLifecycleRecord newAccount(UUID accountId) {
        CharacterLifecycleRecord record = migration(accountId);
        record.activeCharacterId = "";
        record.generation = 0;
        record.status = CharacterLifecycleStatus.CREATION_REQUIRED;
        return record;
    }
}
