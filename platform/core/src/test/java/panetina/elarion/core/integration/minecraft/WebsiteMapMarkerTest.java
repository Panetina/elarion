package panetina.elarion.core.integration.minecraft;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class WebsiteMapMarkerTest {
    @Test
    void createsBoundedProjectionPayload() {
        WebsiteMapMarker marker = new WebsiteMapMarker(
                "shrine", "foundation_1", "ashlands", "Shrine of Foundation",
                "minecraft:overworld", 12, 70, -8,
                MinecraftProjectionProtocol.Visibility.PUBLIC, true,
                Map.of("status", "active"));

        assertEquals("map.marker.shrine", marker.projectionKind());
        assertEquals("12", marker.payload().get("x"));
        assertEquals("true", marker.payload().get("active"));
    }

    @Test
    void rejectsUnboundedMetadata() {
        Map<String, String> metadata = new java.util.LinkedHashMap<>();
        for (int index = 0; index < 17; index++) metadata.put("value" + index, "x");
        assertThrows(IllegalArgumentException.class, () -> new WebsiteMapMarker(
                "shrine", "foundation_1", "", "Shrine", "minecraft:overworld",
                0, 0, 0, MinecraftProjectionProtocol.Visibility.PUBLIC, true, metadata));
    }
}
