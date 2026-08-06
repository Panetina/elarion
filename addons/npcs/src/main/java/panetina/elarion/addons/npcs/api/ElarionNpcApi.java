package panetina.elarion.addons.npcs.api;

import panetina.elarion.addons.npcs.service.NpcDefinitionService;
import panetina.elarion.addons.npcs.service.NpcInteractionService;
import panetina.elarion.addons.npcs.service.NpcPlacementService;
import panetina.elarion.addons.npcs.entity.ElarionNpcEntity;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.addons.npcs.network.NpcQuestMarkerSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.registry.ActionHandler;
import panetina.elarion.core.registry.ActionType;
import panetina.elarion.core.registry.ConditionHandler;
import panetina.elarion.core.registry.ConditionType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ElarionNpcApi {
    private static ElarionNpcApi instance;

    private final ElarionApi core;
    private final NpcDefinitionService definitions;
    private final NpcPlacementService placements;
    private final NpcInteractionService interactions;
    private final NpcReputationApi reputation;

    public ElarionNpcApi(
            ElarionApi core,
            NpcDefinitionService definitions,
            NpcPlacementService placements,
            NpcInteractionService interactions,
            NpcReputationApi reputation
    ) {
        if (instance != null) throw new IllegalStateException("ElarionNpcApi is already initialized");
        this.core = core;
        this.definitions = definitions;
        this.placements = placements;
        this.interactions = interactions;
        this.reputation = reputation;
        instance = this;
    }

    public static ElarionNpcApi get() {
        if (instance == null) throw new IllegalStateException("Elarion NPCs has not initialized yet");
        return instance;
    }

    public NpcDefinitionService definitions() {
        return definitions;
    }

    public NpcPlacementService placements() {
        return placements;
    }

    public NpcInteractionService interactions() {
        return interactions;
    }

    public NpcReputationApi reputation() {
        return reputation;
    }

    /** Stable query boundary for integrations that refer to a placed NPC. */
    public Optional<PlacedNpcRecord> findPlacement(UUID placementId) {
        return placements.find(placementId);
    }

    /** Resolves a placed-NPC identity without exposing the NPC entity class. */
    public Optional<UUID> placementId(Entity entity) {
        return entity instanceof ElarionNpcEntity npc ? npc.placedNpcId() : Optional.empty();
    }

    public Optional<MinecraftServer> server() {
        return placements.server();
    }

    public void onPlacementTopologyChanged(Runnable listener) {
        placements.onTopologyChanged(listener);
    }

    /** NPC owns the viewer-specific marker payload and its network contract. */
    public void syncQuestMarkers(ServerPlayerEntity player, Collection<UUID> placementIds) {
        if (player == null) return;
        List<UUID> ids = placementIds == null ? List.of() : List.copyOf(placementIds);
        ServerPlayNetworking.send(player, new NpcQuestMarkerSyncPayload(ids));
    }

    public void registerAction(String id, String owner, String description, ActionHandler handler) {
        core.registries().actions().register(new ActionType(id, owner, description));
        core.registries().registerActionHandler(id, handler);
    }

    public void registerCondition(String id, String owner, String description, ConditionHandler handler) {
        core.registries().conditions().register(new ConditionType(id, owner, description));
        core.registries().registerConditionHandler(id, handler);
    }
}
