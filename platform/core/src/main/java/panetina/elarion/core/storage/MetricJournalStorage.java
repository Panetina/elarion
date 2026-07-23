package panetina.elarion.core.storage;

import panetina.elarion.core.metric.MetricProjectionService;
import panetina.elarion.core.metric.MetricProjectionState;
import panetina.elarion.core.metric.MetricUpdateBatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/** Source-partitioned append-only metric journal; replay is startup-only. */
public final class MetricJournalStorage {
    public synchronized void append(Path elarionRoot, MetricUpdateBatch batch) throws IOException {
        Path file = journalPath(elarionRoot, batch);
        Files.createDirectories(file.getParent());
        Files.writeString(file, MetricPersistenceCodec.encodeBatch(batch) + System.lineSeparator(),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE)) {
            channel.force(false);
        }
    }

    public synchronized long replay(
            Path elarionRoot,
            MetricProjectionState restoredState,
            MetricProjectionService projections
    ) throws IOException {
        Path root = elarionRoot.resolve("metrics").resolve("journal");
        if (Files.notExists(root)) return 0;
        Map<Partition, Long> checkpoints = new HashMap<>();
        for (MetricProjectionState.PartitionState partition : restoredState.partitions()) {
            checkpoints.put(new Partition(partition.sourceSystem().toString(), partition.sourcePartition()),
                    partition.sequence());
        }
        long applied = 0;
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path file : paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jsonl"))
                    .sorted().toList()) {
                try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    long lineNumber = 0;
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lineNumber++;
                        if (line.isBlank()) continue;
                        MetricUpdateBatch batch = MetricPersistenceCodec.decodeBatch(
                                file + ":" + lineNumber, line);
                        long checkpoint = checkpoints.getOrDefault(
                                new Partition(batch.sourceSystem().toString(), batch.sourcePartition()), 0L);
                        if (batch.sequence() < checkpoint) continue;
                        if (projections.apply(batch)) applied++;
                    }
                }
            }
        }
        return applied;
    }

    /** Removes only metric journal segments after their state is durably checkpointed. */
    public synchronized long compact(Path elarionRoot) throws IOException {
        Path root = elarionRoot.resolve("metrics").resolve("journal");
        if (Files.notExists(root)) return 0;
        long deleted = 0;
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path file : paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jsonl"))
                    .toList()) {
                Files.deleteIfExists(file);
                deleted++;
            }
        }
        return deleted;
    }

    static Path journalPath(Path elarionRoot, MetricUpdateBatch batch) {
        String hash = sha256(batch.sourceSystem() + "\n" + batch.sourcePartition());
        return elarionRoot.resolve("metrics").resolve("journal")
                .resolve(batch.sourceSystem().getNamespace())
                .resolve(batch.sourceSystem().getPath())
                .resolve(hash + ".jsonl");
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private record Partition(String sourceSystem, String sourcePartition) {
    }
}
