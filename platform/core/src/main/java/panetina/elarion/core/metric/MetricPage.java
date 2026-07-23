package panetina.elarion.core.metric;

import java.util.List;

public record MetricPage(long revision, List<MetricRankEntry> entries, MetricCursor nextCursor) {
    public MetricPage {
        if (revision < 0) throw new IllegalArgumentException("metric page revision is invalid");
        entries = List.copyOf(entries);
        if (entries.size() > 100) throw new IllegalArgumentException("metric page exceeds 100 entries");
    }
}
