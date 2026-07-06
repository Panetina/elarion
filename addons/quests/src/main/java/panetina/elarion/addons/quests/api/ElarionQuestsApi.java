package panetina.elarion.addons.quests.api;

import panetina.elarion.addons.quests.service.QuestDefinitionService;
import panetina.elarion.addons.quests.service.QuestStateService;

public final class ElarionQuestsApi {
    private static ElarionQuestsApi instance;
    private final QuestDefinitionService definitions;
    private final QuestStateService states;

    public ElarionQuestsApi(QuestDefinitionService definitions, QuestStateService states) {
        if (instance != null) throw new IllegalStateException("ElarionQuestsApi is already initialized");
        this.definitions = definitions;
        this.states = states;
        instance = this;
    }

    public static ElarionQuestsApi get() {
        if (instance == null) throw new IllegalStateException("Elarion quests has not initialized yet");
        return instance;
    }

    public QuestDefinitionService definitions() {
        return definitions;
    }

    public QuestStateService states() {
        return states;
    }
}
