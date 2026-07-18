package panetina.elarion.core.integration.minecraft;

import panetina.elarion.core.integration.minecraft.MinecraftProjectionProtocol.Visibility;

import java.util.LinkedHashMap;
import java.util.Map;

public record WebsiteMapMarker(
        String type,
        String id,
        String realmId,
        String label,
        String worldId,
        int x,
        int y,
        int z,
        Visibility visibility,
        boolean active,
        Map<String, String> metadata
) {
    private static final int MAX_METADATA = 16;

    public WebsiteMapMarker {
        type = requireId(type, "marker type");
        id = requireValue(id, "marker id", 128);
        realmId = clean(realmId, 64);
        label = requireValue(label, "marker label", 160);
        worldId = requireValue(worldId, "marker world", 128);
        visibility = visibility == null ? Visibility.PUBLIC : visibility;
        metadata = metadata == null ? Map.of() : boundedMetadata(metadata);
    }

    public String projectionKind() {
        return "map.marker." + type;
    }

    public Map<String, String> payload() {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("type", type);
        payload.put("label", label);
        payload.put("worldId", worldId);
        payload.put("x", Integer.toString(x));
        payload.put("y", Integer.toString(y));
        payload.put("z", Integer.toString(z));
        payload.put("active", Boolean.toString(active));
        payload.putAll(metadata);
        return Map.copyOf(payload);
    }

    private static Map<String, String> boundedMetadata(Map<String, String> values) {
        if (values.size() > MAX_METADATA) {
            throw new IllegalArgumentException("Map marker metadata exceeds " + MAX_METADATA + " entries");
        }
        Map<String, String> output = new LinkedHashMap<>();
        values.forEach((key, value) -> output.put(requireId(key, "metadata key"), clean(value, 500)));
        return Map.copyOf(output);
    }

    private static String requireId(String value, String name) {
        String normalized = clean(value, 64).toLowerCase(java.util.Locale.ROOT);
        if (!normalized.matches("[a-z][a-z0-9_.-]{1,63}")) {
            throw new IllegalArgumentException("Invalid " + name);
        }
        return normalized;
    }

    private static String requireValue(String value, String name, int maximum) {
        String normalized = clean(value, maximum);
        if (normalized.isBlank()) throw new IllegalArgumentException(name + " is required");
        return normalized;
    }

    private static String clean(String value, int maximum) {
        String normalized = value == null ? "" : value.trim();
        return normalized.substring(0, Math.min(maximum, normalized.length()));
    }
}
