package panetina.elarion.addons.portals.model;

import net.minecraft.util.math.BlockPos;

public record PortalSelection(String worldId, BlockPos first, BlockPos second) {
    public PortalSelection withFirst(String world, BlockPos pos) {
        return new PortalSelection(world, pos.toImmutable(), world.equals(worldId) ? second : null);
    }

    public PortalSelection withSecond(String world, BlockPos pos) {
        return new PortalSelection(world, world.equals(worldId) ? first : null, pos.toImmutable());
    }

    public boolean complete() {
        return first != null && second != null && !worldId.isBlank();
    }
}
