package panetina.elarion.core.model;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

public record CatchJournalCheckpoint(String month, long processedLines) {
    public static final CatchJournalCheckpoint START = new CatchJournalCheckpoint("", 0);

    public CatchJournalCheckpoint {
        month = month == null ? "" : month.trim();
        if (!month.isEmpty()) {
            try {
                YearMonth.parse(month);
            } catch (DateTimeParseException exception) {
                throw new IllegalArgumentException("month must use yyyy-MM", exception);
            }
        }
        if (processedLines < 0) {
            throw new IllegalArgumentException("processedLines must not be negative");
        }
        if (month.isEmpty() && processedLines != 0) {
            throw new IllegalArgumentException("start checkpoint cannot contain processed lines");
        }
    }

    public boolean isStart() {
        return month.isEmpty();
    }
}
