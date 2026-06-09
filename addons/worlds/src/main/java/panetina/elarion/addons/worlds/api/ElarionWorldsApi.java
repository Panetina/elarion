package panetina.elarion.addons.worlds.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import panetina.elarion.addons.worlds.model.ManagedWorldDefinition;
import panetina.elarion.addons.worlds.service.WorldService;

import java.util.Map;

public final class ElarionWorldsApi {
    private static ElarionWorldsApi instance;
    private final WorldService worlds;

    public ElarionWorldsApi(WorldService worlds) {
        if (instance != null) throw new IllegalStateException("ElarionWorldsApi is already initialized");
        this.worlds = worlds;
        instance = this;
    }

    public static ElarionWorldsApi get() {
        if (instance == null) throw new IllegalStateException("Elarion Worlds has not initialized yet");
        return instance;
    }

    public Map<String, ManagedWorldDefinition> definitions() {
        return worlds.definitions();
    }

    public ManagedWorldDefinition definition(ServerWorld world) {
        return worlds.definition(world);
    }

    public ServerWorld resolve(String nameOrId) {
        return worlds.resolveWorld(nameOrId);
    }

    public ServerWorld open(String nameOrId) {
        ManagedWorldDefinition definition = worlds.findDefinition(nameOrId);
        return definition == null ? null : worlds.open(definition);
    }

    public boolean teleportToSpawn(ServerPlayerEntity player, String nameOrId) {
        return worlds.teleport(player, nameOrId);
    }
}
