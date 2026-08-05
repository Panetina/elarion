package panetina.elarion.core.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import panetina.elarion.core.api.reset.PlayerResetContext;
import panetina.elarion.core.api.reset.PlayerResetFiles;
import panetina.elarion.core.api.reset.PlayerResetHandler;
import panetina.elarion.core.api.reset.PlayerResetRegistry;
import panetina.elarion.core.api.reset.PlayerResetResult;
import panetina.elarion.core.mixin.UserCacheAccessor;

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

public final class PlayerResetService {
    public static final long CONFIRMATION_TTL_MILLIS = 60_000L;
    private static final DateTimeFormatter BACKUP_TIME =
            DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss").withZone(ZoneOffset.UTC);

    private final Logger logger;
    private final PlayerResetRegistry registry;
    private final Map<String, PendingReset> pending = new ConcurrentHashMap<>();
    private final java.util.Set<UUID> resettingPlayers = ConcurrentHashMap.newKeySet();

    public PlayerResetService(Logger logger, PlayerResetRegistry registry) {
        this.logger = logger;
        this.registry = registry;
        registerVanillaHandler();
    }

    public Preview preview(MinecraftServer server, String executorKey) {
        prune();
        Map<String, Long> totals = new LinkedHashMap<>();
        for (PlayerResetHandler handler : registry.handlers()) {
            handler.preview(server).forEach((key, value) -> totals.merge(key, Math.max(0L, value), Long::sum));
        }
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        pending.put(executorKey, new PendingReset(token, System.currentTimeMillis() + CONFIRMATION_TTL_MILLIS));
        return new Preview(token, Map.copyOf(totals));
    }

    public boolean cancel(String executorKey, String token) {
        PendingReset value = pending.get(executorKey);
        if (value == null || !value.token().equals(token)) return false;
        pending.remove(executorKey);
        return true;
    }

    public Execution execute(MinecraftServer server, String executorKey, String executorName, String token)
            throws Exception {
        PendingReset confirmation = pending.remove(executorKey);
        if (confirmation == null || confirmation.expiresAt() < System.currentTimeMillis()
                || !confirmation.token().equals(token)) {
            throw new IllegalArgumentException("That reset confirmation expired or belongs to another executor.");
        }

        server.getPlayerManager().saveAllPlayerData();
        List<ServerPlayerEntity> online = List.copyOf(server.getPlayerManager().getPlayerList());
        online.forEach(player -> resettingPlayers.add(player.getUuid()));
        online.forEach(player -> player.networkHandler.disconnect(net.minecraft.text.Text.literal(
                "The development server is resetting all player progression. Reconnect after the reset completes.")));

        Path worldRoot = server.getSavePath(WorldSavePath.ROOT).toAbsolutePath().normalize();
        Path backup = worldRoot.resolve("elarion/backups/player-reset/"
                + BACKUP_TIME.format(Instant.now()) + "-" + System.currentTimeMillis());
        Files.createDirectories(backup);
        Map<String, List<String>> backedUpTargets = new LinkedHashMap<>();
        for (PlayerResetHandler handler : registry.handlers()) {
            backedUpTargets.put(handler.id(), backup(handler, server, worldRoot, backup));
        }
        PlayerResetFiles.writeBackupManifestAtomic(backup, backedUpTargets);

        Map<String, Long> changed = new LinkedHashMap<>();
        PlayerResetContext context = new PlayerResetContext(server, executorName, backup);
        for (PlayerResetHandler handler : registry.handlers()) {
            PlayerResetResult result = handler.reset(context);
            result.changed().forEach((key, value) -> changed.merge(key, value, Long::sum));
        }
        audit(worldRoot, executorName, backup, changed, online.size());
        logger.warn("Player reset completed by {}: backup={}, changed={}", executorName, backup, changed);
        return new Execution(backup, Map.copyOf(changed), online.size());
    }

    public boolean isResetDisconnect(UUID playerId) {
        return playerId != null && resettingPlayers.contains(playerId);
    }

    public void clearResetDisconnect(UUID playerId) {
        if (playerId != null) resettingPlayers.remove(playerId);
    }

