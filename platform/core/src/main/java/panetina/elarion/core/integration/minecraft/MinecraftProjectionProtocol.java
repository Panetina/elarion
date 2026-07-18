package panetina.elarion.core.integration.minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class MinecraftProjectionProtocol {
    private static final Pattern KIND = Pattern.compile("[a-z][a-z0-9_.-]{1,63}");

    private MinecraftProjectionProtocol() {
    }

    public static String batchBody(List<Projection> projections) {
        if (projections.isEmpty() || projections.size() > 100) {
            throw new IllegalArgumentException("Projection count must be between 1 and 100.");
        }
        JsonArray values = new JsonArray();
        long previous = 0;
        for (Projection projection : projections) {
            if (projection.sequence() <= previous) throw new IllegalArgumentException("Projection sequence is not ordered.");
            previous = projection.sequence();
            JsonObject value = new JsonObject();
            value.addProperty("sequence", Long.toString(projection.sequence()));
            value.addProperty("mode", projection.mode().name());
            value.addProperty("kind", projection.kind());
            value.addProperty("entityId", projection.entityId());
            if (!projection.realmId().isBlank()) value.addProperty("realmId", projection.realmId());
            value.addProperty("visibility", projection.visibility().name());
            value.addProperty("version", projection.version());
            value.addProperty("occurredAt", Instant.ofEpochMilli(projection.occurredAt()).toString());
            JsonObject payload = new JsonObject();
            projection.payload().forEach(payload::addProperty);
            value.add("payload", payload);
            values.add(value);
        }
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", 1);
        root.add("projections", values);
        return root.toString();
    }

    public static long parseAcceptedThrough(String body) {
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        if (!root.has("ok") || !root.get("ok").getAsBoolean()) {
            throw new IllegalArgumentException("Projection response was not successful.");
        }
        long accepted = Long.parseLong(root.get("acceptedThrough").getAsString());
        if (accepted < 0) throw new IllegalArgumentException("Invalid projection cursor.");
        return accepted;
    }

    public enum Mode { STATE, EVENT }

    public enum Visibility { PUBLIC, AUTHENTICATED, WHITELISTED, STAFF }

    public record Projection(
            long sequence,
            Mode mode,
            String kind,
            String entityId,
            String realmId,
            Visibility visibility,
            int version,
            long occurredAt,
            Map<String, String> payload
    ) {
        public Projection {
            if (sequence <= 0) throw new IllegalArgumentException("Projection sequence is required.");
            mode = mode == null ? Mode.STATE : mode;
            kind = clean(kind, 64);
            entityId = clean(entityId, 128);
            realmId = clean(realmId, 64);
            visibility = visibility == null ? Visibility.PUBLIC : visibility;
            version = Math.max(1, Math.min(version, 100));
            occurredAt = occurredAt <= 0 ? System.currentTimeMillis() : occurredAt;
            payload = payload == null ? Map.of() : Map.copyOf(payload);
            if (!KIND.matcher(kind).matches()) throw new IllegalArgumentException("Invalid projection kind.");
            if (entityId.isBlank()) throw new IllegalArgumentException("Projection entity id is required.");
            if (payload.size() > 64) throw new IllegalArgumentException("Projection payload has too many fields.");
            int size = payload.entrySet().stream().mapToInt(entry -> entry.getKey().length() + entry.getValue().length()).sum();
            if (size > 16_384) throw new IllegalArgumentException("Projection payload is too large.");
        }

        private static String clean(String value, int max) {
            String cleaned = value == null ? "" : value.trim();
            return cleaned.substring(0, Math.min(cleaned.length(), max));
        }
    }
}
