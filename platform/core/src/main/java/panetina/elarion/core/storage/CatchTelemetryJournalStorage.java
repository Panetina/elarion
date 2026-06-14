package panetina.elarion.core.storage;

import panetina.elarion.core.model.AcceptedCatchRecord;
import panetina.elarion.core.model.CatchJournalCheckpoint;
import panetina.elarion.core.model.CatchJournalReplay;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public final class CatchTelemetryJournalStorage {
    public synchronized void append(Path elarionRoot, AcceptedCatchRecord record) throws IOException {
        Path file = CatchTelemetryJournalCodec.journalPath(
                elarionRoot, record.actorId(), record.occurredAt());
        Files.createDirectories(file.getParent());
        Files.writeString(
                file,
                CatchTelemetryJournalCodec.encode(record) + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE)) {
            channel.force(false);
        }
    }

    public synchronized CatchJournalReplay replay(
            Path elarionRoot,
            UUID actorId,
            CatchJournalCheckpoint checkpoint,
            Map<UUID, AcceptedCatchRecord> seenRecords,
            int maxLines
    ) throws IOException {
        if (elarionRoot == null) throw new NullPointerException("elarionRoot");
        if (actorId == null) throw new NullPointerException("actorId");
        if (checkpoint == null) throw new NullPointerException("checkpoint");
        if (seenRecords == null) throw new NullPointerException("seenRecords");
        if (maxLines <= 0) throw new IllegalArgumentException("maxLines must be positive");

        List<Path> files = journalFiles(elarionRoot, actorId, checkpoint);
        if (files.isEmpty()) {
            return new CatchJournalReplay(List.of(), checkpoint, 0, false);
        }

        List<AcceptedCatchRecord> records = new ArrayList<>();
        Map<UUID, AcceptedCatchRecord> pageRecords = new LinkedHashMap<>();
        int linesScanned = 0;
        CatchJournalCheckpoint cursor = checkpoint;
        boolean hasMore = false;

        outer:
        for (int fileIndex = 0; fileIndex < files.size(); fileIndex++) {
            Path file = files.get(fileIndex);
            String month = month(file);
            long skipLines = month.equals(checkpoint.month()) ? checkpoint.processedLines() : 0;
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                long lineNumber = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    if (lineNumber <= skipLines) continue;
                    if (linesScanned >= maxLines) {
                        hasMore = true;
                        break outer;
                    }
                    linesScanned++;
                    cursor = new CatchJournalCheckpoint(month, lineNumber);
                    if (line.isBlank()) continue;
                    AcceptedCatchRecord record = CatchTelemetryJournalCodec.decode(
                            file + ":" + lineNumber, line);
                    if (!actorId.equals(record.actorId())) {
                        throw new CatchTelemetryFormatException(
                                file + ":" + lineNumber + ": actorId does not match journal partition");
                    }
                    AcceptedCatchRecord previous = pageRecords.get(record.eventId());
                    if (previous == null) previous = seenRecords.get(record.eventId());
                    if (previous != null && !previous.equals(record)) {
                        throw new CatchTelemetryFormatException(
                                file + ":" + lineNumber + ": eventId conflicts with an earlier catch record");
                    }
                    if (previous == null) {
                        pageRecords.put(record.eventId(), record);
                        records.add(record);
                    }
                }
            }
        }

        seenRecords.putAll(pageRecords);
        return new CatchJournalReplay(records, cursor, linesScanned, hasMore);
    }

    private static List<Path> journalFiles(
            Path elarionRoot,
            UUID actorId,
            CatchJournalCheckpoint checkpoint
    ) throws IOException {
        Path directory = elarionRoot.resolve("catch-telemetry")
                .resolve("journal")
                .resolve(actorId.toString());
        if (Files.notExists(directory)) return List.of();
        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches("\\d{4}-\\d{2}\\.jsonl"))
                    .filter(path -> checkpoint.isStart() || month(path).compareTo(checkpoint.month()) >= 0)
                    .sorted(Comparator.comparing(CatchTelemetryJournalStorage::month))
                    .toList();
        }
    }

    private static String month(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.substring(0, fileName.length() - ".jsonl".length());
    }
}
