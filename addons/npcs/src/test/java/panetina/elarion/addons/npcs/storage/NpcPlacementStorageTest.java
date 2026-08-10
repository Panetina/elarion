package panetina.elarion.addons.npcs.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.npcs.model.NpcTaxJurisdictionKind;
import panetina.elarion.addons.npcs.service.NpcTaxJurisdictionResolver;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NpcPlacementStorageTest {
    @TempDir
    Path root;

    @Test
    void schemaOneMigratesWithBackupAndResolvedJurisdiction() throws Exception {
        UUID id = UUID.randomUUID();
        Path file = root.resolve("placed-npcs.json");
        Files.writeString(file, schemaOne(id, "elarion:realm_world_1"), StandardCharsets.UTF_8);
        NpcPlacementStorage storage = new NpcPlacementStorage(LoggerFactory.getLogger("npc-test"), root);
        NpcTaxJurisdictionResolver resolver = new NpcTaxJurisdictionResolver(
                world -> Optional.of("oak"));

        var loaded = storage.load(null, record -> resolver.resolve(record, "auto"));

        assertEquals(NpcTaxJurisdictionKind.REALM, loaded.get(id).taxJurisdictionKind());
        assertEquals("oak", loaded.get(id).taxJurisdictionId());
        assertTrue(Files.exists(root.resolve("placed-npcs.json.schema-v1.bak")));
        String migrated = Files.readString(file);
        assertTrue(migrated.contains("\"schemaVersion\": 2"));
        assertTrue(migrated.contains("\"taxJurisdictionKind\": \"REALM\""));
    }

    @Test
    void migrationFailureLeavesOriginalStateUntouched() throws Exception {
        UUID id = UUID.randomUUID();
        Path file = root.resolve("placed-npcs.json");
        String original = schemaOne(id, "elarion:worldheart");
        Files.writeString(file, original, StandardCharsets.UTF_8);
        NpcPlacementStorage storage = new NpcPlacementStorage(LoggerFactory.getLogger("npc-test"), root);
        NpcTaxJurisdictionResolver resolver = new NpcTaxJurisdictionResolver(
                world -> Optional.of("oak"));

        assertThrows(IllegalArgumentException.class,
                () -> storage.load(null, record -> resolver.resolve(record, "realm:other")));
        assertEquals(original, Files.readString(file));
        assertTrue(Files.exists(root.resolve("placed-npcs.json.schema-v1.bak")));
    }

    @Test
    void unsupportedSchemaFailsClosed() throws Exception {
        Path file = root.resolve("placed-npcs.json");
        String original = "{\"schemaVersion\":99,\"placed\":[]}";
        Files.writeString(file, original, StandardCharsets.UTF_8);
        NpcPlacementStorage storage = new NpcPlacementStorage(LoggerFactory.getLogger("npc-test"), root);

        assertThrows(IllegalStateException.class, () -> storage.load(null));
        assertEquals(original, Files.readString(file));
    }

    @Test
    void unreadableStateIsQuarantinedAndDoesNotBlockStartup() throws Exception {
        Path file = root.resolve("placed-npcs.json");
        Files.writeString(file, "not-json", StandardCharsets.UTF_8);
        NpcPlacementStorage storage = new NpcPlacementStorage(LoggerFactory.getLogger("npc-test"), root);

        assertTrue(storage.load(null).isEmpty());
        try (var files = Files.list(root)) {
            assertTrue(files.anyMatch(path -> path.getFileName().toString()
                    .startsWith("placed-npcs.json.corrupt-")));
        }
    }

    private static String schemaOne(UUID id, String worldId) {
        return """
                {
                  "schemaVersion": 1,
                  "placed": [{
                    "id": "%s",
                    "handle": "merchant_1",
                    "definitionId": "merchant",
                    "worldId": "%s",
                    "x": 0.0,
                    "y": 64.0,
                    "z": 0.0,
                    "yaw": 0.0,
                    "pitch": 0.0,
                    "displayNameOverride": "",
                    "skinOverride": "",
                    "portraitOverride": "",
                    "dialogueOverride": "",
                    "createdAt": 1
                  }]
                }
                """.formatted(id, worldId);
    }
}
