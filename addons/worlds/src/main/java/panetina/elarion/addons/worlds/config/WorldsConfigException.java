package panetina.elarion.addons.worlds.config;

import java.util.List;

public final class WorldsConfigException extends RuntimeException {
    private final List<String> errors;

    public WorldsConfigException(List<String> errors) {
        super("Invalid Elarion Worlds configuration:\n - " + String.join("\n - ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
