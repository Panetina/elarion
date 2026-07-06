package panetina.elarion.core.api;

import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.config.ElarionConfigApplyRegistrar;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.registry.ElarionRegistries;
import panetina.elarion.core.service.AbilityService;
import panetina.elarion.core.service.ElarionTaskService;
import panetina.elarion.core.service.ElarionCollectionService;
import panetina.elarion.core.service.ElarionAdminPanelService;
import panetina.elarion.core.service.PlayerRestrictionService;

public final class ElarionSystemApi {
    private final AbilityService abilities;
    private final ElarionEventBus events;
    private final ElarionCommandRegistry commands;
    private final ElarionRegistries registries;
    private final ElarionTaskService tasks;
    private final ElarionCollectionService collections;
    private final ElarionAdminPanelService adminPanel;
    private final ElarionConfigRegistry configs;
    private final ElarionConfigApplyRegistrar configAppliers;
    private final PlayerRestrictionService restrictions;

    ElarionSystemApi(
            AbilityService abilities,
            ElarionEventBus events,
            ElarionCommandRegistry commands,
            ElarionRegistries registries,
            ElarionTaskService tasks,
            ElarionCollectionService collections,
            ElarionAdminPanelService adminPanel,
            ElarionConfigRegistry configs,
            ElarionConfigApplyRegistrar configAppliers,
            PlayerRestrictionService restrictions
    ) {
        this.abilities = abilities;
        this.events = events;
        this.commands = commands;
        this.registries = registries;
        this.tasks = tasks;
        this.collections = collections;
        this.adminPanel = adminPanel;
        this.configs = configs;
        this.configAppliers = configAppliers;
        this.restrictions = restrictions;
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
}
