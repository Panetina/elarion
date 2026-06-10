package panetina.elarion.core.model;

import java.util.Locale;

public record ProgressionRegion(
        String id,
        String world,
        double minX,
        double minY,
        double minZ,
        double maxX,
        double maxY,
        double maxZ
) {
    public ProgressionRegion {
        id = normalize(id);
        world = world == null ? "" : world.trim().toLowerCase(Locale.ROOT);
        double lowX = Math.min(minX, maxX);
        double lowY = Math.min(minY, maxY);
        double lowZ = Math.min(minZ, maxZ);
        double highX = Math.max(minX, maxX);
        double highY = Math.max(minY, maxY);
        double highZ = Math.max(minZ, maxZ);
        minX = lowX;
        minY = lowY;
        minZ = lowZ;
        maxX = highX;
        maxY = highY;
        maxZ = highZ;
    }

    public boolean contains(String worldId, double x, double y, double z) {
        String normalizedWorld = worldId == null ? "" : worldId.trim().toLowerCase(Locale.ROOT);
        return world.equals(normalizedWorld)
                && x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
    }
}
