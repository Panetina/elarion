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
        Pending confirmation = pending.remove(executor);
        if (confirmation == null || confirmation.expiresAt < System.currentTimeMillis() || !confirmation.token.equals(token))
            throw new IllegalArgumentException("That reset confirmation expired or belongs to another executor.");
        String worldId = confirmation.worldId;
        Path root = server.getSavePath(WorldSavePath.ROOT).toAbsolutePath().normalize();
        Path backup = root.resolve("elarion/backups/world-reset/" + safe(worldId) + "-" + TIME.format(Instant.now()) + "-" + System.currentTimeMillis());
        Files.createDirectories(backup);
        Map<String, List<String>> backedUpTargets = new LinkedHashMap<>();
        backedUpTargets.put("managed-world", backupOperatorTargets(registry.operator(), server, worldId, root, backup));
        for (WorldResetHandler handler : registry.handlers()) {
            backedUpTargets.put(handler.id(), backup(handler, server, worldId, root, backup));
        }
        PlayerResetFiles.writeBackupManifestAtomic(backup, backedUpTargets);
        return registry.operator().regenerate(server, worldId).thenApply(ignored -> {
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
                throw new java.util.concurrent.CompletionException(exception);
            }
        });
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
            String relative = source.startsWith(root) ? root.relativize(source).toString() : source.getFileName().toString();
            PlayerResetFiles.copyTree(source, destination.resolve(relative));
            copied.add(handler.id() + "/" + relative.replace('\\', '/'));
        }
        return List.copyOf(copied);
    }

    private List<String> backupOperatorTargets(WorldResetOperator operator, MinecraftServer server, String worldId,
                                                Path root, Path backup) throws IOException {
        java.util.ArrayList<String> copied = new java.util.ArrayList<>();
        Path destination = backup.resolve("managed-world");
        for (Path raw : operator.backupTargets(server, worldId)) {
            if (raw == null || Files.notExists(raw)) continue;
            Path source = raw.toAbsolutePath().normalize();
            String relative = source.startsWith(root) ? root.relativize(source).toString() : source.getFileName().toString();
            PlayerResetFiles.copyTree(source, destination.resolve(relative));
            copied.add("managed-world/" + relative.replace('\\', '/'));
        }
        return List.copyOf(copied);
    }

    private static String safe(String value) { return value.replaceAll("[^a-zA-Z0-9._-]", "_"); }
    public record Preview(String token, Map<String, Long> counts) {}
    public record Execution(String worldId, Path backup, Map<String, Long> changed) {}
    private record Pending(String token, String worldId, long expiresAt) {}
}
