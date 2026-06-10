package panetina.elarion.core.registry;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.api.ElarionApi;

import java.util.Map;
import java.util.UUID;

public record RegistryExecutionContext(
        ElarionApi api,
        MinecraftServer server,
        ServerPlayerEntity actor,
        UUID actorId,
        String actorRealmId,
        UUID targetId,
        String targetRealmId,
        String worldId,
        String sourceAddon,
        Map<String, String> metadata
) {
    public RegistryExecutionContext {
        actorRealmId = actorRealmId == null ? "" : actorRealmId;
        targetRealmId = targetRealmId == null ? "" : targetRealmId;
        worldId = worldId == null ? "" : worldId;
        sourceAddon = sourceAddon == null ? "elarion_core" : sourceAddon;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static RegistryExecutionContext server(ElarionApi api, MinecraftServer server, String sourceAddon) {
        return new RegistryExecutionContext(
                api, server, null, null, "", null, "", "", sourceAddon, Map.of());
    }

    public RegistryExecutionContext withMetadata(Map<String, String> metadata) {
        return new RegistryExecutionContext(
                api, server, actor, actorId, actorRealmId, targetId, targetRealmId, worldId, sourceAddon, metadata);
    }
}
