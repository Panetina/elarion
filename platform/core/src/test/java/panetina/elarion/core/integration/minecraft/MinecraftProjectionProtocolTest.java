package panetina.elarion.core.integration.minecraft;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class MinecraftProjectionProtocolTest {
    @Test
    void writesStringSequencesAndBoundedPayloads() {
        var projection = new MinecraftProjectionProtocol.Projection(
                7, MinecraftProjectionProtocol.Mode.STATE, "realm", "ashlands", "ashlands",
                MinecraftProjectionProtocol.Visibility.PUBLIC, 1, 1_700_000_000_000L,
                Map.of("displayName", "The Ashlands"));

        var root = JsonParser.parseString(MinecraftProjectionProtocol.batchBody(List.of(projection)))
                .getAsJsonObject();
        var value = root.getAsJsonArray("projections").get(0).getAsJsonObject();

        assertEquals(1, root.get("schemaVersion").getAsInt());
        assertEquals("7", value.get("sequence").getAsString());
        assertEquals("The Ashlands", value.getAsJsonObject("payload").get("displayName").getAsString());
    }

    @Test
    void rejectsOutOfOrderBatches() {
        var first = projection(2, "one");
        var second = projection(1, "two");
        assertThrows(IllegalArgumentException.class,
                () -> MinecraftProjectionProtocol.batchBody(List.of(first, second)));
    }

    @Test
    void parsesAcknowledgedCursor() {
        assertEquals(42, MinecraftProjectionProtocol.parseAcceptedThrough(
                "{\"ok\":true,\"acceptedThrough\":\"42\"}"));
    }

    private static MinecraftProjectionProtocol.Projection projection(long sequence, String entityId) {
        return new MinecraftProjectionProtocol.Projection(
                sequence, MinecraftProjectionProtocol.Mode.EVENT, "chronicle", entityId, "",
                MinecraftProjectionProtocol.Visibility.PUBLIC, 1, 1, Map.of());
    }
}
