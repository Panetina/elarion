package panetina.elarion.core.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.AcceptedCatchRecord;
import panetina.elarion.core.model.CatchTelemetryDetails;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public final class CatchTelemetryJournalCodec {
    private static final DateTimeFormatter MONTH =
            DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneOffset.UTC);

    private CatchTelemetryJournalCodec() {
    }

    public static String encode(AcceptedCatchRecord record) {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", record.schemaVersion());
        root.addProperty("eventId", record.eventId().toString());
        root.addProperty("occurredAt", record.occurredAt());
        root.addProperty("actorId", record.actorId().toString());
        root.addProperty("sourceId", record.sourceId().toString());
        root.addProperty("fishDefinitionId", record.fishDefinitionId().toString());
        root.addProperty("rarityId", record.rarityId().toString());
        root.addProperty("quantity", record.quantity());
        addOptionalIdentifier(root, "worldId", record.worldId());
        addOptionalIdentifier(root, "dimensionId", record.dimensionId());
        addOptionalIdentifier(root, "biomeId", record.biomeId());
        if (record.details() != null) root.add("details", encodeDetails(record.details()));
        JsonObject metadata = new JsonObject();
        new TreeMap<>(record.metadata()).forEach(metadata::addProperty);
        root.add("metadata", metadata);
        return root.toString();
    }

    public static AcceptedCatchRecord decode(String documentId, String json) {
        String source = documentId == null || documentId.isBlank() ? "<catch-record>" : documentId;
        try {
            JsonElement parsed = JsonParser.parseString(json);
            if (!parsed.isJsonObject()) {
                throw format(source, "root must be a JSON object");
            }
            JsonObject root = parsed.getAsJsonObject();
            int schemaVersion = requiredInt(source, root, "schemaVersion");
            if (schemaVersion != 1 && schemaVersion != AcceptedCatchRecord.CURRENT_SCHEMA_VERSION) {
                throw format(source, "unsupported schemaVersion " + schemaVersion);
            }
            return new AcceptedCatchRecord(
                    AcceptedCatchRecord.CURRENT_SCHEMA_VERSION,
                    requiredUuid(source, root, "eventId"),
                    requiredLong(source, root, "occurredAt"),
                    requiredUuid(source, root, "actorId"),
                    requiredIdentifier(source, root, "sourceId"),
                    requiredIdentifier(source, root, "fishDefinitionId"),
                    requiredIdentifier(source, root, "rarityId"),
                    requiredLong(source, root, "quantity"),
                    optionalIdentifier(source, root, "worldId"),
                    optionalIdentifier(source, root, "dimensionId"),
                    optionalIdentifier(source, root, "biomeId"),
                    metadata(source, root),
                    schemaVersion == 1 ? null : details(source, root));
        } catch (CatchTelemetryFormatException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw format(source, "invalid catch record: " + exception.getMessage(), exception);
        }
    }

    public static Path journalPath(Path elarionRoot, UUID actorId, long occurredAt) {
        if (elarionRoot == null) throw new NullPointerException("elarionRoot");
        if (actorId == null) throw new NullPointerException("actorId");
        if (occurredAt <= 0) throw new IllegalArgumentException("occurredAt must be positive");
        String month = MONTH.format(Instant.ofEpochMilli(occurredAt));
        return elarionRoot.resolve("catch-telemetry")
                .resolve("journal")
                .resolve(actorId.toString())
                .resolve(month + ".jsonl");
    }

    private static void addOptionalIdentifier(JsonObject root, String field, Identifier value) {
        if (value != null) root.addProperty(field, value.toString());
    }

    private static JsonObject encodeDetails(CatchTelemetryDetails details) {
        JsonObject root = new JsonObject();
        root.addProperty("outputItemId", details.outputItemId().toString());
        root.addProperty("catchTypeId", details.catchTypeId().toString());
        root.addProperty("sizeMillimetres", details.sizeMillimetres());
        root.addProperty("weightGrams", details.weightGrams());
        root.addProperty("percentileBasisPoints", details.percentileBasisPoints());
        root.addProperty("minigameDurationTicks", details.minigameDurationTicks());
        root.addProperty("perfectCatch", details.perfectCatch());
        root.addProperty("goldenCatch", details.goldenCatch());
        root.addProperty("treasureCompleted", details.treasureCompleted());
        root.addProperty("minigameHits", details.minigameHits());
        addOptionalIdentifier(root, "baitId", details.baitId());
        addOptionalIdentifier(root, "rodId", details.rodId());
        addOptionalIdentifier(root, "bobberId", details.bobberId());
        addOptionalIdentifier(root, "hookId", details.hookId());
        addOptionalIdentifier(root, "fluidId", details.fluidId());
        addOptionalIdentifier(root, "realmId", details.realmId());
        addOptionalIdentifier(root, "tournamentId", details.tournamentId());
        return root;
    }

    private static CatchTelemetryDetails details(String source, JsonObject root) {
        if (!root.has("details") || root.get("details").isJsonNull()) return null;
        JsonElement element = root.get("details");
        if (!element.isJsonObject()) throw format(source, "details must be a JSON object");
        JsonObject details = element.getAsJsonObject();
        return new CatchTelemetryDetails(
                requiredIdentifier(source + ".details", details, "outputItemId"),
                requiredIdentifier(source + ".details", details, "catchTypeId"),
                requiredInt(source + ".details", details, "sizeMillimetres"),
                requiredLong(source + ".details", details, "weightGrams"),
                requiredInt(source + ".details", details, "percentileBasisPoints"),
                requiredInt(source + ".details", details, "minigameDurationTicks"),
                requiredBoolean(source + ".details", details, "perfectCatch"),
                requiredBoolean(source + ".details", details, "goldenCatch"),
                requiredBoolean(source + ".details", details, "treasureCompleted"),
                requiredInt(source + ".details", details, "minigameHits"),
                optionalIdentifier(source + ".details", details, "baitId"),
                optionalIdentifier(source + ".details", details, "rodId"),
                optionalIdentifier(source + ".details", details, "bobberId"),
                optionalIdentifier(source + ".details", details, "hookId"),
                optionalIdentifier(source + ".details", details, "fluidId"),
                optionalIdentifier(source + ".details", details, "realmId"),
                optionalIdentifier(source + ".details", details, "tournamentId"));
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

    private static UUID requiredUuid(String source, JsonObject root, String field) {
        String value = requiredString(source, root, field);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw format(source, field + " must be a UUID", exception);
        }
    }

    private static boolean requiredBoolean(String source, JsonObject root, String field) {
        JsonElement value = required(source, root, field);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            throw format(source, field + " must be a boolean");
        }
        return value.getAsBoolean();
    }

    private static Identifier requiredIdentifier(String source, JsonObject root, String field) {
        String value = requiredString(source, root, field);
        Identifier identifier = Identifier.tryParse(value);
        if (identifier == null) throw format(source, field + " must be a valid identifier");
        return identifier;
    }

    private static Identifier optionalIdentifier(String source, JsonObject root, String field) {
        if (!root.has(field) || root.get(field).isJsonNull()) return null;
        return requiredIdentifier(source, root, field);
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

    private static JsonElement required(String source, JsonObject root, String field) {
        if (!root.has(field) || root.get(field).isJsonNull()) {
            throw format(source, "missing required field " + field);
        }
        return root.get(field);
    }

    private static Map<String, String> metadata(String source, JsonObject root) {
        if (!root.has("metadata") || root.get("metadata").isJsonNull()) return Map.of();
        JsonElement element = root.get("metadata");
        if (!element.isJsonObject()) throw format(source, "metadata must be a JSON object");
        Map<String, String> values = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            JsonElement value = entry.getValue();
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                throw format(source, "metadata." + entry.getKey() + " must be a string");
            }
            values.put(entry.getKey(), value.getAsString());
        }
        return values;
    }

    private static CatchTelemetryFormatException format(String source, String message) {
        return new CatchTelemetryFormatException(source + ": " + message);
    }

    private static CatchTelemetryFormatException format(String source, String message, Throwable cause) {
        return new CatchTelemetryFormatException(source + ": " + message, cause);
    }
}
