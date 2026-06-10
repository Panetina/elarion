package panetina.elarion.addons.optimization;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

final class WorldTrendTracker {
    private static final int MAX_SAMPLES = 10;
    private final Map<String, ArrayDeque<Point>> samplesByWorld = new HashMap<>();

    synchronized Trend record(String worldId, int loadedChunks, int entities, int blockEntities) {
        ArrayDeque<Point> samples = samplesByWorld.computeIfAbsent(worldId, ignored -> new ArrayDeque<>());
        samples.addLast(new Point(loadedChunks, entities, blockEntities));
        while (samples.size() > MAX_SAMPLES) {
            samples.removeFirst();
        }
        Point first = samples.getFirst();
        Point last = samples.getLast();
        return new Trend(
                samples.size(),
                last.loadedChunks() - first.loadedChunks(),
                last.entities() - first.entities(),
                last.blockEntities() - first.blockEntities());
    }

    record Trend(int samples, int loadedChunkTrend, int entityTrend, int blockEntityTrend) {
    }

    private record Point(int loadedChunks, int entities, int blockEntities) {
    }
}
