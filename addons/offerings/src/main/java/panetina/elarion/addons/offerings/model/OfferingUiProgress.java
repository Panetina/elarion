package panetina.elarion.addons.offerings.model;

import java.util.List;

public record OfferingUiProgress(long current, long required) {
    public static OfferingUiProgress from(List<OfferingProgress.Row> rows) {
        long current = 0L;
        long required = 0L;
        for (OfferingProgress.Row row : rows) {
            required += row.required();
            current += Math.min(row.current(), row.required());
        }
        return new OfferingUiProgress(current, required);
    }
}
