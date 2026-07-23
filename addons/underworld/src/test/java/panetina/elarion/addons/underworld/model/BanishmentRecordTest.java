package panetina.elarion.addons.underworld.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BanishmentRecordTest {
    @Test
    void timedAndPermanentSentencesUseExplicitExpirySemantics() {
        BanishmentRecord timed = new BanishmentRecord();
        timed.expiresAt = 2_000L;
        assertTrue(timed.activeAt(1_999L));
        assertFalse(timed.activeAt(2_000L));
        assertEquals(500L, timed.remainingMillis(1_500L));

        BanishmentRecord permanent = new BanishmentRecord();
        permanent.expiresAt = 0L;
        assertTrue(permanent.permanent());
        assertTrue(permanent.activeAt(Long.MAX_VALUE));
        assertEquals(0L, permanent.remainingMillis(1_500L));
    }

    @Test
    void normalizesPersistedTextToWireSafeBounds() {
        BanishmentRecord record = new BanishmentRecord();
        record.reason = "x".repeat(BanishmentRecord.MAX_REASON_LENGTH + 20);
        record.playerName = " Player ";
        record.normalized();

        assertEquals("Player", record.playerName);
        assertEquals(BanishmentRecord.MAX_REASON_LENGTH, record.reason.length());
    }
}
