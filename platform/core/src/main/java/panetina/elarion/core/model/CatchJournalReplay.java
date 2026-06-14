package panetina.elarion.core.model;

import java.util.List;
import java.util.Objects;

public record CatchJournalReplay(
        List<AcceptedCatchRecord> records,
        CatchJournalCheckpoint nextCheckpoint,
        int linesScanned,
        boolean hasMore
) {
    public CatchJournalReplay {
        records = List.copyOf(records);
        Objects.requireNonNull(nextCheckpoint, "nextCheckpoint");
        if (linesScanned < 0) {
            throw new IllegalArgumentException("linesScanned must not be negative");
        }
    }
}
