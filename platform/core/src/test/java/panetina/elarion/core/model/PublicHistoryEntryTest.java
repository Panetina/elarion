package panetina.elarion.core.model;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PublicHistoryEntryTest {
    @Test
    void metadataFlowsFromHistoryEventThroughIndexAndArchiveViews() {
        HistoryEvent event = HistoryEvent.create(
                "government",
                "proposal-approved",
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "proposal",
                "proposal-1",
                "realm1",
                Map.of("title", "Harbor Tax Reform", "category", "law"),
                "A civic proposal was approved.");

        HistoryIndexEntry index = HistoryIndexEntry.from(event);
        ChronicleEntry chronicle = ChronicleEntry.from(index);
        PublicHistoryEntry live = PublicHistoryEntry.fromIndex(index);
        PublicHistoryEntry archived = PublicHistoryEntry.fromArchive(chronicle);

        assertEquals("Harbor Tax Reform", index.metadata().get("title"));
        assertEquals("law", chronicle.metadata().get("category"));
        assertEquals(index.metadata(), live.metadata());
        assertEquals(index.metadata(), archived.metadata());
    }

    @Test
    void oldEntriesWithoutMetadataDefaultToEmptyMetadata() {
        PublicHistoryEntry entry = new PublicHistoryEntry(
                UUID.randomUUID(), 1L, "live-index", "government", "event",
                null, "realm", "realm1", "realm1", "text");

        assertTrue(entry.metadata().isEmpty());
    }
}
