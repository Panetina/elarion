package panetina.elarion.addons.npcs.api;

import panetina.elarion.addons.npcs.service.NpcDefinitionService;
import panetina.elarion.addons.npcs.service.NpcInteractionService;
import panetina.elarion.addons.npcs.service.NpcPlacementService;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.registry.ActionHandler;
import panetina.elarion.core.registry.ActionType;
import panetina.elarion.core.registry.ConditionHandler;
import panetina.elarion.core.registry.ConditionType;

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

    public void registerAction(String id, String owner, String description, ActionHandler handler) {
        core.registries().actions().register(new ActionType(id, owner, description));
        core.registries().registerActionHandler(id, handler);
    }

    public void registerCondition(String id, String owner, String description, ConditionHandler handler) {
        core.registries().conditions().register(new ConditionType(id, owner, description));
        core.registries().registerConditionHandler(id, handler);
    }
}
