package panetina.elarion.core.api;

import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.config.ElarionConfigApplyRegistrar;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.registry.ElarionRegistries;
import panetina.elarion.core.service.AbilityService;
import panetina.elarion.core.service.ElarionTaskService;
import panetina.elarion.core.service.ElarionCollectionService;
import panetina.elarion.core.service.CitizenProfileService;
import panetina.elarion.core.service.ElarionAdminPanelService;
import panetina.elarion.core.service.PlayerRestrictionService;
import panetina.elarion.core.placeholder.ElarionPlaceholderService;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionPublisher;
import panetina.elarion.core.api.reset.PlayerResetRegistry;
import panetina.elarion.core.api.reset.WorldResetRegistry;
import panetina.elarion.core.service.WorldResetService;

public final class ElarionSystemApi {
    private final AbilityService abilities;
    private final ElarionEventBus events;
    private final ElarionCommandRegistry commands;
    private final ElarionRegistries registries;
    private final ElarionTaskService tasks;
    private final ElarionCollectionService collections;
    private final CitizenProfileService profiles;
    private final ElarionAdminPanelService adminPanel;
    private final ElarionConfigRegistry configs;
    private final ElarionConfigApplyRegistrar configAppliers;
    private final PlayerRestrictionService restrictions;
    private final ElarionPlaceholderService placeholders;
    private final MinecraftProjectionPublisher webProjections;
    private final PlayerResetRegistry playerResets;
    private final WorldResetRegistry worldResets;
    private final WorldResetService worldResetService;

    ElarionSystemApi(
            AbilityService abilities,
            ElarionEventBus events,
            ElarionCommandRegistry commands,
            ElarionRegistries registries,
            ElarionTaskService tasks,
            ElarionCollectionService collections,
            CitizenProfileService profiles,
            ElarionAdminPanelService adminPanel,
            ElarionConfigRegistry configs,
            ElarionConfigApplyRegistrar configAppliers,
            PlayerRestrictionService restrictions,
            ElarionPlaceholderService placeholders,
            MinecraftProjectionPublisher webProjections,
            PlayerResetRegistry playerResets,
            WorldResetRegistry worldResets,
            WorldResetService worldResetService
    ) {
        this.abilities = abilities;
        this.events = events;
        this.commands = commands;
        this.registries = registries;
        this.tasks = tasks;
        this.collections = collections;
        this.profiles = profiles;
        this.adminPanel = adminPanel;
        this.configs = configs;
        this.configAppliers = configAppliers;
        this.restrictions = restrictions;
        this.placeholders = placeholders;
        this.webProjections = webProjections;
        this.playerResets = playerResets;
        this.worldResets = worldResets;
        this.worldResetService = worldResetService;
    }

    public AbilityService abilities() {
        return abilities;
    }

    public ElarionEventBus events() {
        return events;
    }

    public ElarionCommandRegistry commands() {
        return commands;
    }

    public ElarionRegistries registries() {
        return registries;
    }

    public ElarionTaskService tasks() {
        return tasks;
    }

    public ElarionCollectionService collections() {
        return collections;
    }

    public CitizenProfileService profiles() {
        return profiles;
    }

    public ElarionAdminPanelService adminPanel() {
        return adminPanel;
    }

    public ElarionConfigRegistry configs() {
        return configs;
    }

    public ElarionConfigApplyRegistrar configAppliers() {
        return configAppliers;
    }

    public PlayerRestrictionService restrictions() {
        return restrictions;
    }

    public ElarionPlaceholderService placeholders() {
        return placeholders;
    }

    /**
     * Publishes bounded read-model projections. Core remains the canonical owner of game state.
     */
    public MinecraftProjectionPublisher webProjections() {
        return webProjections;
    }

    public PlayerResetRegistry playerResets() {
        return playerResets;
    }

    public WorldResetRegistry worldResets() {
        return worldResets;
    }

    public WorldResetService worldResetService() {
        return worldResetService;
    }
}
