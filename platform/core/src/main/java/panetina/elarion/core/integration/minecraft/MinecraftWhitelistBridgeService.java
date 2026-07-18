package panetina.elarion.core.integration.minecraft;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Whitelist;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import panetina.elarion.core.integration.minecraft.MinecraftBridgeProtocol.Acknowledgement;
import panetina.elarion.core.integration.minecraft.MinecraftBridgeProtocol.Command;
import panetina.elarion.core.storage.JsonStateStorage;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class MinecraftWhitelistBridgeService {
    private final Logger logger;
    private final MinecraftBridgeConfig config;
    private final MinecraftBridgeStateStorage storage;
    private final MinecraftBridgeClient client;
    private final MinecraftProjectionPublisher projections;
    private volatile boolean running;
    private MinecraftServer server;
    private ScheduledExecutorService executor;
    private MinecraftBridgeStateStorage.State state = MinecraftBridgeStateStorage.State.empty();

    public MinecraftWhitelistBridgeService(Logger logger, MinecraftBridgeConfig config) {
        this.logger = logger;
        this.config = config;
        this.storage = new MinecraftBridgeStateStorage(logger);
        this.client = config.enabled() ? new MinecraftBridgeClient(config) : null;
        this.projections = new MinecraftProjectionPublisher(logger, config.enabled());
    }

    public MinecraftProjectionPublisher projections() {
        return projections;
    }

    public void start(MinecraftServer server) {
        if (!config.enabled()) {
            logger.info("Minecraft whitelist bridge is disabled.");
            return;
        }
        if (!server.isOnlineMode()) {
            logger.error("Minecraft whitelist bridge requires online-mode=true; bridge was not started.");
            return;
        }
        if (!server.getPlayerManager().isWhitelistEnabled()) {
            logger.error("Minecraft whitelist bridge requires white-list=true; bridge was not started.");
            return;
        }
        this.server = server;
        this.state = storage.load(JsonStateStorage.elarionRoot(server));
        this.projections.bind(JsonStateStorage.elarionRoot(server));
        this.running = true;
        this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "elarion-minecraft-bridge");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleWithFixedDelay(this::pollSafely, 1, config.pollSeconds(), TimeUnit.SECONDS);
        logger.info("Minecraft whitelist bridge started for server id {} at sequence {}.",
                config.serverId(), state.cursor());
    }

    public void stop() {
        running = false;
        ScheduledExecutorService current = executor;
        executor = null;
        if (current != null) current.shutdownNow();
        server = null;
        projections.unbind();
    }

    private void pollSafely() {
        if (!running) return;
        try {
            projections.flush(client);
            flushPendingAcknowledgements();
            List<Command> commands = client.fetchChanges(state.cursor());
            if (!commands.isEmpty()) applyBatch(commands);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (Exception exception) {
            logger.warn("Minecraft whitelist bridge poll failed: {}", safeMessage(exception));
        }
    }

    private void applyBatch(List<Command> commands) throws java.io.IOException, InterruptedException {
        MinecraftBridgeStateStorage.State next = state;
        Acknowledgement failed = null;
        for (Command command : commands) {
            ApplyResult result = applyOnServerThread(command);
            if (!result.applied()) {
                failed = Acknowledgement.failed(command.sequence(), result.error());
                break;
            }
            next = next.applied(command.sequence());
        }
        if (next.cursor() != state.cursor()) {
            state = next;
            storage.save(JsonStateStorage.elarionRoot(requireServer()), state);
            flushPendingAcknowledgements();
        }
        if (failed != null) {
            client.acknowledge(List.of(failed));
        }
    }

    private void flushPendingAcknowledgements() throws InterruptedException, java.io.IOException {
        if (state.pendingAcknowledgements().isEmpty()) return;
        List<Acknowledgement> acknowledgements = state.pendingAcknowledgements().stream()
                .map(Acknowledgement::applied)
                .toList();
        client.acknowledge(acknowledgements);
        state = state.acknowledged();
        storage.save(JsonStateStorage.elarionRoot(requireServer()), state);
    }

    private ApplyResult applyOnServerThread(Command command) throws InterruptedException {
        MinecraftServer current = requireServer();
        CompletableFuture<ApplyResult> result = new CompletableFuture<>();
        current.execute(() -> {
            try {
                result.complete(apply(current, command));
            } catch (RuntimeException exception) {
                result.complete(new ApplyResult(false, safeMessage(exception)));
            }
        });
        try {
            return result.get(10, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException exception) {
            return new ApplyResult(false, "server_apply_timeout");
        }
    }

    private ApplyResult apply(MinecraftServer server, Command command) {
        if (!server.getPlayerManager().isWhitelistEnabled()) {
            return new ApplyResult(false, "whitelist_disabled");
        }
        GameProfile profile = new GameProfile(command.minecraftUuid(), command.minecraftName());
        Whitelist whitelist = server.getPlayerManager().getWhitelist();
        WhitelistEntry existing = whitelist.get(profile);
        if (command.action() == MinecraftBridgeProtocol.Action.ADD) {
            if (existing == null) whitelist.add(new WhitelistEntry(profile));
            return ApplyResult.success();
        }
        if (existing != null) whitelist.remove(profile);
        ServerPlayerEntity online = server.getPlayerManager().getPlayer(command.minecraftUuid());
        if (online != null) {
            online.networkHandler.disconnect(Text.literal("Your Elarion whitelist access has been removed."));
        }
        return ApplyResult.success();
    }

    private MinecraftServer requireServer() {
        MinecraftServer current = server;
        if (!running || current == null) throw new IllegalStateException("bridge_stopped");
        return current;
    }

    private static String safeMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) return throwable.getClass().getSimpleName();
        return message.substring(0, Math.min(200, message.length()));
    }

    private record ApplyResult(boolean applied, String error) {
        static ApplyResult success() {
            return new ApplyResult(true, null);
        }
    }
}
