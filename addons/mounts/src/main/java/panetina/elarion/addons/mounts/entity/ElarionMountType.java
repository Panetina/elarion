package panetina.elarion.addons.mounts.entity;

import panetina.elarion.core.model.ElarionCollectionRank;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum ElarionMountType {
    AIRSHIP("airship", "Airship", "airship", "flight_airship.png", 1.0F,
            new MovementProfile(0.28D, -0.10D, 1.30D, 0.028D, 0.024D, 0.94D, 0.86D, 0.16D, 0.007D, 0.16D, 0.16D, 3.0D)),
    BEE("bee", "Bee", "bee", "flight_bee.png", 1.0F,
            new MovementProfile(0.42D, -0.18D, 1.45D, 0.060D, 0.050D, 0.89D, 0.74D, 0.42D, 0.018D, 0.28D, 0.26D, 3.0D)),
    CHINESE_DRAGON("chinese_dragon", "Chinese Dragon", "chinesedragon", "flight_chinesedragon_body.png", 1.3F, 0.0D, -0.5D,
            new MovementProfile(0.46D, -0.14D, 1.35D, 0.045D, 0.035D, 0.91D, 0.72D, 0.20D, 0.010D, 0.22D, 0.22D, 3.0D)),
    GHAST("ghast", "Ghast", "ghast", "flight_ghast.png", 1.0F,
            new MovementProfile(0.28D, -0.10D, 1.30D, 0.028D, 0.024D, 0.94D, 0.86D, 0.16D, 0.007D, 0.16D, 0.16D, 3.0D)),
    HOT_AIR_BALLOON("hot_air_balloon", "Hot Air Balloon", "hotairballoon", "flight_hotairballoon.png", 1.0F,
            new MovementProfile(0.28D, -0.10D, 1.30D, 0.028D, 0.024D, 0.94D, 0.86D, 0.16D, 0.007D, 0.16D, 0.16D, 3.0D)),
    SCIFI_BIKE("scifi_bike", "Sci-Fi Bike", "scifibike", "flight_scifibike.png", 1.0F,
            new MovementProfile(0.58D, -0.20D, 1.55D, 0.070D, 0.060D, 0.88D, 0.70D, 0.32D, 0.016D, 0.23D, 0.23D, 3.0D)),
    WYVERN("wyvern", "Wyvern", "wyvern", "flight_wyvern.png", 1.0F,
            new MovementProfile(0.50D, -0.15D, 1.50D, 0.055D, 0.045D, 0.90D, 0.73D, 0.28D, 0.014D, 0.25D, 0.24D, 3.0D));

    private final String id;
    private final String label;
    private final String modelId;
    private final String textureFileName;
    private final float renderScale;
    private final double renderAnchorX;
    private final double renderAnchorZ;
    private final MovementProfile movementProfile;
    private final RiderSeatProfile riderSeatProfile;
    private final CameraProfile cameraProfile;

    ElarionMountType(
            String id,
            String label,
            String modelId,
            String textureFileName,
            float renderScale,
            MovementProfile movementProfile
    ) {
        this(id, label, modelId, textureFileName, renderScale, Double.NaN, Double.NaN, movementProfile);
    }

    ElarionMountType(
            String id,
            String label,
            String modelId,
            String textureFileName,
            float renderScale,
            double renderAnchorX,
            double renderAnchorZ,
            MovementProfile movementProfile
    ) {
        this.id = id;
        this.label = label;
        this.modelId = modelId;
        this.textureFileName = textureFileName;
        this.renderScale = renderScale;
        this.renderAnchorX = renderAnchorX;
        this.renderAnchorZ = renderAnchorZ;
        this.movementProfile = movementProfile;
        this.riderSeatProfile = RiderSeatProfile.forType(this);
        this.cameraProfile = CameraProfile.forType(this);
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public String modelId() {
        return modelId;
    }

    public String geoFileName() {
        return "mount_" + modelId + ".geo.json";
    }

    public String animationFileName() {
        return "mount_" + modelId + ".animation.json";
    }

    public String textureFileName() {
        return textureFileName;
    }

    public float renderScale() {
        return renderScale;
    }

    public double passengerYOffset() {
        return riderSeatProfile.serverYOffset();
    }

    public double riderVisualYOffset() {
        return riderSeatProfile.visualYOffset();
    }

    public RiderSeatProfile riderSeatProfile() {
        return riderSeatProfile;
    }

    public CameraProfile cameraProfile() {
        return cameraProfile;
    }

    public double renderAnchorX(double fallback) {
        return Double.isNaN(renderAnchorX) ? fallback : renderAnchorX;
    }

    public double renderAnchorZ(double fallback) {
        return Double.isNaN(renderAnchorZ) ? fallback : renderAnchorZ;
    }

    public String itemId() {
        return id + "_whistle";
    }

    public MovementProfile movementProfile() {
        return movementProfile;
    }

    public String collectionRankLabel() {
        return collectionRank().label();
    }

    public int collectionRankColor() {
        return collectionRank().color();
    }

    public ElarionCollectionRank collectionRank() {
        if (this == SCIFI_BIKE) return ElarionCollectionRank.LEGENDARY;
        if (this == AIRSHIP || this == GHAST || this == HOT_AIR_BALLOON) return ElarionCollectionRank.COMMON;
        return ElarionCollectionRank.UNCOMMON;
    }

    public static ElarionMountType byId(String id) {
        if (id == null || id.isBlank()) {
            return BEE;
        }
        String normalized = id.toLowerCase(Locale.ROOT);
        Optional<ElarionMountType> match = Arrays.stream(values())
                .filter(type -> type.id.equals(normalized) || type.modelId.equals(normalized))
                .findFirst();
        return match.orElse(BEE);
    }

    public record MovementProfile(
            double maxForwardSpeed,
            double maxReverseSpeed,
            double boostMultiplier,
            double acceleration,
            double brake,
            double horizontalDrag,
            double verticalDrag,
            double turnDegrees,
            double turnResponse,
            double ascendSpeed,
            double descendSpeed,
            double minClearanceBlocks
    ) {
        public double boostedForwardSpeed() {
            return maxForwardSpeed * boostMultiplier;
        }
    }

    public record CameraProfile(
            float thirdPersonDistance,
            float boostDistanceBonus,
            float boostFovBonus
    ) {
        private static CameraProfile forType(ElarionMountType type) {
            return switch (type) {
                case AIRSHIP -> new CameraProfile(8.0F, 2.0F, 0.07F);
                case BEE -> new CameraProfile(7.0F, 1.6F, 0.08F);
                case CHINESE_DRAGON -> new CameraProfile(10.0F, 2.6F, 0.08F);
                case GHAST -> new CameraProfile(7.5F, 1.8F, 0.06F);
                case HOT_AIR_BALLOON -> new CameraProfile(8.5F, 1.5F, 0.05F);
                case SCIFI_BIKE -> new CameraProfile(8.0F, 2.4F, 0.08F);
                case WYVERN -> new CameraProfile(8.4F, 2.4F, 0.09F);
            };
        }
    }

    public record RiderSeatProfile(
            double serverYOffset,
            double serverXOffset,
            double serverZOffset,
            double visualXOffset,
            double visualYOffset,
            double visualZOffset,
            float visualYawOffset,
            List<String> firstPersonHiddenBones
    ) {
        private static RiderSeatProfile forType(ElarionMountType type) {
            return switch (type) {
                case AIRSHIP -> new RiderSeatProfile(0.0D, 0.0D, -1.80D, 0.0D, 0.02D, 0.0D, 0.0F, List.of());
                case BEE -> new RiderSeatProfile(0.0D, 0.0D, -1.40D, 0.0D, 1.05D, 0.0D, 0.0F, List.of());
                case CHINESE_DRAGON -> new RiderSeatProfile(0.08D, 0.0D, -1.80D, 0.0D, 0.445D, -0.18D, 0.0F, List.of(
                        "seg_1",
                        "seg_2",
                        "seg_3",
                        "seg_4",
                        "seg_5",
                        "seg_6",
                        "seg_7",
                        "seg_8",
                        "seg_9",
                        "seg_10",
                        "seg_11",
                        "seg_12",
                        "seg_13",
                        "front_left_arm",
                        "front_left_forearm",
                        "front_left_paw",
                        "front_right_arm",
                        "front_right_forearm",
                        "front_right_paw"));
                case GHAST -> new RiderSeatProfile(0.0D, 0.0D, -1.40D, 0.0D, 3.00D, 0.0D, 0.0F, List.of());
                case HOT_AIR_BALLOON -> new RiderSeatProfile(0.0D, 0.0D, -1.40D, 0.0D, 0.12D, 0.35D, 0.0F, List.of());
                case SCIFI_BIKE -> new RiderSeatProfile(0.0D, 0.0D, 1.40D, 0.0D, 0.52D, 1.75D, 0.0F, List.of());
                case WYVERN -> new RiderSeatProfile(0.0D, 0.0D, -1.80D, 0.0D, 0.94D, 0.0D, 0.0F, List.of());
            };
        }
    }
}
