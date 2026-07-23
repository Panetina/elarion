package panetina.elarion.addons.angling.compile;

import panetina.elarion.addons.angling.definition.AnglingCatchDefinition;

import java.util.List;
import java.util.Objects;

/**
 * Fully compiled reload product. Runtime code consumes these typed lists and
 * never reparses the raw polymorphic nodes retained in the source DTO.
 */
public record AnglingCompiledCatchDefinition<R, M, S>(
        AnglingCatchDefinition source,
        List<R> restrictions,
        List<M> minigameModifiers,
        List<AnglingCompiledSweetSpot<M, S>> sweetspots
) {
    public AnglingCompiledCatchDefinition {
        Objects.requireNonNull(source, "source");
        restrictions = List.copyOf(Objects.requireNonNull(restrictions, "restrictions"));
        minigameModifiers = List.copyOf(Objects.requireNonNull(minigameModifiers, "minigameModifiers"));
        sweetspots = List.copyOf(Objects.requireNonNull(sweetspots, "sweetspots"));
    }
}
