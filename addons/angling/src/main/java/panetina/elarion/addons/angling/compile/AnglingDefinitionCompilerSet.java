package panetina.elarion.addons.angling.compile;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.definition.AnglingCatchDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Three explicit ownership boundaries for restriction, modifier, and sweetspot compilation. */
public record AnglingDefinitionCompilerSet<R, M, S>(
        AnglingTypedCompilerRegistry<R> restrictions,
        AnglingTypedCompilerRegistry<M> modifiers,
        AnglingIdentifierRegistry<S> sweetspotBehaviors
) {
    public AnglingDefinitionCompilerSet {
        Objects.requireNonNull(restrictions, "restrictions");
        Objects.requireNonNull(modifiers, "modifiers");
        Objects.requireNonNull(sweetspotBehaviors, "sweetspotBehaviors");
    }

    public AnglingCompiledCatchDefinition<R, M, S> compile(
            Identifier definitionId,
            AnglingCatchDefinition definition
    ) {
        Objects.requireNonNull(definitionId, "definitionId");
        Objects.requireNonNull(definition, "definition");
        List<R> compiledRestrictions = restrictions.compileAll(definitionId, definition.restrictions());
        List<M> compiledModifiers = modifiers.compileAll(definitionId, definition.difficulty().modifiers());
        List<AnglingCompiledSweetSpot<M, S>> compiledSweetspots = new ArrayList<>(
                definition.difficulty().sweetspots().size());
        for (var sweetspot : definition.difficulty().sweetspots()) {
            S behavior = sweetspotBehaviors.require(definitionId, sweetspot.sweetspotType());
            List<M> onHit = modifiers.compileAll(definitionId, sweetspot.modifiers());
            compiledSweetspots.add(new AnglingCompiledSweetSpot<>(sweetspot, behavior, onHit));
        }
        return new AnglingCompiledCatchDefinition<>(definition, compiledRestrictions, compiledModifiers,
                compiledSweetspots);
    }
}
