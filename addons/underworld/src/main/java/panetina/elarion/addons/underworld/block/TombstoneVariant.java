package panetina.elarion.addons.underworld.block;

import java.util.Locale;

public enum TombstoneVariant {
    TOMBSTONE_1(0, "tombstone_1", 1, 2, 1, 0.5, 0.0, 6.0, 15.5, 17.0, 11.0),
    TOMBSTONE_2(1, "tombstone_2", 1, 2, 1, 0.5, 0.0, 6.0, 15.5, 18.0, 11.0),
    TOMBSTONE_3(2, "tombstone_3", 1, 2, 1, 0.5, 0.0, 6.0, 15.5, 30.0, 13.0);

    private static final TombstoneVariant[] VALUES = values();

    private final int blockStateId;
    private final String id;
    private final int widthBlocks;
    private final int heightBlocks;
    private final int depthBlocks;
    private final double minX;
    private final double minY;
    private final double minZ;
    private final double maxX;
    private final double maxY;
    private final double maxZ;

    TombstoneVariant(
            int blockStateId,
            String id,
            int widthBlocks,
            int heightBlocks,
            int depthBlocks,
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ
    ) {
        this.blockStateId = blockStateId;
        this.id = id;
        this.widthBlocks = widthBlocks;
        this.heightBlocks = heightBlocks;
        this.depthBlocks = depthBlocks;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public int blockStateId() {
        return blockStateId;
    }

    public String id() {
        return id;
    }

    public int widthBlocks() {
        return widthBlocks;
    }

    public int heightBlocks() {
        return heightBlocks;
    }

    public int depthBlocks() {
        return depthBlocks;
    }

    public double minX() {
        return minX;
    }

    public double minY() {
        return minY;
    }

    public double minZ() {
        return minZ;
    }

    public double maxX() {
        return maxX;
    }

    public double maxY() {
        return maxY;
    }

    public double maxZ() {
        return maxZ;
    }

    public static TombstoneVariant byBlockStateId(int id) {
        for (TombstoneVariant variant : VALUES) {
            if (variant.blockStateId == id) return variant;
        }
        return TOMBSTONE_1;
    }

    public static TombstoneVariant byId(String id) {
        if (id == null || id.isBlank()) return TOMBSTONE_1;
        String normalized = id.toLowerCase(Locale.ROOT);
        for (TombstoneVariant variant : VALUES) {
            if (variant.id.equals(normalized)) return variant;
        }
        return TOMBSTONE_1;
    }

    public static TombstoneVariant forRealm(String realmId, String fallbackSeed) {
        String seed = realmId == null || realmId.isBlank() ? fallbackSeed : realmId;
        if (seed == null || seed.isBlank()) return TOMBSTONE_1;
        return VALUES[Math.floorMod(seed.hashCode(), VALUES.length)];
    }
}
