package panetina.elarion.addons.angling.persistence;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import panetina.elarion.addons.angling.fishing.AnglingCatchCommit;
import panetina.elarion.addons.angling.fishing.AnglingCatchReward;
import panetina.elarion.addons.angling.fishing.AnglingCatchOutcome;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.AcceptedCatchRecord;
import panetina.elarion.core.model.CatchTelemetryEvent;
import panetina.elarion.core.storage.CatchTelemetryJournalCodec;
import panetina.elarion.core.storage.MetricPersistenceCodec;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Append-only crash-recovery journal for the catch/metric/delivery transaction. */
public final class AnglingCatchTransactionJournal {
    static final int MAX_PENDING_TRANSACTIONS = 4096;
    static final int MAX_LINE_CHARACTERS = 262_144;

    public synchronized void appendRequest(Path root, AnglingCatchCommit commit) throws IOException {
        JsonObject line = new JsonObject();
        line.addProperty("type", "request");
        line.add("telemetry", JsonParser.parseString(
                CatchTelemetryJournalCodec.encode(AcceptedCatchRecord.from(commit.telemetry()))));
        line.add("metrics", JsonParser.parseString(MetricPersistenceCodec.encodeBatch(commit.metrics())));
        line.add("reward", encodeReward(commit.reward()));
        append(root, line);
    }

    public synchronized void appendStage(Path root, UUID eventId, Stage stage) throws IOException {
        if (stage == Stage.REQUESTED) {
            throw new IllegalArgumentException("REQUESTED is represented by the complete request record");
        }
        JsonObject line = new JsonObject();
        line.addProperty("type", stage == Stage.PROJECTED ? "projected" : "delivered");
        line.addProperty("event_id", eventId.toString());
        append(root, line);
    }

