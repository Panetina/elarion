package panetina.elarion.core.api;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.CatchTelemetryEvent;
import panetina.elarion.core.service.CatchTelemetryService;
import panetina.elarion.core.storage.CatchSummaryStorage;
import panetina.elarion.core.storage.CatchTelemetryJournalStorage;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ElarionCatchTelemetryApiTest {
    @TempDir
    Path tempDir;

    @Test
    void exposesImmutableDirectPlayerSummaryQueries() {
        UUID actorId = UUID.randomUUID();
        Identifier sourceId = Identifier.of("elarion_angling", "fishing");
        Identifier fishId = Identifier.of("elarion_angling", "placeholder_fish_001");
        Identifier rarityId = Identifier.of("elarion_angling", "placeholder_common");
        CatchTelemetryService service = new CatchTelemetryService(
                new CatchTelemetryJournalStorage(),
                new CatchSummaryStorage(),
                LoggerFactory.getLogger("catch-telemetry-api-test"));
        service.bind(tempDir);
        service.accept(new CatchTelemetryEvent(
                UUID.randomUUID(),
                Instant.parse("2026-06-12T10:15:30Z").toEpochMilli(),
                actorId,
                sourceId,
                fishId,
                rarityId,
                3,
                null,
                null,
                null,
                Map.of()));
        ElarionCatchTelemetryApi api = new ElarionCatchTelemetryApi(service);

        assertEquals(3, api.totalQuantity(actorId));
        assertEquals(3, api.quantityForSource(actorId, sourceId));
        assertEquals(3, api.quantityForFishDefinition(actorId, fishId));
        assertEquals(3, api.quantityForRarity(actorId, rarityId));
        assertThrows(UnsupportedOperationException.class, () -> api.recentCatches(actorId).clear());
    }
}
