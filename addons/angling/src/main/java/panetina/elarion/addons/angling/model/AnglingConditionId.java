package panetina.elarion.addons.angling.model;

import net.minecraft.util.Identifier;

import java.util.Objects;

public record AnglingConditionId(Identifier value) {
    public static final String NAMESPACE = "elarion_angling";

    public AnglingConditionId {
        Objects.requireNonNull(value, "value");
    }

    public static AnglingConditionId of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new FishDefinitionValidationException("Condition ID must not be blank.");
        }

        Identifier identifier = raw.indexOf(':') >= 0
                ? Identifier.tryParse(raw)
                : Identifier.tryParse(NAMESPACE + ":" + raw);
        if (identifier == null) {
            throw new FishDefinitionValidationException("Invalid condition ID: " + raw);
        }
        return new AnglingConditionId(identifier);
    }
}
