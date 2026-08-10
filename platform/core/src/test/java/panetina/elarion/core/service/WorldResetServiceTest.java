package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.reset.WorldResetContext;
import panetina.elarion.core.api.reset.WorldResetHandler;
import panetina.elarion.core.api.reset.WorldResetOperator;
import panetina.elarion.core.api.reset.WorldResetRegistry;
import panetina.elarion.core.api.reset.WorldResetResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WorldResetServiceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void failedHandlerRestoresManagedWorldAndAddonFilesBeforeReportingFailure() throws Exception {
        Path root = temporaryDirectory.resolve("save");
        Path managedWorld = root.resolve("dimensions/elarion/lobby/region.dat");
        Path addonState = root.resolve("elarion/portals/routes.json");
        Files.createDirectories(managedWorld.getParent());
        Files.createDirectories(addonState.getParent());
        Files.writeString(managedWorld, "original-world");
        Files.writeString(addonState, "original-addon");

        WorldResetRegistry registry = new WorldResetRegistry();
        AtomicBoolean restoredWorld = new AtomicBoolean();
        AtomicBoolean restoredHandler = new AtomicBoolean();
        registry.setOperator(new WorldResetOperator() {
            @Override public boolean exists(net.minecraft.server.MinecraftServer server, String worldId) {
                return "lobby".equals(worldId);
            }

            @Override public java.util.concurrent.CompletionStage<Void> regenerate(
                    net.minecraft.server.MinecraftServer server, String worldId
            ) throws Exception {
                Files.writeString(managedWorld, "regenerated-world");
                return CompletableFuture.completedFuture(null);
            }

            @Override public java.util.concurrent.CompletionStage<Void> restore(
                    net.minecraft.server.MinecraftServer server, String worldId, Path backupRoot
            ) throws Exception {
                Path backup = backupRoot.resolve("managed-world/dimensions/elarion/lobby/region.dat");
                Files.writeString(managedWorld, Files.readString(backup));
                restoredWorld.set(true);
                return CompletableFuture.completedFuture(null);
            }

            @Override public List<Path> backupTargets(net.minecraft.server.MinecraftServer server, String worldId) {
                return List.of(managedWorld);
            }
        });
        registry.register(new WorldResetHandler() {
            @Override public String id() {
                return "portals";
            }

            @Override public Map<String, Long> preview(net.minecraft.server.MinecraftServer server, String worldId) {
                return Map.of();
            }

            @Override public List<Path> backupTargets(net.minecraft.server.MinecraftServer server, String worldId) {
                return List.of(addonState);
            }

            @Override public WorldResetResult reset(WorldResetContext context) throws Exception {
                Files.writeString(addonState, "partially-reset-addon");
                throw new IllegalStateException("forced handler failure");
            }

            @Override public void restore(WorldResetContext context) {
                assertEquals("original-addon", read(addonState), "Core must restore files before handler refresh");
                restoredHandler.set(true);
            }
        });

        WorldResetService service = new WorldResetService(LoggerFactory.getLogger("world-reset-test"), registry);
        WorldResetService.Preview preview = service.preview(null, "operator", "lobby");

        CompletionException failure = assertThrows(CompletionException.class, () -> service
                .executeFromSaveRoot(null, root, "operator", "Operator", preview.token())
                .toCompletableFuture().join());

        assertEquals("forced handler failure", failure.getCause().getMessage());
        assertEquals("original-world", Files.readString(managedWorld));
        assertEquals("original-addon", Files.readString(addonState));
        assertTrue(restoredWorld.get(), "The managed world must be restored before completing the failed reset");
        assertTrue(restoredHandler.get(), "Every affected handler must refresh its in-memory state after restore");
        Path backupRoot;
        try (var backups = Files.list(root.resolve("elarion/backups/world-reset"))) {
            backupRoot = backups.findFirst().orElseThrow();
        }
        String manifest = Files.readString(backupRoot.resolve("manifest.json"));
        assertTrue(manifest.contains("managed-world/dimensions/elarion/lobby/region.dat"));
        assertTrue(manifest.contains("portals/elarion/portals/routes.json"));
        assertTrue(Files.readString(root.resolve("elarion/audit/world-reset.log")).contains("rolled-back=IllegalStateException"));
    }

    @Test
    void refusesBackupTargetsOutsideTheSaveBeforeRegeneration() throws Exception {
        Path root = temporaryDirectory.resolve("save");
        Path outsideSave = temporaryDirectory.resolve("outside/secret.dat");
        Files.createDirectories(outsideSave.getParent());
        Files.writeString(outsideSave, "must-not-be-copied");
        AtomicBoolean regenerated = new AtomicBoolean();
        WorldResetRegistry registry = new WorldResetRegistry();
        registry.setOperator(new WorldResetOperator() {
            @Override public boolean exists(net.minecraft.server.MinecraftServer server, String worldId) {
                return true;
            }

            @Override public java.util.concurrent.CompletionStage<Void> regenerate(
                    net.minecraft.server.MinecraftServer server, String worldId
            ) {
                regenerated.set(true);
                return CompletableFuture.completedFuture(null);
            }

            @Override public java.util.concurrent.CompletionStage<Void> restore(
                    net.minecraft.server.MinecraftServer server, String worldId, Path backupRoot
            ) {
                return CompletableFuture.completedFuture(null);
            }

            @Override public List<Path> backupTargets(net.minecraft.server.MinecraftServer server, String worldId) {
                return List.of(outsideSave);
            }
        });

        WorldResetService service = new WorldResetService(LoggerFactory.getLogger("world-reset-test"), registry);
        WorldResetService.Preview preview = service.preview(null, "operator", "lobby");

        IOException failure = assertThrows(IOException.class, () ->
                service.executeFromSaveRoot(null, root, "operator", "Operator", preview.token()));

        assertEquals("World reset backup target escapes save root: managed-world", failure.getMessage());
        assertFalse(regenerated.get(), "A rejected backup target must prevent destructive regeneration");
        assertEquals("must-not-be-copied", Files.readString(outsideSave));
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
