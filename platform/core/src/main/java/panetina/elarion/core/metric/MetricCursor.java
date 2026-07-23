package panetina.elarion.core.metric;

import java.util.UUID;

public record MetricCursor(long revision, long fixedPointValue, UUID actorId) {
    public MetricCursor {
        if (revision < 0 || actorId == null) throw new IllegalArgumentException("metric cursor is invalid");
    }
}
