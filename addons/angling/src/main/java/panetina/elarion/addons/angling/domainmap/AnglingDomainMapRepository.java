package panetina.elarion.addons.angling.domainmap;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.modifier.AnglingCompiledModifier;
import panetina.elarion.addons.angling.treasure.AnglingTreasureDefinition;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/** Publishes both completed simple domain maps in one atomic reload snapshot. */
final class AnglingDomainMapRepository {
    private final AtomicReference<AnglingDomainMapSnapshot> current = new AtomicReference<>(AnglingDomainMapSnapshot.EMPTY);

    AnglingDomainMapSnapshot current() {
        return current.get();
    }

    void publish(
            Map<AnglingRegistrySelector, AnglingAquariumInteraction> aquarium,
            Map<AnglingRegistrySelector, Identifier> tackleSkins,
            Map<AnglingRegistrySelector, List<AnglingCompiledModifier>> itemModifiers,
            Map<AnglingRegistrySelector, List<AnglingCompiledModifier>> effectModifiers,
            Map<AnglingRegistrySelector, AnglingTreasureDefinition> treasures
    ) {
        long revision = Math.addExact(current.get().revision(), 1);
        current.set(new AnglingDomainMapSnapshot(
                revision, aquarium, tackleSkins, itemModifiers, effectModifiers, treasures));
    }
}
