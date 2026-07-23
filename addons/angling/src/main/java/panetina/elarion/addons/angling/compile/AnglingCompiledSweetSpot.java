package panetina.elarion.addons.angling.compile;

import panetina.elarion.addons.angling.definition.AnglingSweetSpotDefinition;

import java.util.List;
import java.util.Objects;

/** Sweetspot definition with resolved behavior and typed on-hit modifiers. */
public record AnglingCompiledSweetSpot<M, S>(
        AnglingSweetSpotDefinition definition,
        S behavior,
        List<M> onHitModifiers
) {
    public AnglingCompiledSweetSpot {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(behavior, "behavior");
        onHitModifiers = List.copyOf(Objects.requireNonNull(onHitModifiers, "onHitModifiers"));
    }
}
