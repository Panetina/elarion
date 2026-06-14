package panetina.elarion.core.model;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class CatchTelemetryEventTest {
    @Test
    void rejectsMissingRequiredFields() {
        UUID eventId = UUID.randomUUID();
        UUID actor = UUID.randomUUID();
        Identifier source = Identifier.of("elarion_angling", "fishing");
        Identifier fish = Identifier.of("elarion_angling", "placeholder_fish_001");
        Identifier rarity = Identifier.of("elarion_angling", "placeholder_common");

        assertThrows(NullPointerException.class,
                () -> event(null, 1, actor, source, fish, rarity, 1, Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> event(eventId, 0, actor, source, fish, rarity, 1, Map.of()));
        assertThrows(NullPointerException.class,
                () -> event(eventId, 1, null, source, fish, rarity, 1, Map.of()));
        assertThrows(NullPointerException.class,
                () -> event(eventId, 1, actor, null, fish, rarity, 1, Map.of()));
        assertThrows(NullPointerException.class,
                () -> event(eventId, 1, actor, source, null, rarity, 1, Map.of()));
        assertThrows(NullPointerException.class,
                () -> event(eventId, 1, actor, source, fish, null, 1, Map.of()));
    }

    @Test
    void rejectsNonpositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () -> event(
                UUID.randomUUID(),
                1,
                UUID.randomUUID(),
                Identifier.of("elarion_angling", "fishing"),
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                Identifier.of("elarion_angling", "placeholder_common"),
                0,
                Map.of()));
    }

    @Test
    void metadataIsBoundedDefensivelyCopiedAndImmutable() {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("method", "placeholder");
        CatchTelemetryEvent event = event(
                UUID.randomUUID(),
                1,
                UUID.randomUUID(),
                Identifier.of("elarion_angling", "fishing"),
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                Identifier.of("elarion_angling", "placeholder_common"),
                1,
                metadata);

        metadata.put("changed", "after-construction");

        assertEquals(Map.of("method", "placeholder"), event.metadata());
        assertThrows(UnsupportedOperationException.class, () -> event.metadata().put("new", "value"));

        Map<String, String> tooMany = new LinkedHashMap<>();
        for (int index = 0; index <= CatchTelemetryEvent.MAX_METADATA_ENTRIES; index++) {
            tooMany.put("key_" + index, "value");
        }
        assertThrows(IllegalArgumentException.class, () -> event(
                UUID.randomUUID(),
                1,
                UUID.randomUUID(),
                Identifier.of("elarion_angling", "fishing"),
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                Identifier.of("elarion_angling", "placeholder_common"),
                1,
                tooMany));
        assertThrows(IllegalArgumentException.class, () -> event(
                UUID.randomUUID(),
                1,
                UUID.randomUUID(),
                Identifier.of("elarion_angling", "fishing"),
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                Identifier.of("elarion_angling", "placeholder_common"),
                1,
                Map.of("", "value")));
        assertThrows(IllegalArgumentException.class, () -> event(
                UUID.randomUUID(),
                1,
                UUID.randomUUID(),
                Identifier.of("elarion_angling", "fishing"),
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                Identifier.of("elarion_angling", "placeholder_common"),
                1,
                Map.of("key", "x".repeat(CatchTelemetryEvent.MAX_METADATA_VALUE_LENGTH + 1))));
    }

    @Test
    void allowsAbsentLocationIdentifiers() {
        CatchTelemetryEvent event = event(
                UUID.randomUUID(),
                1,
                UUID.randomUUID(),
                Identifier.of("elarion_angling", "fishing"),
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                Identifier.of("elarion_angling", "placeholder_common"),
                1,
                null);

        assertNull(event.worldId());
        assertNull(event.dimensionId());
        assertNull(event.biomeId());
        assertEquals(Map.of(), event.metadata());
    }

    private static CatchTelemetryEvent event(
            UUID eventId,
            long occurredAt,
            UUID actor,
            Identifier source,
            Identifier fish,
            Identifier rarity,
            long quantity,
            Map<String, String> metadata
    ) {
        return new CatchTelemetryEvent(
                eventId, occurredAt, actor, source, fish, rarity, quantity, null, null, null, metadata);
    }
}
