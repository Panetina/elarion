package panetina.elarion.addons.angling.definition;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

import java.util.concurrent.atomic.AtomicBoolean;

/** Server-data reload registration for the atomic native catch snapshot. */
public final class AnglingCatchResources {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();
    private static final AnglingCatchSnapshotRepository REPOSITORY = new AnglingCatchSnapshotRepository();

    private AnglingCatchResources() {
    }

    public static void initialize() {
        if (INITIALIZED.compareAndSet(false, true)) {
            ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                    .registerReloadListener(new AnglingCatchResourceReloadListener(REPOSITORY));
        }
    }

    public static AnglingCatchSnapshot snapshot() {
        return REPOSITORY.current();
    }
}
