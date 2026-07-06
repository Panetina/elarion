package panetina.elarion.core.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ElarionConfigApplyAuditJournal implements ElarionConfigApplyAuditSink {
    public static final int SCHEMA_VERSION = 1;
    private static final int TAIL_CHUNK_BYTES = 8192;

    private final Path file;

    public ElarionConfigApplyAuditJournal(Path file) {
        this.file = Objects.requireNonNull(file, "Config audit journal file is required");
    }

    public static Path journalPath(Path elarionRoot) {
        if (elarionRoot == null) throw new NullPointerException("elarionRoot");
        return elarionRoot.resolve("core")
                .resolve("audit")
                .resolve("config-changes.jsonl");
    }

    public Path file() {
        return file;
    }

    @Override
    public synchronized ElarionConfigApplyAuditSession prepare(ElarionConfigApplyAuditRecord record) {
        Objects.requireNonNull(record, "Config apply audit record is required");
        UUID auditId = UUID.randomUUID();
        append(new Entry(SCHEMA_VERSION, auditId, ElarionConfigApplyAuditPhase.PREPARED,
                System.currentTimeMillis(), record, ""));
        return new JournalSession(auditId, record);
    }

    public synchronized Recovery recoverUnresolvedTail(int maxLines) {
        if (maxLines <= 0) throw new IllegalArgumentException("maxLines must be positive");
        List<String> lines = tailLines(maxLines);
        Map<UUID, PendingAudit> unresolved = new LinkedHashMap<>();
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (line.isBlank()) continue;
            Entry entry = decode(file + ":" + (index + 1), line);
            if (entry.phase() == ElarionConfigApplyAuditPhase.PREPARED) {
                unresolved.put(entry.auditId(), new PendingAudit(
                        entry.auditId(), entry.occurredAt(), entry.record()));
            } else {
                unresolved.remove(entry.auditId());
            }
        }
        return new Recovery(List.copyOf(unresolved.values()), lines.size(), tailTruncated(maxLines));
    }

    synchronized List<Entry> readTailEntries(int maxLines) {
        return tailLines(maxLines).stream()
                .filter(line -> !line.isBlank())
                .map(line -> decode(file.toString(), line))
                .toList();
    }

    private boolean tailTruncated(int maxLines) {
        return Files.exists(file) && tailLines(maxLines + 1).size() > maxLines;
    }

    private void append(Entry entry) {
        try {
            Path parent = file.getParent();
            if (parent != null) Files.createDirectories(parent);
            byte[] bytes = (encode(entry) + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
            try (FileChannel channel = FileChannel.open(
                    file,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND)) {
                channel.write(ByteBuffer.wrap(bytes));
                channel.force(false);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to append config audit journal", exception);
        }
    }

    private List<String> tailLines(int maxLines) {
        if (Files.notExists(file)) return List.of();
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            long size = channel.size();
            if (size == 0L) return List.of();
            long start = tailStart(channel, size, maxLines);
            int length = Math.toIntExact(size - start);
            ByteBuffer buffer = ByteBuffer.allocate(length);
            while (buffer.hasRemaining()) {
                if (channel.read(buffer, start + buffer.position()) < 0) break;
            }
            buffer.flip();
            String tail = StandardCharsets.UTF_8.decode(buffer).toString();
            List<String> lines = tail.lines().toList();
            if (lines.size() <= maxLines) return lines;
            return lines.subList(lines.size() - maxLines, lines.size());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read config audit journal tail", exception);
        }
    }

    private static long tailStart(FileChannel channel, long size, int maxLines) throws IOException {
        long position = size;
        int boundaries = 0;
        while (position > 0L) {
            int read = (int) Math.min(TAIL_CHUNK_BYTES, position);
            position -= read;
            ByteBuffer buffer = ByteBuffer.allocate(read);
            channel.read(buffer, position);
            byte[] bytes = buffer.array();
            for (int index = read - 1; index >= 0; index--) {
                long absolute = position + index;
                if (bytes[index] == '\n') {
                    if (absolute == size - 1L) continue;
                    boundaries++;
                    if (boundaries >= maxLines) return absolute + 1L;
                }
            }
        }
        return 0L;
    }

    private static String encode(Entry entry) {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", entry.schemaVersion());
        root.addProperty("auditId", entry.auditId().toString());
        root.addProperty("phase", entry.phase().name());
        root.addProperty("occurredAt", entry.occurredAt());
        root.add("record", encodeRecord(entry.record()));
        if (!entry.failure().isBlank()) root.addProperty("failure", entry.failure());
        return root.toString();
    }

    static Entry decode(String documentId, String json) {
        String source = documentId == null || documentId.isBlank() ? "<config-audit-journal>" : documentId;
        try {
            JsonElement parsed = JsonParser.parseString(json);
            if (!parsed.isJsonObject()) throw format(source, "root must be a JSON object");
            JsonObject root = parsed.getAsJsonObject();
            int schemaVersion = requiredInt(source, root, "schemaVersion");
            if (schemaVersion != SCHEMA_VERSION) {
                throw format(source, "unsupported schemaVersion " + schemaVersion);
            }
            return new Entry(
                    schemaVersion,
                    requiredUuid(source, root, "auditId"),
                    requiredPhase(source, root, "phase"),
                    requiredLong(source, root, "occurredAt"),
                    decodeRecord(source, requiredObject(source, root, "record")),
                    optionalString(source, root, "failure"));
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw format(source, "invalid config audit journal entry: " + exception.getMessage(), exception);
        }
    }

    private static JsonObject encodeRecord(ElarionConfigApplyAuditRecord record) {
        JsonObject root = new JsonObject();
        JsonObject target = new JsonObject();
        target.addProperty("domainId", record.target().domainId());
        target.addProperty("categoryId", record.target().categoryId());
        target.addProperty("entryId", record.target().entryId());
        root.add("target", target);
        if (record.actorId() != null) root.addProperty("actorId", record.actorId().toString());
        root.addProperty("reason", record.reason());
        root.addProperty("oldDisplayValue", record.oldDisplayValue());
        root.addProperty("newDisplayValue", record.newDisplayValue());
        root.addProperty("reloadRequired", record.reloadRequired());
        root.addProperty("restartRequired", record.restartRequired());
        root.addProperty("auditEventType", record.auditEventType());
        JsonArray affectedFiles = new JsonArray();
        record.affectedFiles().forEach(affectedFiles::add);
        root.add("affectedFiles", affectedFiles);
        return root;
    }

    private static ElarionConfigApplyAuditRecord decodeRecord(String source, JsonObject root) {
        JsonObject target = requiredObject(source, root, "target");
        return new ElarionConfigApplyAuditRecord(
                new ElarionConfigEditTarget(
                        requiredString(source, target, "domainId"),
                        requiredString(source, target, "categoryId"),
                        requiredString(source, target, "entryId")),
                optionalUuid(source, root, "actorId"),
                optionalString(source, root, "reason"),
                optionalString(source, root, "oldDisplayValue"),
                optionalString(source, root, "newDisplayValue"),
                requiredBoolean(source, root, "reloadRequired"),
                requiredBoolean(source, root, "restartRequired"),
                requiredString(source, root, "auditEventType"),
                requiredStringList(source, root, "affectedFiles"));
    }

    private static int requiredInt(String source, JsonObject root, String field) {
        long value = requiredLong(source, root, field);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw format(source, field + " must fit in a 32-bit integer");
        }
        return (int) value;
    }

    private static long requiredLong(String source, JsonObject root, String field) {
        JsonElement value = required(source, root, field);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw format(source, field + " must be a number");
        }
        try {
            return value.getAsBigDecimal().longValueExact();
        } catch (ArithmeticException | NumberFormatException exception) {
            throw format(source, field + " must be an integer", exception);
        }
    }

    private static boolean requiredBoolean(String source, JsonObject root, String field) {
        JsonElement value = required(source, root, field);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            throw format(source, field + " must be a boolean");
        }
        return value.getAsBoolean();
    }

    private static UUID requiredUuid(String source, JsonObject root, String field) {
        return parseUuid(source, requiredString(source, root, field), field);
    }

    private static UUID optionalUuid(String source, JsonObject root, String field) {
        if (!root.has(field) || root.get(field).isJsonNull()) return null;
        return parseUuid(source, requiredString(source, root, field), field);
    }

    private static UUID parseUuid(String source, String value, String field) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw format(source, field + " must be a UUID", exception);
        }
    }

    private static ElarionConfigApplyAuditPhase requiredPhase(String source, JsonObject root, String field) {
        String value = requiredString(source, root, field);
        try {
            return ElarionConfigApplyAuditPhase.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw format(source, field + " must be a known config audit phase", exception);
        }
    }

    private static JsonObject requiredObject(String source, JsonObject root, String field) {
        JsonElement value = required(source, root, field);
        if (!value.isJsonObject()) throw format(source, field + " must be a JSON object");
        return value.getAsJsonObject();
    }

    private static String requiredString(String source, JsonObject root, String field) {
        JsonElement value = required(source, root, field);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw format(source, field + " must be a string");
        }
        String text = value.getAsString();
        if (text.isBlank()) throw format(source, field + " must not be blank");
        return text;
    }

    private static String optionalString(String source, JsonObject root, String field) {
        if (!root.has(field) || root.get(field).isJsonNull()) return "";
        JsonElement value = root.get(field);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw format(source, field + " must be a string");
        }
        return value.getAsString().trim();
    }

    private static List<String> requiredStringList(String source, JsonObject root, String field) {
        JsonElement value = required(source, root, field);
        if (!value.isJsonArray()) throw format(source, field + " must be a JSON array");
        List<String> values = new ArrayList<>();
        for (JsonElement element : value.getAsJsonArray()) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw format(source, field + " entries must be strings");
            }
            String text = element.getAsString().trim();
            if (!text.isBlank()) values.add(text);
        }
        return values;
    }

    private static JsonElement required(String source, JsonObject root, String field) {
        if (!root.has(field) || root.get(field).isJsonNull()) {
            throw format(source, "missing required field " + field);
        }
        return root.get(field);
    }

    private static IllegalStateException format(String source, String message) {
        return new IllegalStateException(source + ": " + message);
    }

    private static IllegalStateException format(String source, String message, Throwable cause) {
        return new IllegalStateException(source + ": " + message, cause);
    }

    public record Entry(
            int schemaVersion,
            UUID auditId,
            ElarionConfigApplyAuditPhase phase,
            long occurredAt,
            ElarionConfigApplyAuditRecord record,
            String failure
    ) {
        public Entry {
            if (schemaVersion != SCHEMA_VERSION) {
                throw new IllegalArgumentException("Unsupported config audit schema version");
            }
            auditId = Objects.requireNonNull(auditId, "Config audit id is required");
            phase = Objects.requireNonNull(phase, "Config audit phase is required");
            if (occurredAt <= 0L) throw new IllegalArgumentException("Config audit occurrence time is required");
            record = Objects.requireNonNull(record, "Config audit record is required");
            failure = failure == null ? "" : failure.trim();
        }
    }

    public record PendingAudit(
            UUID auditId,
            long preparedAt,
            ElarionConfigApplyAuditRecord record
    ) {
        public PendingAudit {
            auditId = Objects.requireNonNull(auditId, "Config audit id is required");
            if (preparedAt <= 0L) throw new IllegalArgumentException("Config audit prepared time is required");
            record = Objects.requireNonNull(record, "Config audit record is required");
        }
    }

    public record Recovery(
            List<PendingAudit> unresolved,
            int linesScanned,
            boolean tailTruncated
    ) {
        public Recovery {
            unresolved = unresolved == null ? List.of() : List.copyOf(unresolved);
            if (linesScanned < 0) throw new IllegalArgumentException("linesScanned must not be negative");
        }
    }

    private final class JournalSession implements ElarionConfigApplyAuditSession {
        private final UUID auditId;
        private final ElarionConfigApplyAuditRecord record;
        private final AtomicBoolean closed = new AtomicBoolean();

        private JournalSession(UUID auditId, ElarionConfigApplyAuditRecord record) {
            this.auditId = auditId;
            this.record = record;
        }

        @Override
        public void committed() {
            close(ElarionConfigApplyAuditPhase.COMMITTED, "");
        }

        @Override
        public void rolledBack(String failure) {
            close(ElarionConfigApplyAuditPhase.ROLLED_BACK, failure);
        }

        @Override
        public void failed(String failure) {
            close(ElarionConfigApplyAuditPhase.FAILED, failure);
        }

        private void close(ElarionConfigApplyAuditPhase phase, String failure) {
            if (closed.get()) throw new IllegalStateException("Config audit session is already closed");
            append(new Entry(SCHEMA_VERSION, auditId, phase, System.currentTimeMillis(), record, failure));
            closed.set(true);
        }
    }
}