    private List<String> backup(PlayerResetHandler handler, MinecraftServer server, Path worldRoot, Path backupRoot)
            throws IOException {
        java.util.ArrayList<String> copied = new java.util.ArrayList<>();
        Path handlerRoot = backupRoot.resolve(handler.id());
        for (Path raw : handler.backupTargets(server)) {
            if (raw == null || Files.notExists(raw)) continue;
            Path source = raw.toAbsolutePath().normalize();
            String relative = source.startsWith(worldRoot)
                    ? worldRoot.relativize(source).toString()
                    : source.getFileName().toString();
            PlayerResetFiles.copyTree(source, handlerRoot.resolve(relative));
            copied.add(handler.id() + "/" + relative.replace('\\', '/'));
        }
        return List.copyOf(copied);
    }

    private void audit(Path worldRoot, String executor, Path backup, Map<String, Long> changed, int kicked)
            throws IOException {
        Path audit = worldRoot.resolve("elarion/audit/player-reset.log");
        Files.createDirectories(audit.getParent());
        String line = Instant.now() + " executor=" + executor + " backup=" + backup
                + " kicked=" + kicked + " changed=" + changed + System.lineSeparator();
        Files.writeString(audit, line, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private void registerVanillaHandler() {
        registry.register(new PlayerResetHandler() {
            @Override public String id() { return "minecraft"; }

            @Override public Map<String, Long> preview(MinecraftServer server) {
                Path root = server.getSavePath(WorldSavePath.ROOT);
                Path run = server.getRunDirectory();
                try {
                    Map<String, Long> counts = new LinkedHashMap<>();
                    counts.put("players", PlayerResetFiles.countRegularFiles(root.resolve("playerdata")));
                    counts.put("operators", (long) server.getPlayerManager().getOpList().getNames().length);
                    counts.put("whitelistEntries", (long) server.getPlayerManager().getWhitelist().getNames().length);
                    counts.put("cachedProfiles", PlayerResetFiles.countJsonArrayEntries(run.resolve("usercache.json")));
                    return Map.copyOf(counts);
                } catch (IOException exception) {
                    throw new IllegalStateException("Could not inspect vanilla player or access data.", exception);
                }
            }

            @Override public List<Path> backupTargets(MinecraftServer server) {
                Path root = server.getSavePath(WorldSavePath.ROOT);
                Path run = server.getRunDirectory();
                return List.of(
                        root.resolve("playerdata"),
                        root.resolve("advancements"),
                        root.resolve("stats"),
                        run.resolve("ops.json"),
                        run.resolve("whitelist.json"),
                        run.resolve("usercache.json")
                );
            }

            @Override public PlayerResetResult reset(PlayerResetContext context) throws IOException {
                Path root = context.server().getSavePath(WorldSavePath.ROOT);
                Path run = context.server().getRunDirectory();
                long players = PlayerResetFiles.countRegularFiles(root.resolve("playerdata"));
                long operators = context.server().getPlayerManager().getOpList().getNames().length;
                long whitelistEntries = context.server().getPlayerManager().getWhitelist().getNames().length;
                long cachedProfiles = PlayerResetFiles.countJsonArrayEntries(run.resolve("usercache.json"));
                PlayerResetFiles.deleteTree(root.resolve("playerdata"));
                PlayerResetFiles.deleteTree(root.resolve("advancements"));
                PlayerResetFiles.deleteTree(root.resolve("stats"));
                PlayerResetFiles.writeEmptyJsonArrayAtomic(run.resolve("ops.json"));
                PlayerResetFiles.writeEmptyJsonArrayAtomic(run.resolve("whitelist.json"));
                context.server().getPlayerManager().getOpList().load();
                context.server().getPlayerManager().getWhitelist().load();

                UserCacheAccessor cache = (UserCacheAccessor) context.server().getUserCache();
                cache.elarion$pendingRequests().clear();
                cache.elarion$byName().clear();
                cache.elarion$byUuid().clear();
                context.server().getUserCache().save();

                Map<String, Long> changed = new LinkedHashMap<>();
                changed.put("vanillaPlayers", players);
                changed.put("operators", operators);
                changed.put("whitelistEntries", whitelistEntries);
                changed.put("cachedProfiles", cachedProfiles);
                return new PlayerResetResult(changed);
            }
        });
    }

    private void prune() {
        long now = System.currentTimeMillis();
        pending.entrySet().removeIf(entry -> entry.getValue().expiresAt() < now);
    }

    public record Preview(String token, Map<String, Long> counts) {}
    public record Execution(Path backup, Map<String, Long> changed, int kickedPlayers) {}
    private record PendingReset(String token, long expiresAt) {}
}
