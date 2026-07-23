package panetina.elarion.core.metric;

import java.util.UUID;

public record MetricRankEntry(UUID actorId, long fixedPointValue, long rank) {
    public MetricRankEntry {
        if (actorId == null || rank <= 0) throw new IllegalArgumentException("metric rank entry is invalid");
    }
}
