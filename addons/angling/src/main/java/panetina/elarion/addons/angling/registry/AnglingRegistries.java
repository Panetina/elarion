package panetina.elarion.addons.angling.registry;

import panetina.elarion.addons.angling.component.AnglingDataComponents;
import panetina.elarion.addons.angling.component.AnglingAttachments;
import panetina.elarion.addons.angling.definition.AnglingCatchResources;
import panetina.elarion.addons.angling.domainmap.AnglingDomainMaps;

/** Explicit server-safe bootstrap order for completed Fabric registry slices. */
public final class AnglingRegistries {
    private AnglingRegistries() {
    }

    public static void initialize() {
        AnglingDataComponents.initialize();
        AnglingAttachments.initialize();
        AnglingItems.initialize();
        AnglingSounds.initialize();
        AnglingParticles.initialize();
        AnglingEntities.initialize();
        AnglingCatchResources.initialize();
        AnglingDomainMaps.initialize();
    }
}
