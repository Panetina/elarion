package panetina.elarion.addons.angling.fishing;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

import java.util.Objects;

/** Explicit lifecycle bridge between statically registered items and the world-bound fishing service. */
public final class AnglingFishingRuntime {
    /** Flips only after client screen/rendering and remaining catch modifiers pass parity acceptance. */
    public static final boolean GAMEPLAY_RELEASE_GATE = false;
    private static AnglingFishingService service;

    private AnglingFishingRuntime() {
    }

    public static synchronized void bind(AnglingFishingService service) {
        if (AnglingFishingRuntime.service != null) {
            throw new IllegalStateException("Angling fishing runtime is already bound");
        }
        AnglingFishingRuntime.service = Objects.requireNonNull(service, "service");
    }

    public static synchronized void unbind(AnglingFishingService expected) {
        if (service == expected) service = null;
    }

    public static boolean use(ServerPlayerEntity player, Hand hand, ItemStack rod) {
        AnglingFishingService current;
        synchronized (AnglingFishingRuntime.class) {
            current = service;
        }
        return GAMEPLAY_RELEASE_GATE && current != null && current.use(player, hand, rod);
    }
}
