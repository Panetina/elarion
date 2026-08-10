package panetina.elarion.core.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import panetina.elarion.core.api.reset.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class WorldResetService {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss")
            .withZone(ZoneOffset.UTC);
    private final Logger logger;
    private final WorldResetRegistry registry;
    private final Map<String, Pending> pending = new ConcurrentHashMap<>();

    public WorldResetService(Logger logger, WorldResetRegistry registry) {
        this.logger = logger;
        this.registry = registry;
    }

    public Preview preview(MinecraftServer server, String executor, String worldId) {
        WorldResetOperator operator = registry.operator();
        if (operator == null || !operator.exists(server, worldId)) throw new IllegalArgumentException("Unknown managed world: " + worldId);
        Map<String, Long> counts = new LinkedHashMap<>();
        for (WorldResetHandler handler : registry.handlers()) handler.preview(server, worldId)
                .forEach((key, value) -> counts.merge(key, Math.max(0L, value), Long::sum));
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        pending.put(executor, new Pending(token, worldId, System.currentTimeMillis() + 60_000L));
        return new Preview(token, Map.copyOf(counts));
    }

    public CompletionStage<Execution> execute(MinecraftServer server, String executor, String name, String token)
            throws Exception {
        return executeFromSaveRoot(server, server.getSavePath(WorldSavePath.ROOT), executor, name, token);
    }

    /**
     * Executes a confirmed reset from an explicit save root. The overload keeps
     * the filesystem transaction independently verifiable; production callers
     * always derive the root from the authoritative Minecraft server.
     */
    CompletionStage<Execution> executeFromSaveRoot(
            MinecraftServer server, Path saveRoot, String executor, String name, String token
    ) throws Exception {
        Pending confirmation = pending.remove(executor);
        if (confirmation == null || confirmation.expiresAt < System.currentTimeMillis() || !confirmation.token.equals(token))
            throw new IllegalArgumentException("That reset confirmation expired or belongs to another executor.");
        String worldId = confirmation.worldId;
        Path root = saveRoot.toAbsolutePath().normalize();
        Path backup = root.resolve("elarion/backups/world-reset/" + safe(worldId) + "-" + TIME.format(Instant.now()) + "-" + System.currentTimeMillis());
        Files.createDirectories(backup);
        Map<String, List<String>> backedUpTargets = new LinkedHashMap<>();
        backedUpTargets.put("managed-world", backupOperatorTargets(registry.operator(), server, worldId, root, backup));
        for (WorldResetHandler handler : registry.handlers()) {
            backedUpTargets.put(handler.id(), backup(handler, server, worldId, root, backup));
        }
        PlayerResetFiles.writeBackupManifestAtomic(backup, backedUpTargets);
        CompletableFuture<Execution> result = new CompletableFuture<>();
        registry.operator().regenerate(server, worldId).thenApply(ignored -> {
            try {
                Map<String, Long> changed = new LinkedHashMap<>();
                WorldResetContext context = new WorldResetContext(server, worldId, name, backup);
                for (WorldResetHandler handler : registry.handlers()) handler.reset(context).changed()
                        .forEach((key, value) -> changed.merge(key, value, Long::sum));
                Path audit = root.resolve("elarion/audit/world-reset.log");
                Files.createDirectories(audit.getParent());
                Files.writeString(audit, Instant.now() + " executor=" + name + " world=" + worldId + " backup=" + backup
                        + " changed=" + changed + System.lineSeparator(), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                logger.warn("World reset completed by {} for {}: backup={}, changed={}", name, worldId, backup, changed);
                return new Execution(worldId, backup, Map.copyOf(changed));
            } catch (Exception exception) {
                throw new CompletionException(exception);
            }
        }).whenComplete((execution, failure) -> {
            if (failure == null) {
                result.complete(execution);
                return;
            }
            Throwable original = unwrap(failure);
            rollback(server, worldId, name, backup, root, original).whenComplete((ignored, rollbackFailure) -> {
                if (rollbackFailure != null) original.addSuppressed(unwrap(rollbackFailure));
                result.completeExceptionally(original);
            });
        });
        return result;
    }

    public boolean cancel(String executor, String token) {
        Pending value = pending.get(executor);
        return value != null && value.token.equals(token) && pending.remove(executor, value);
    }

    private List<String> backup(WorldResetHandler handler, MinecraftServer server, String worldId, Path root, Path backup)
            throws IOException {
        java.util.ArrayList<String> copied = new java.util.ArrayList<>();
        Path destination = backup.resolve(handler.id());
        for (Path raw : handler.backupTargets(server, worldId)) {
            if (raw == null || Files.notExists(raw)) continue;
            Path source = raw.toAbsolutePath().normalize();
            String relative = relativeWithinSaveRoot(source, root, handler.id());
            PlayerResetFiles.copyTree(source, destination.resolve(relative));
            copied.add(handler.id() + "/" + relative.replace('\\', '/'));
        }
        return List.copyOf(copied);
    }

    private CompletionStage<Void> rollback(
            MinecraftServer server, String worldId, String executor, Path backup, Path root, Throwable failure
    ) {
        Throwable original = unwrap(failure);
        logger.error("World reset failed for {}; restoring backup {}", worldId, backup, original);
        WorldResetContext context = new WorldResetContext(server, worldId, executor, backup);
        try {
            return registry.operator().restore(server, worldId, backup).thenRun(() -> {
                try {
                    for (WorldResetHandler handler : registry.handlers()) restoreBackup(handler, server, worldId, root, backup);
                    for (WorldResetHandler handler : registry.handlers()) handler.restore(context);
                    appendAudit(root, executor, worldId, backup, "rolled-back=" + original.getClass().getSimpleName());
                    logger.warn("World reset rolled back for {} from {}", worldId, backup);
                } catch (Exception rollbackFailure) {
                    original.addSuppressed(rollbackFailure);
                    throw new CompletionException(original);
                }
            });
        } catch (Exception rollbackStartFailure) {
            original.addSuppressed(rollbackStartFailure);
            return CompletableFuture.failedFuture(original);
        }
    }

    private void restoreBackup(WorldResetHandler handler, MinecraftServer server, String worldId, Path root, Path backup)
            throws IOException {
        Path destination = backup.resolve(handler.id());
        for (Path raw : handler.backupTargets(server, worldId)) {
            if (raw == null) continue;
            Path target = raw.toAbsolutePath().normalize();
            if (!target.startsWith(root)) throw new IOException("World reset target escapes save root: " + handler.id());
            String relative = root.relativize(target).toString();
            PlayerResetFiles.deleteTree(target);
            Path source = destination.resolve(relative).normalize();
            if (source.startsWith(destination) && Files.exists(source)) PlayerResetFiles.copyTree(source, target);
        }
    }

    private static Throwable unwrap(Throwable failure) {
        return failure instanceof CompletionException && failure.getCause() != null ? failure.getCause() : failure;
    }

    private static void appendAudit(Path root, String executor, String worldId, Path backup, String detail) throws IOException {
        Path audit = root.resolve("elarion/audit/world-reset.log");
        Files.createDirectories(audit.getParent());
        Files.writeString(audit, Instant.now() + " executor=" + executor + " world=" + worldId + " backup=" + backup
                + " " + detail + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private List<String> backupOperatorTargets(WorldResetOperator operator, MinecraftServer server, String worldId,
                                                Path root, Path backup) throws IOException {
        java.util.ArrayList<String> copied = new java.util.ArrayList<>();
        Path destination = backup.resolve("managed-world");
        for (Path raw : operator.backupTargets(server, worldId)) {
            if (raw == null || Files.notExists(raw)) continue;
            Path source = raw.toAbsolutePath().normalize();
            String relative = relativeWithinSaveRoot(source, root, "managed-world");
            PlayerResetFiles.copyTree(source, destination.resolve(relative));
            copied.add("managed-world/" + relative.replace('\\', '/'));
        }
        return List.copyOf(copied);
    }

    private static String relativeWithinSaveRoot(Path source, Path root, String owner) throws IOException {
        if (!source.startsWith(root)) {
            throw new IOException("World reset backup target escapes save root: " + owner);
        }
        return root.relativize(source).toString();
    }

    private static String safe(String value) { return value.replaceAll("[^a-zA-Z0-9._-]", "_"); }
    public record Preview(String token, Map<String, Long> counts) {}
    public record Execution(String worldId, Path backup, Map<String, Long> changed) {}
    private record Pending(String token, String worldId, long expiresAt) {}
}
