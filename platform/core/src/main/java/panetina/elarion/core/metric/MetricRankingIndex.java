package panetina.elarion.core.metric;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.UUID;

final class MetricRankingIndex {
    private final MetricSortDirection direction;
    private final Comparator<ActorValue> comparator;
    private final NavigableSet<ActorValue> ordered;
    private final Map<UUID, Long> values = new HashMap<>();
    private final MetricValueCountIndex valueCounts = new MetricValueCountIndex();
    private long revision;

    MetricRankingIndex(MetricDescriptor descriptor) {
        direction = descriptor.sortDirection();
        Comparator<ActorValue> valueComparator = Comparator.comparingLong(ActorValue::value);
        if (direction == MetricSortDirection.DESCENDING) valueComparator = valueComparator.reversed();
        comparator = valueComparator.thenComparing(ActorValue::actorId);
        ordered = new TreeSet<>(comparator);
    }

    Long value(UUID actorId) {
        return values.get(actorId);
    }

    boolean apply(UUID actorId, long next) {
        Long previous = values.get(actorId);
        if (previous != null && previous == next) return false;
        if (previous != null) {
            ordered.remove(new ActorValue(actorId, previous));
            valueCounts.add(previous, -1);
        }
        values.put(actorId, next);
        ordered.add(new ActorValue(actorId, next));
        valueCounts.add(next, 1);
        revision = Math.addExact(revision, 1);
        return true;
    }

    MetricRankEntry player(UUID actorId) {
        Long value = values.get(actorId);
        return value == null ? null : entry(new ActorValue(actorId, value));
    }

    MetricPage top(int limit) {
        return page(ordered, limit);
    }

    MetricPage after(MetricCursor cursor, int limit) {
        if (cursor.revision() != revision) throw new IllegalArgumentException("metric cursor revision is stale");
        return page(ordered.tailSet(new ActorValue(cursor.actorId(), cursor.fixedPointValue()), false), limit);
    }

    MetricPage around(UUID actorId, int radius) {
        Long value = values.get(actorId);
        if (value == null) return new MetricPage(revision, List.of(), null);
        ActorValue center = new ActorValue(actorId, value);
        List<ActorValue> before = new ArrayList<>(radius);
        ActorValue cursor = center;
        for (int index = 0; index < radius; index++) {
            cursor = ordered.lower(cursor);
            if (cursor == null) break;
            before.add(cursor);
        }
        java.util.Collections.reverse(before);
        List<MetricRankEntry> entries = new ArrayList<>(before.size() + radius + 1);
        before.forEach(valueEntry -> entries.add(entry(valueEntry)));
        entries.add(entry(center));
        cursor = center;
        for (int index = 0; index < radius; index++) {
            cursor = ordered.higher(cursor);
            if (cursor == null) break;
            entries.add(entry(cursor));
        }
        return new MetricPage(revision, entries, null);
    }

    long revision() {
        return revision;
    }

    Map<UUID, Long> valuesSnapshot() {
        return Map.copyOf(values);
    }

    void restore(Map<UUID, Long> restoredValues, long restoredRevision) {
        if (!values.isEmpty() || revision != 0) throw new IllegalStateException("metric index is not empty");
        if (restoredRevision < restoredValues.size()) {
            throw new IllegalArgumentException("metric index revision precedes its materialized actor count");
        }
        restoredValues.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> apply(entry.getKey(), entry.getValue()));
        revision = restoredRevision;
    }

    private MetricPage page(Iterable<ActorValue> source, int limit) {
        List<MetricRankEntry> entries = new ArrayList<>(limit);
        ActorValue last = null;
        for (ActorValue value : source) {
            if (entries.size() == limit) break;
            entries.add(entry(value));
            last = value;
        }
        MetricCursor next = last == null || entries.size() < limit
                ? null
                : new MetricCursor(revision, last.value(), last.actorId());
        return new MetricPage(revision, entries, next);
    }

    private MetricRankEntry entry(ActorValue value) {
        long better = direction == MetricSortDirection.DESCENDING
                ? valueCounts.countGreater(value.value())
                : valueCounts.countLess(value.value());
        return new MetricRankEntry(value.actorId(), value.value(), Math.addExact(better, 1));
    }

    private record ActorValue(UUID actorId, long value) {
    }
}
