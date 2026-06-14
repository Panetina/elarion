package panetina.elarion.addons.offerings.model;

import java.util.List;

public record OfferingProgress(
        String instanceId,
        boolean complete,
        List<Row> rows
) {
    public record Row(String key, long current, long required, boolean complete) {
    }
}
