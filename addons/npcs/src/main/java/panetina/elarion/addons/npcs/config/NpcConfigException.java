package panetina.elarion.addons.npcs.config;

import java.util.List;

public final class NpcConfigException extends RuntimeException {
    private final List<String> errors;

    public NpcConfigException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
