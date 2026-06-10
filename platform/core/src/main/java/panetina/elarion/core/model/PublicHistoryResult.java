package panetina.elarion.core.model;

import java.util.List;

public record PublicHistoryResult(
        PublicHistoryConsumer consumer,
        int archivesScanned,
        int liveIndexesScanned,
        List<PublicHistoryEntry> entries
) {
    public PublicHistoryResult {
        consumer = consumer == null ? PublicHistoryConsumer.GUI_SEARCH : consumer;
        entries = entries == null ? List.of() : List.copyOf(entries);
    }
}
