package panetina.elarion.core.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.AcceptedCatchRecord;
import panetina.elarion.core.model.CatchJournalCheckpoint;
import panetina.elarion.core.model.CatchSummary;
import panetina.elarion.core.model.CatchSpeciesSummary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CatchSummaryCodec {
    private CatchSummaryCodec() {
    }

    public static JsonObject encode(CatchSummary summary) {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", summary.schemaVersion());
        root.addProperty("actorId", summary.actorId().toString());
        root.addProperty("totalQuantity", summary.totalQuantity());
        root.add("quantitiesBySource", encodeCounts(summary.quantitiesBySource()));
        root.add("quantitiesByFishDefinition", encodeCounts(summary.quantitiesByFishDefinition()));
        root.add("quantitiesByRarity", encodeCounts(summary.quantitiesByRarity()));
        root.add("speciesSummaries", encodeSpeciesSummaries(summary.speciesSummaries()));
        root.addProperty("firstCatchAt", summary.firstCatchAt());
        root.addProperty("latestCatchAt", summary.latestCatchAt());
        JsonObject checkpoint = new JsonObject();
        checkpoint.addProperty("month", summary.checkpoint().month());
        checkpoint.addProperty("processedLines", summary.checkpoint().processedLines());
        root.add("checkpoint", checkpoint);
        JsonArray recent = new JsonArray();
        summary.recentCatches().forEach(record ->
                recent.add(JsonParser.parseString(CatchTelemetryJournalCodec.encode(record))));
        root.add("recentCatches", recent);
        return root;
    }

    public static CatchSummary decode(String documentId, String json) {
        String source = documentId == null || documentId.isBlank() ? "<catch-summary>" : documentId;
        try {
            JsonElement parsed = JsonParser.parseString(json);
            if (!parsed.isJsonObject()) throw format(source, "root must be a JSON object");
            JsonObject root = parsed.getAsJsonObject();
            UUID actorId = uuid(source, root, "actorId");
            int schemaVersion = integer(source, root, "schemaVersion");
            if (schemaVersion != 1 && schemaVersion != CatchSummary.CURRENT_SCHEMA_VERSION) {
                throw format(source, "unsupported schemaVersion " + schemaVersion);
            }
            Map<Identifier, Long> fishCounts = counts(source, root, "quantitiesByFishDefinition");
            long firstCatchAt = number(source, root, "firstCatchAt");
            return new CatchSummary(
                    CatchSummary.CURRENT_SCHEMA_VERSION,
                    actorId,
                    number(source, root, "totalQuantity"),
                    counts(source, root, "quantitiesBySource"),
                    fishCounts,
                    counts(source, root, "quantitiesByRarity"),
                    schemaVersion == 1
                            ? countOnlySpecies(fishCounts, firstCatchAt)
                            : speciesSummaries(source, root),
                    firstCatchAt,
                    number(source, root, "latestCatchAt"),
                    checkpoint(source, root),
                    recent(source, root, actorId));
        } catch (CatchTelemetryFormatException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw format(source, "invalid catch summary: " + exception.getMessage(), exception);
        }
    }

    private static JsonObject encodeCounts(Map<Identifier, Long> counts) {
        JsonObject object = new JsonObject();
        counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)))
                .forEach(entry -> object.addProperty(entry.getKey().toString(), entry.getValue()));
        return object;
    }

    private static JsonObject encodeSpeciesSummaries(Map<Identifier, CatchSpeciesSummary> summaries) {
        JsonObject object = new JsonObject();
        summaries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)))
                .forEach(entry -> {
                    CatchSpeciesSummary summary = entry.getValue();
                    JsonObject value = new JsonObject();
                    value.addProperty("totalCount", summary.totalCount());
                    value.addProperty("firstCatchAt", summary.firstCatchAt());
                    value.addProperty("fastestTimeTicks", summary.fastestTimeTicks());
                    value.addProperty("accumulatedTimeTicks", summary.accumulatedTimeTicks());
                    value.addProperty("timedSampleCount", summary.timedSampleCount());
                    value.addProperty("largestSizeMillimetres", summary.largestSizeMillimetres());
                    value.addProperty("heaviestWeightGrams", summary.heaviestWeightGrams());
                    value.addProperty("bestPercentileBasisPoints", summary.bestPercentileBasisPoints());
                    value.addProperty("goldenCount", summary.goldenCount());
                    value.addProperty("perfectCount", summary.perfectCount());
                    value.addProperty("treasureCount", summary.treasureCount());
                    object.add(entry.getKey().toString(), value);
                });
        return object;
    }

    private static Map<Identifier, CatchSpeciesSummary> speciesSummaries(
            String source,
            JsonObject root
    ) {
        JsonElement element = required(source, root, "speciesSummaries");
        if (!element.isJsonObject()) throw format(source, "speciesSummaries must be a JSON object");
        Map<Identifier, CatchSpeciesSummary> summaries = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id == null) throw format(source, "speciesSummaries contains an invalid identifier");
            if (!entry.getValue().isJsonObject()) {
                throw format(source, "speciesSummaries." + entry.getKey() + " must be a JSON object");
            }
            JsonObject value = entry.getValue().getAsJsonObject();
            String nested = source + ".speciesSummaries." + entry.getKey();
            summaries.put(id, new CatchSpeciesSummary(
                    number(nested, value, "totalCount"),
                    number(nested, value, "firstCatchAt"),
                    integer(nested, value, "fastestTimeTicks"),
                    number(nested, value, "accumulatedTimeTicks"),
                    number(nested, value, "timedSampleCount"),
                    integer(nested, value, "largestSizeMillimetres"),
                    number(nested, value, "heaviestWeightGrams"),
                    integer(nested, value, "bestPercentileBasisPoints"),
                    number(nested, value, "goldenCount"),
                    number(nested, value, "perfectCount"),
                    number(nested, value, "treasureCount")));
        }
        return summaries;
    }

    private static Map<Identifier, CatchSpeciesSummary> countOnlySpecies(
            Map<Identifier, Long> counts,
            long firstCatchAt
    ) {
        Map<Identifier, CatchSpeciesSummary> summaries = new LinkedHashMap<>();
        counts.forEach((id, count) -> summaries.put(id, CatchSpeciesSummary.countOnly(count, firstCatchAt)));
        return summaries;
    }

    private static Map<Identifier, Long> counts(String source, JsonObject root, String field) {
        JsonElement element = required(source, root, field);
        if (!element.isJsonObject()) throw format(source, field + " must be a JSON object");
        Map<Identifier, Long> counts = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id == null) throw format(source, field + " contains an invalid identifier");
            counts.put(id, exactLong(source, field + "." + entry.getKey(), entry.getValue()));
        }
        return counts;
    }

    private static CatchJournalCheckpoint checkpoint(String source, JsonObject root) {
        JsonElement element = required(source, root, "checkpoint");
        if (!element.isJsonObject()) throw format(source, "checkpoint must be a JSON object");
        JsonObject checkpoint = element.getAsJsonObject();
        return new CatchJournalCheckpoint(
                string(source, checkpoint, "month", true),
                number(source, checkpoint, "processedLines"));
    }

    private static List<AcceptedCatchRecord> recent(
            String source,
            JsonObject root,
            UUID actorId
    ) {
        JsonElement element = required(source, root, "recentCatches");
        if (!element.isJsonArray()) throw format(source, "recentCatches must be a JSON array");
        List<AcceptedCatchRecord> records = new ArrayList<>();
        int index = 0;
        for (JsonElement value : element.getAsJsonArray()) {
            AcceptedCatchRecord record = CatchTelemetryJournalCodec.decode(
                    source + ".recentCatches[" + index + "]", value.toString());
            if (!actorId.equals(record.actorId())) {
                throw format(source, "recentCatches[" + index + "] actor does not match summary actor");
            }
            records.add(record);
            index++;
        }
        return records;
    }

    private static int integer(String source, JsonObject root, String field) {
        long value = number(source, root, field);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw format(source, field + " must fit in a 32-bit integer");
        }
        return (int) value;
    }

    private static long number(String source, JsonObject root, String field) {
        return exactLong(source, field, required(source, root, field));
    }

    private static long exactLong(String source, String field, JsonElement value) {
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw format(source, field + " must be a number");
        }
        try {
            return value.getAsBigDecimal().longValueExact();
        } catch (ArithmeticException | NumberFormatException exception) {
            throw format(source, field + " must be an integer", exception);
        }
    }

    private static UUID uuid(String source, JsonObject root, String field) {
        try {
            return UUID.fromString(string(source, root, field, false));
        } catch (IllegalArgumentException exception) {
            throw format(source, field + " must be a UUID", exception);
        }
    }

    private static String string(
            String source,
            JsonObject root,
            String field,
            boolean allowBlank
    ) {
        JsonElement value = required(source, root, field);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw format(source, field + " must be a string");
        }
        String text = value.getAsString();
        if (!allowBlank && text.isBlank()) throw format(source, field + " must not be blank");
        return text;
    }

    private static JsonElement required(String source, JsonObject root, String field) {
        if (!root.has(field) || root.get(field).isJsonNull()) {
            throw format(source, "missing required field " + field);
        }
        return root.get(field);
    }

    private static CatchTelemetryFormatException format(String source, String message) {
        return new CatchTelemetryFormatException(source + ": " + message);
    }

    private static CatchTelemetryFormatException format(String source, String message, Throwable cause) {
        return new CatchTelemetryFormatException(source + ": " + message, cause);
    }
}