    public synchronized Map<UUID, Pending> loadPending(Path root) throws IOException {
        Path file = file(root);
        Map<UUID, Pending> pending = new LinkedHashMap<>();
        if (Files.notExists(file)) return pending;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            long number = 0;
            while ((line = reader.readLine()) != null) {
                number++;
                if (line.isBlank()) continue;
                if (line.length() > MAX_LINE_CHARACTERS) {
                    throw new IllegalArgumentException(file + ":" + number
                            + ": Angling catch transaction line exceeds " + MAX_LINE_CHARACTERS + " characters");
                }
                JsonObject value;
                try {
                    value = JsonParser.parseString(line).getAsJsonObject();
                    String type = value.get("type").getAsString();
                    if ("request".equals(type)) {
                        AnglingCatchCommit commit = decodeCommit(file + ":" + number, value);
                        Pending previous = pending.putIfAbsent(commit.telemetry().eventId(),
                                new Pending(commit, Stage.REQUESTED));
                        if (previous == null && pending.size() > MAX_PENDING_TRANSACTIONS) {
                            throw new IllegalArgumentException("Angling catch transaction pending limit exceeded");
                        }
                        if (previous != null && !previous.commit().equals(commit)) {
                            throw new IllegalArgumentException("conflicting Angling catch transaction request");
                        }
                    } else {
                        UUID eventId = UUID.fromString(value.get("event_id").getAsString());
                        Pending current = pending.get(eventId);
                        if (current == null) throw new IllegalArgumentException("stage precedes Angling request");
                        Stage stage = switch (type) {
                            case "projected" -> Stage.PROJECTED;
                            case "delivered" -> Stage.DELIVERED;
                            default -> throw new IllegalArgumentException("unknown Angling transaction stage " + type);
                        };
                        if (stage.ordinal() < current.stage().ordinal()) {
                            throw new IllegalArgumentException("Angling transaction stage moved backwards");
                        }
                        pending.put(eventId, new Pending(current.commit(), stage));
                    }
                } catch (RuntimeException exception) {
                    throw new IllegalArgumentException(file + ":" + number
                            + ": malformed Angling catch transaction", exception);
                }
            }
        }
        pending.entrySet().removeIf(entry -> entry.getValue().stage() == Stage.DELIVERED);
        return pending;
    }

    /** Atomic rewrite retaining only incomplete transactions; safe only on the single transaction worker. */
    public synchronized void compact(Path root, Map<UUID, Pending> pending) throws IOException {
        if (pending.size() > MAX_PENDING_TRANSACTIONS) {
            throw new IllegalArgumentException("Angling catch transaction pending limit exceeded");
        }
        Path file = file(root);
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.createDirectories(file.getParent());
        StringBuilder content = new StringBuilder();
        for (Pending value : pending.values()) {
            if (value.stage() == Stage.DELIVERED) continue;
            JsonObject request = new JsonObject();
            request.addProperty("type", "request");
            request.add("telemetry", JsonParser.parseString(CatchTelemetryJournalCodec.encode(
                    AcceptedCatchRecord.from(value.commit().telemetry()))));
            request.add("metrics", JsonParser.parseString(MetricPersistenceCodec.encodeBatch(value.commit().metrics())));
            request.add("reward", encodeReward(value.commit().reward()));
            content.append(request).append(System.lineSeparator());
            if (value.stage() == Stage.PROJECTED) {
                JsonObject projected = new JsonObject();
                projected.addProperty("type", "projected");
                projected.addProperty("event_id", value.commit().telemetry().eventId().toString());
                content.append(projected).append(System.lineSeparator());
            }
        }
        Files.writeString(temporary, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
            channel.force(false);
        }
        try {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicMoveFailure) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static AnglingCatchCommit decodeCommit(String source, JsonObject value) {
        AcceptedCatchRecord record = CatchTelemetryJournalCodec.decode(source + ":telemetry",
                value.get("telemetry").toString());
        CatchTelemetryEvent telemetry = new CatchTelemetryEvent(
                record.eventId(), record.occurredAt(), record.actorId(), record.sourceId(),
                record.fishDefinitionId(), record.rarityId(), record.quantity(), record.worldId(),
                record.dimensionId(), record.biomeId(), record.metadata(), record.details());
        return new AnglingCatchCommit(telemetry,
                MetricPersistenceCodec.decodeBatch(source + ":metrics", value.get("metrics").toString()),
                decodeReward(value.getAsJsonObject("reward")));
    }

    private static JsonObject encodeReward(AnglingCatchReward reward) {
        JsonObject value = new JsonObject();
        reward.item().ifPresent(item -> value.add("item", encodeItem(item)));
        if (!reward.additionalItems().isEmpty()) {
            com.google.gson.JsonArray extras = new com.google.gson.JsonArray();
            reward.additionalItems().forEach(item -> extras.add(encodeItem(item)));
            value.add("additional_items", extras);
        }
        reward.entity().ifPresent(entity -> {
            JsonObject encoded = new JsonObject();
            encoded.addProperty("entity_type_id", entity.entityTypeId().toString());
            encoded.addProperty("x", entity.x());
            encoded.addProperty("y", entity.y());
            encoded.addProperty("z", entity.z());
            value.add("entity", encoded);
        });
        reward.baitDebit().ifPresent(debit -> {
            JsonObject encoded = new JsonObject();
            encoded.addProperty("rod_item_id", debit.rodItemId().toString());
            encoded.addProperty("bait_item_id", debit.baitItemId().toString());
            value.add("bait_debit", encoded);
        });
        return value;
    }

    private static AnglingCatchReward decodeReward(JsonObject value) {
        if (value == null) throw new IllegalArgumentException("missing Angling catch reward");
        Optional<AnglingCatchReward.ItemReward> item = Optional.empty();
        if (value.has("item")) {
            item = Optional.of(decodeItem(value.getAsJsonObject("item")));
        }
        Optional<AnglingCatchReward.EntityReward> entity = Optional.empty();
        if (value.has("entity")) {
            JsonObject encoded = value.getAsJsonObject("entity");
            entity = Optional.of(new AnglingCatchReward.EntityReward(
                    id(encoded, "entity_type_id"), encoded.get("x").getAsDouble(),
                    encoded.get("y").getAsDouble(), encoded.get("z").getAsDouble()));
        }
        java.util.ArrayList<AnglingCatchReward.ItemReward> extras = new java.util.ArrayList<>();
        if (value.has("additional_items")) {
            var array = value.getAsJsonArray("additional_items");
            if (array.size() > AnglingCatchOutcome.MAX_ADDITIONAL_REWARD_STACKS) {
                throw new IllegalArgumentException("too many additional catch rewards");
            }
            array.forEach(element -> extras.add(decodeItem(element.getAsJsonObject())));
        }
        Optional<AnglingCatchReward.BaitDebit> baitDebit = Optional.empty();
        if (value.has("bait_debit")) {
            JsonObject encoded = value.getAsJsonObject("bait_debit");
            baitDebit = Optional.of(new AnglingCatchReward.BaitDebit(
                    id(encoded, "rod_item_id"), id(encoded, "bait_item_id")));
        }
        return new AnglingCatchReward(item, entity, extras, baitDebit);
    }

    private static JsonObject encodeItem(AnglingCatchReward.ItemReward item) {
        JsonObject encoded = new JsonObject();
        encoded.addProperty("item_id", item.itemId().toString());
        encoded.addProperty("count", item.count());
        encoded.addProperty("caught_fish_component", item.caughtFishComponent());
        if (!item.stackNbt().isEmpty()) encoded.addProperty("stack_nbt", item.stackNbt());
        item.containedItem().ifPresent(contained -> {
            JsonObject nested = new JsonObject();
            nested.addProperty("item_id", contained.itemId().toString());
            nested.addProperty("count", contained.count());
            nested.addProperty("caught_fish_component", contained.caughtFishComponent());
            encoded.add("contained_item", nested);
        });
        return encoded;
    }

    private static AnglingCatchReward.ItemReward decodeItem(JsonObject encoded) {
        Optional<AnglingCatchReward.ContainedItem> contained = Optional.empty();
        if (encoded.has("contained_item")) {
            JsonObject nested = encoded.getAsJsonObject("contained_item");
            contained = Optional.of(new AnglingCatchReward.ContainedItem(
                    id(nested, "item_id"), nested.get("count").getAsInt(),
                    nested.get("caught_fish_component").getAsBoolean()));
        }
        return new AnglingCatchReward.ItemReward(
                id(encoded, "item_id"), encoded.get("count").getAsInt(),
                encoded.get("caught_fish_component").getAsBoolean(), contained,
                encoded.has("stack_nbt") ? encoded.get("stack_nbt").getAsString() : "");
    }

    private static Identifier id(JsonObject value, String field) {
        Identifier id = Identifier.tryParse(value.get(field).getAsString());
        if (id == null) throw new IllegalArgumentException("invalid identifier in " + field);
        return id;
    }

    private static void append(Path root, JsonObject line) throws IOException {
        Path file = file(root);
        Files.createDirectories(file.getParent());
        Files.writeString(file, line + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE)) {
            channel.force(false);
        }
    }

    public static Path file(Path root) {
        return root.resolve("addon-state").resolve("elarion_angling").resolve("catch-transactions.jsonl");
    }

    public enum Stage {
        REQUESTED,
        PROJECTED,
        DELIVERED
    }

    public record Pending(AnglingCatchCommit commit, Stage stage) {
        public Pending {
            if (commit == null || stage == null) throw new NullPointerException("Angling pending transaction");
        }
    }
}
