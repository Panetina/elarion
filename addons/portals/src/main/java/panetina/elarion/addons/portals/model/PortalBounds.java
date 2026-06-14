package panetina.elarion.addons.portals.model;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public record PortalBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, PortalAxis axis) {
    public static PortalBounds between(BlockPos first, BlockPos second) {
        int minX = Math.min(first.getX(), second.getX());
        int minY = Math.min(first.getY(), second.getY());
        int minZ = Math.min(first.getZ(), second.getZ());
        int maxX = Math.max(first.getX(), second.getX());
        int maxY = Math.max(first.getY(), second.getY());
        int maxZ = Math.max(first.getZ(), second.getZ());
        int thinAxes = (minX == maxX ? 1 : 0) + (minY == maxY ? 1 : 0) + (minZ == maxZ ? 1 : 0);
        if (thinAxes != 1) {
            throw new IllegalArgumentException(
                    "Portal selection must be exactly one block thick on one axis and at least two blocks wide and tall.");
        }
        PortalAxis axis = minX == maxX ? PortalAxis.X : minY == maxY ? PortalAxis.Y : PortalAxis.Z;
        return new PortalBounds(minX, minY, minZ, maxX, maxY, maxZ, axis);
    }

    public boolean contains(BlockPos pos) {
        return pos.getX() >= minX && pos.getX() <= maxX
                && pos.getY() >= minY && pos.getY() <= maxY
                && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    public int volume() {
        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }

    public List<BlockPos> positions() {
        List<BlockPos> positions = new ArrayList<>(volume());
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) positions.add(new BlockPos(x, y, z));
            }
        }
        return List.copyOf(positions);
    }

    public boolean intersects(PortalBounds other) {
        return minX <= other.maxX && maxX >= other.minX
                && minY <= other.maxY && maxY >= other.minY
                && minZ <= other.maxZ && maxZ >= other.minZ;
    }
}
