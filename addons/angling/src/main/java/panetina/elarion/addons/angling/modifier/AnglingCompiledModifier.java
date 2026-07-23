package panetina.elarion.addons.angling.modifier;

import net.minecraft.util.Identifier;

import java.util.Objects;

/** Keeps the stable dispatch identity beside its fully decoded immutable value. */
public record AnglingCompiledModifier(Identifier type, AnglingModifierValue value) {
    public AnglingCompiledModifier {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(value, "value");
    }
}
