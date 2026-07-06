package panetina.elarion.addons.portals.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import panetina.elarion.addons.portals.network.PortalVisualSyncPayload;

import java.util.List;

public final class PortalClientVisuals {
    private static volatile List<PortalVisualSyncPayload.Entry> entries = List.of();

    private PortalClientVisuals() {
    }

    public static void replace(PortalVisualSyncPayload payload) {
        entries = payload.entries();
    }

    public static void clear() {
        entries = List.of();
    }

    public static int color(BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || pos == null) return 0xFFFFFFFF;
        String world = client.world.getRegistryKey().getValue().toString();
        for (PortalVisualSyncPayload.Entry entry : entries) {
            if (entry.active() && entry.worldId().equals(world) && entry.bounds().contains(pos)) {
                return entry.argb();
            }
        }
        return 0xFFFFFFFF;
    }
}
