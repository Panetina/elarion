package panetina.elarion.core.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class CharacterLifecycleRecordTest {
    @Test
    void existingAccountsRequireOneMigrationConfirmation() {
        UUID accountId = UUID.randomUUID();
        CharacterLifecycleRecord record = CharacterLifecycleRecord.migration(accountId);

        assertEquals(accountId.toString(), record.accountId);
        assertEquals(1, record.generation);
        assertEquals(CharacterLifecycleStatus.MIGRATION_REQUIRED, record.status);
        assertFalse(record.nonce.isBlank());
    }

    @Test
    void newAccountsRequireFreshCharacterCreation() {
        CharacterLifecycleRecord record = CharacterLifecycleRecord.newAccount(UUID.randomUUID());

        assertEquals(0, record.generation);
        assertEquals(CharacterLifecycleStatus.CREATION_REQUIRED, record.status);
    }
}
