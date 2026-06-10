package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IdentitySyncBatcherTest {
    @Test
    void repeatedSubjectRequestsAreDeduplicated() {
        IdentitySyncBatcher batcher = new IdentitySyncBatcher();
        UUID subject = UUID.randomUUID();

        batcher.requestSubject(subject);
        batcher.requestSubject(subject);

        IdentitySyncBatcher.Intent intent = batcher.drain();
        assertEquals(1, intent.subjects().size());
        assertTrue(intent.subjects().contains(subject));
        assertTrue(batcher.drain().empty());
    }

    @Test
    void fullSyncClearsSpecificRequests() {
        IdentitySyncBatcher batcher = new IdentitySyncBatcher();
        batcher.requestViewer(UUID.randomUUID());
        batcher.requestSubject(UUID.randomUUID());

        batcher.requestFull();

        IdentitySyncBatcher.Intent intent = batcher.drain();
        assertTrue(intent.full());
        assertTrue(intent.viewers().isEmpty());
        assertTrue(intent.subjects().isEmpty());
    }
}
