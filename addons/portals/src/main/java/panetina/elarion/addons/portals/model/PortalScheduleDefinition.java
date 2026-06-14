package panetina.elarion.addons.portals.model;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public record PortalScheduleDefinition(
        ZoneId zone,
        Instant anchor,
        Duration interval,
        Duration duration,
        List<Duration> warnings,
        boolean continuous
) {
    public PortalScheduleDefinition(
            ZoneId zone,
            Instant anchor,
            Duration interval,
            Duration duration,
            List<Duration> warnings
    ) {
        this(zone, anchor, interval, duration, warnings, false);
    }

    public PortalScheduleDefinition {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public static PortalScheduleDefinition alwaysOpenSchedule() {
        return new PortalScheduleDefinition(
                ZoneId.of("UTC"),
                Instant.EPOCH,
                Duration.ofDays(1),
                Duration.ofHours(1),
                List.of(),
                true);
    }

    public Window windowAt(Instant now) {
        if (continuous) {
            return new Window(Instant.EPOCH, Instant.ofEpochMilli(Long.MAX_VALUE), true);
        }
        if (now.isBefore(anchor)) {
            return new Window(anchor, anchor.plus(duration), false);
        }
        long intervalMillis = interval.toMillis();
        long elapsed = now.toEpochMilli() - anchor.toEpochMilli();
        long index = Math.floorDiv(elapsed, intervalMillis);
        Instant start = anchor.plusMillis(index * intervalMillis);
        Instant end = start.plus(duration);
        if (now.isBefore(start)) return new Window(start, end, false);
        if (now.isBefore(end)) return new Window(start, end, true);
        Instant next = start.plus(interval);
        return new Window(next, next.plus(duration), false);
    }

    public String display(Instant instant) {
        return ZonedDateTime.ofInstant(instant, zone).toString();
    }

    public record Window(Instant start, Instant end, boolean active) {
    }
}
