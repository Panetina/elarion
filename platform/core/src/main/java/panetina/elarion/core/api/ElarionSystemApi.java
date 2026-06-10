package panetina.elarion.core.api;

import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.registry.ElarionRegistries;
import panetina.elarion.core.service.AbilityService;
import panetina.elarion.core.service.ElarionTaskService;

public final class ElarionSystemApi {
    private final AbilityService abilities;
    private final ElarionEventBus events;
    private final ElarionCommandRegistry commands;
    private final ElarionRegistries registries;
    private final ElarionTaskService tasks;

    ElarionSystemApi(
            AbilityService abilities,
            ElarionEventBus events,
            ElarionCommandRegistry commands,
            ElarionRegistries registries,
            ElarionTaskService tasks
    ) {
        this.abilities = abilities;
        this.events = events;
        this.commands = commands;
        this.registries = registries;
        this.tasks = tasks;
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
}
