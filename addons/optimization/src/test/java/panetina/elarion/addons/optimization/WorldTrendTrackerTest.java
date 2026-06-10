package panetina.elarion.addons.optimization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class WorldTrendTrackerTest {
    @Test
    void tracksTrendAcrossRecentSamples() {
        WorldTrendTracker tracker = new WorldTrendTracker();

        tracker.record("elarion:lobby", 10, 20, 5);
        WorldTrendTracker.Trend trend = tracker.record("elarion:lobby", 12, 27, 9);

        assertEquals(2, trend.samples());
        assertEquals(2, trend.loadedChunkTrend());
        assertEquals(7, trend.entityTrend());
        assertEquals(4, trend.blockEntityTrend());
    }

    @Test
    void keepsWindowBoundedToTenSamples() {
        WorldTrendTracker tracker = new WorldTrendTracker();

        for (int index = 0; index < 12; index++) {
            tracker.record("elarion:lobby", index, index * 2, index * 3);
        }
        WorldTrendTracker.Trend trend = tracker.record("elarion:lobby", 12, 24, 36);

        assertEquals(10, trend.samples());
        assertEquals(9, trend.loadedChunkTrend());
        assertEquals(18, trend.entityTrend());
        assertEquals(27, trend.blockEntityTrend());
    }
}
