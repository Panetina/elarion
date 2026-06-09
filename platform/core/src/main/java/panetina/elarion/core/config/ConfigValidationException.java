package panetina.elarion.core.config;

import java.util.List;

public final class ConfigValidationException extends IllegalArgumentException {
    private final List<String> errors;

    public ConfigValidationException(List<String> errors) {
        super("Elarion configuration is invalid:\n - " + String.join("\n - ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
