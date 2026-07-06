package panetina.elarion.addons.quests.config;

import java.util.List;

public final class QuestConfigException extends RuntimeException {
    private final List<String> errors;

    public QuestConfigException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
