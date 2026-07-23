package panetina.elarion.addons.angling.domainmap;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

import java.util.concurrent.atomic.AtomicBoolean;

public final class AnglingDomainMaps {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();
    private static final AnglingDomainMapRepository REPOSITORY = new AnglingDomainMapRepository();

    private AnglingDomainMaps() {
    }

    public static void initialize() {
        if (INITIALIZED.compareAndSet(false, true)) {
            ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                    .registerReloadListener(new AnglingDomainMapReloadListener(REPOSITORY));
        }
    }

    public static AnglingDomainMapSnapshot snapshot() {
        return REPOSITORY.current();
    }
}
