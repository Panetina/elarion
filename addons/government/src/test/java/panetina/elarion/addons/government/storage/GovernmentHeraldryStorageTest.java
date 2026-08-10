package panetina.elarion.addons.government.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.government.model.RealmHeraldry;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class GovernmentHeraldryStorageTest {
    @Test void persistsHeraldryAcrossStorageReload(@TempDir Path root) {
        byte[] pixels = new byte[1024];
        pixels[77] = 9;
        GovernmentState state = new GovernmentState();
        state.heraldry.put("ember", RealmHeraldry.blank().revised(pixels));
        GovernmentStorage storage = new GovernmentStorage(LoggerFactory.getLogger("test"), root);
        storage.save(root, state);
        RealmHeraldry restored = storage.load(root).heraldry.get("ember");
        assertEquals(1L, restored.revision());
        assertArrayEquals(pixels, restored.paletteIndices());
    }

    @Test void migratesSchemaOneStateWithoutDiscardingCivicData(@TempDir Path root) {
        GovernmentState state = new GovernmentState();
        state.schemaVersion = 1;
        state.realms.put("ember", panetina.elarion.addons.government.model.RealmGovernmentState.empty("ember"));
        GovernmentStorage storage = new GovernmentStorage(LoggerFactory.getLogger("test"), root);
        storage.save(root, state);

        GovernmentState loaded = storage.load(root);

        assertEquals(GovernmentState.CURRENT_SCHEMA_VERSION, loaded.schemaVersion);
        assertEquals(1, loaded.realms.size());
        assertEquals(0, loaded.heraldry.size());
    }
}
