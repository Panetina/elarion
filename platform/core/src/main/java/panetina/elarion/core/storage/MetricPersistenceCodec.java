package panetina.elarion.core.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;
import panetina.elarion.core.metric.MetricDescriptor;
import panetina.elarion.core.metric.MetricOperation;
import panetina.elarion.core.metric.MetricProjectionState;
import panetina.elarion.core.metric.MetricScope;
import panetina.elarion.core.metric.MetricScopeType;
import panetina.elarion.core.metric.MetricUpdate;
import panetina.elarion.core.metric.MetricUpdateBatch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Strict versioned JSON codec shared by metric journals and atomic snapshots. */
public final class MetricPersistenceCodec {
    private MetricPersistenceCodec() {
    }

    public static String encodeBatch(MetricUpdateBatch batch) {
        return batch(batch).toString();
    }

    public static MetricUpdateBatch decodeBatch(String source, String json) {
        try {
            return batch(object(JsonParser.parseString(json), "batch"));
        } catch (MetricPersistenceFormatException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw format(source, "invalid metric batch", exception);
        }
    }

    public static String encodeState(MetricProjectionState state) {
        JsonObject root = new JsonObject();
        root.addProperty("schema_version", state.schemaVersion());
        JsonArray indexes = new JsonArray();
        for (MetricProjectionState.IndexState index : state.indexes()) {
            JsonObject value = new JsonObject();
            value.addProperty("metric_id", index.metricId().toString());
            value.add("scope", scope(index.scope()));
            value.add("dimensions", identifiers(index.dimensions()));
            value.addProperty("revision", index.revision());
            JsonArray actors = new JsonArray();
            index.actorValues().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                JsonObject actor = new JsonObject();
                actor.addProperty("actor_id", entry.getKey().toString());
                actor.addProperty("value", entry.getValue());
                actors.add(actor);
            });
            value.add("actors", actors);
            indexes.add(value);
        }
        root.add("indexes", indexes);
        JsonArray partitions = new JsonArray();
        for (MetricProjectionState.PartitionState partition : state.partitions()) {
            JsonObject value = new JsonObject();
            value.addProperty("source_system", partition.sourceSystem().toString());
            value.addProperty("source_partition", partition.sourcePartition());
            value.addProperty("sequence", partition.sequence());
            value.addProperty("event_id", partition.eventId().toString());
            value.add("latest_batch", batch(partition.latestBatch()));
            partitions.add(value);
        }
        root.add("partitions", partitions);
        return root.toString();
    }

    public static MetricProjectionState decodeState(String source, String json) {
        try {
            JsonObject root = object(JsonParser.parseString(json), "state");
            int schema = integer(root, "schema_version");
            JsonArray indexArray = array(root, "indexes", MetricProjectionState.MAX_INDEXES);
            List<MetricProjectionState.IndexState> indexes = new ArrayList<>(indexArray.size());
            for (JsonElement element : indexArray) {
                JsonObject value = object(element, "index");
                Map<UUID, Long> actors = new LinkedHashMap<>();
                JsonArray actorArray = array(value, "actors", MetricProjectionState.MAX_ACTORS_PER_INDEX);
                for (JsonElement actorElement : actorArray) {
                    JsonObject actor = object(actorElement, "actor");
                    UUID actorId = uuid(string(actor, "actor_id"), "actor_id");
                    if (actors.putIfAbsent(actorId, number(actor, "value")) != null) {
                        throw format(source, "duplicate metric actor " + actorId, null);
                    }
                }
                indexes.add(new MetricProjectionState.IndexState(
                        identifier(string(value, "metric_id"), "metric_id"),
                        scope(object(required(value, "scope"), "scope")),
                        identifiers(object(required(value, "dimensions"), "dimensions")),
                        number(value, "revision"), actors));
            }
            JsonArray partitionArray = array(root, "partitions", MetricProjectionState.MAX_PARTITIONS);
            List<MetricProjectionState.PartitionState> partitions = new ArrayList<>(partitionArray.size());
            for (JsonElement element : partitionArray) {
                JsonObject value = object(element, "partition");
                partitions.add(new MetricProjectionState.PartitionState(
                        identifier(string(value, "source_system"), "source_system"),
                        string(value, "source_partition"), number(value, "sequence"),
                        uuid(string(value, "event_id"), "event_id"),
                        batch(object(required(value, "latest_batch"), "latest_batch"))));
            }
            return new MetricProjectionState(schema, indexes, partitions);
        } catch (MetricPersistenceFormatException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw format(source, "invalid metric projection state", exception);
        }
    }

    private static JsonObject batch(MetricUpdateBatch batch) {
        JsonObject root = new JsonObject();
        root.addProperty("source_system", batch.sourceSystem().toString());
        root.addProperty("source_partition", batch.sourcePartition());
        root.addProperty("sequence", batch.sequence());
        root.addProperty("event_id", batch.eventId().toString());
        root.addProperty("actor_id", batch.actorId().toString());
        root.addProperty("occurred_at", batch.occurredAt());
        if (batch.realmId() != null) root.addProperty("realm_id", batch.realmId().toString());
        JsonArray updates = new JsonArray();
        for (MetricUpdate update : batch.updates()) {
            JsonObject value = new JsonObject();
            value.addProperty("metric_id", update.metricId().toString());
            value.addProperty("operation", update.operation().name().toLowerCase(Locale.ROOT));
            value.addProperty("value", update.fixedPointValue());
            JsonArray scopes = new JsonArray();
            update.scopes().stream()
                    .sorted((left, right) -> scopeKey(left).compareTo(scopeKey(right)))
                    .forEach(scope -> scopes.add(scope(scope)));
            value.add("scopes", scopes);
            value.add("dimensions", identifiers(update.dimensions()));
            updates.add(value);
        }
        root.add("updates", updates);
        return root;
    }

    private static MetricUpdateBatch batch(JsonObject root) {
        JsonArray updateArray = array(root, "updates", MetricUpdateBatch.MAX_UPDATES);
        List<MetricUpdate> updates = new ArrayList<>(updateArray.size());
        for (JsonElement element : updateArray) {
            JsonObject value = object(element, "update");
            JsonArray scopeArray = array(value, "scopes", MetricUpdate.MAX_SCOPES);
            Set<MetricScope> scopes = new LinkedHashSet<>();
            for (JsonElement scopeElement : scopeArray) {
                MetricScope scope = scope(object(scopeElement, "scope"));
                if (!scopes.add(scope)) throw new IllegalArgumentException("duplicate metric scope");
            }
            updates.add(new MetricUpdate(
                    identifier(string(value, "metric_id"), "metric_id"),
                    enumValue(MetricOperation.class, string(value, "operation"), "operation"),
                    number(value, "value"), scopes,
                    identifiers(object(required(value, "dimensions"), "dimensions"))));
        }
        return new MetricUpdateBatch(
                identifier(string(root, "source_system"), "source_system"),
                string(root, "source_partition"), number(root, "sequence"),
                uuid(string(root, "event_id"), "event_id"),
                uuid(string(root, "actor_id"), "actor_id"), number(root, "occurred_at"),
                root.has("realm_id") ? identifier(string(root, "realm_id"), "realm_id") : null,
                updates);
    }

    private static JsonObject scope(MetricScope scope) {
        JsonObject value = new JsonObject();
        value.addProperty("type", scope.type().name().toLowerCase(Locale.ROOT));
        if (scope.id() != null) value.addProperty("id", scope.id().toString());
        return value;
    }

    private static MetricScope scope(JsonObject value) {
        MetricScopeType type = enumValue(MetricScopeType.class, string(value, "type"), "scope type");
        Identifier id = value.has("id") ? identifier(string(value, "id"), "scope id") : null;
        return new MetricScope(type, id);
    }

    private static JsonObject identifiers(Map<String, Identifier> values) {
        JsonObject result = new JsonObject();
        values.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(entry -> result.addProperty(entry.getKey(), entry.getValue().toString()));
        return result;
    }

    private static Map<String, Identifier> identifiers(JsonObject value) {
        if (value.size() > MetricDescriptor.MAX_INDEXED_DIMENSIONS) {
            throw new IllegalArgumentException("metric dimensions exceed the bound");
        }
        Map<String, Identifier> result = new LinkedHashMap<>();
        value.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry ->
                result.put(entry.getKey(), identifier(entry.getValue().getAsString(), "dimension")));
        return result;
    }

    private static JsonArray array(JsonObject object, String field, int maximum) {
        JsonElement value = required(object, field);
        if (!value.isJsonArray()) throw new IllegalArgumentException(field + " must be an array");
        JsonArray array = value.getAsJsonArray();
        if (array.isEmpty() && field.equals("updates")) throw new IllegalArgumentException("updates cannot be empty");
        if (array.size() > maximum) throw new IllegalArgumentException(field + " exceeds " + maximum);
        return array;
    }

    private static JsonObject object(JsonElement value, String label) {
        if (value == null || !value.isJsonObject()) throw new IllegalArgumentException(label + " must be an object");
        return value.getAsJsonObject();
    }

    private static JsonElement required(JsonObject object, String field) {
        JsonElement value = object.get(field);
        if (value == null || value.isJsonNull()) throw new IllegalArgumentException(field + " is required");
        return value;
    }

    private static String string(JsonObject object, String field) {
        String value = required(object, field).getAsString();
        if (value.isBlank()) throw new IllegalArgumentException(field + " cannot be blank");
        return value;
    }

    private static long number(JsonObject object, String field) {
        return required(object, field).getAsLong();
    }

    private static int integer(JsonObject object, String field) {
        return required(object, field).getAsInt();
    }

    private static Identifier identifier(String value, String field) {
        Identifier id = Identifier.tryParse(value);
        if (id == null) throw new IllegalArgumentException(field + " is not a valid identifier");
        return id;
    }

    private static UUID uuid(String value, String field) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(field + " is not a UUID", exception);
        }
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value, String field) {
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(field + " has an invalid value", exception);
        }
    }

    private static String scopeKey(MetricScope scope) {
        return scope.type().name() + ":" + (scope.id() == null ? "" : scope.id());
    }

    private static MetricPersistenceFormatException format(String source, String message, Throwable cause) {
        String text = (source == null || source.isBlank() ? "metric persistence" : source) + ": " + message;
        return cause == null ? new MetricPersistenceFormatException(text)
                : new MetricPersistenceFormatException(text, cause);
    }
}
