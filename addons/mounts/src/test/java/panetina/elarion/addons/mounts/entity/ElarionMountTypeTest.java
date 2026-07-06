package panetina.elarion.addons.mounts.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElarionMountTypeTest {
    @Test
    void resolvesByStableMountId() {
        assertEquals(ElarionMountType.CHINESE_DRAGON, ElarionMountType.byId("chinese_dragon"));
        assertEquals(ElarionMountType.SCIFI_BIKE, ElarionMountType.byId("scifi_bike"));
    }

    @Test
    void resolvesBySourceModelId() {
        assertEquals(ElarionMountType.HOT_AIR_BALLOON, ElarionMountType.byId("hotairballoon"));
        assertEquals(ElarionMountType.CHINESE_DRAGON, ElarionMountType.byId("chinesedragon"));
        assertEquals(ElarionMountType.SCIFI_BIKE, ElarionMountType.byId("scifibike"));
    }

    @Test
    void chineseDragonUsesHeadSeatAnchorAndLargerScale() {
        assertEquals(1.3F, ElarionMountType.CHINESE_DRAGON.renderScale());
        assertEquals(0.08D, ElarionMountType.CHINESE_DRAGON.passengerYOffset());
        assertEquals(0.445D, ElarionMountType.CHINESE_DRAGON.riderVisualYOffset());
        assertEquals(0.0F, ElarionMountType.CHINESE_DRAGON.riderSeatProfile().visualYawOffset());
        assertEquals(-0.5D, ElarionMountType.CHINESE_DRAGON.renderAnchorZ(0.0D));
        assertEquals(0.46D, ElarionMountType.CHINESE_DRAGON.movementProfile().maxForwardSpeed());
        assertEquals(3.0D, ElarionMountType.CHINESE_DRAGON.movementProfile().minClearanceBlocks());
        assertTrue(ElarionMountType.CHINESE_DRAGON.riderSeatProfile().firstPersonHiddenBones().contains("seg_13"));
    }

    @Test
    void exposesRuntimeAssetNamesForAllMounts() {
        assertEquals("mount_bee.geo.json", ElarionMountType.BEE.geoFileName());
        assertEquals("mount_bee.animation.json", ElarionMountType.BEE.animationFileName());
        assertEquals("flight_bee.png", ElarionMountType.BEE.textureFileName());
        for (ElarionMountType type : ElarionMountType.values()) {
            assertEquals(type.id() + "_whistle", type.itemId());
        }
    }

    @Test
    void convertedMountsUseModelTrueScaleAndPassengerMarkerSeatByDefault() {
        for (ElarionMountType type : ElarionMountType.values()) {
            if (type == ElarionMountType.CHINESE_DRAGON) {
                continue;
            }
            assertEquals(1.0F, type.renderScale(), type.id());
            assertEquals(0.0D, type.passengerYOffset(), type.id());
            assertTrue(type.riderVisualYOffset() != 0.0D
                    || type.riderSeatProfile().visualXOffset() != 0.0D
                    || type.riderSeatProfile().visualZOffset() != 0.0D, type.id());
            assertEquals(0.0F, type.riderSeatProfile().visualYawOffset(), type.id());
        }
    }

    @Test
    void cameraSeatIsSeparatedFromVisibleRiderCalibration() {
        assertTrue(ElarionMountType.SCIFI_BIKE.riderSeatProfile().serverZOffset() > 0.0D);
        assertTrue(ElarionMountType.SCIFI_BIKE.riderSeatProfile().visualZOffset() > 0.0D);
        assertTrue(ElarionMountType.HOT_AIR_BALLOON.riderSeatProfile().serverZOffset() < 0.0D);
        assertTrue(ElarionMountType.HOT_AIR_BALLOON.riderSeatProfile().visualZOffset() > 0.0D);
    }

    @Test
    void verticalAnimationMappingMatchesControls() {
        assertEquals("ascend", ElarionMountAnimationLogic.verticalOverlayForInputs(false, true));
        assertEquals("descend", ElarionMountAnimationLogic.verticalOverlayForInputs(true, false));
        assertEquals("ascend", ElarionMountAnimationLogic.verticalOverlayForInputs(true, true));
        assertEquals("none", ElarionMountAnimationLogic.verticalOverlayForInputs(false, false));
    }

    @Test
    void movementProfilesDifferentiateMountRoles() {
        assertTrue(ElarionMountType.BEE.movementProfile().turnDegrees()
                > ElarionMountType.AIRSHIP.movementProfile().turnDegrees());
        assertTrue(ElarionMountType.SCIFI_BIKE.movementProfile().maxForwardSpeed()
                > ElarionMountType.HOT_AIR_BALLOON.movementProfile().maxForwardSpeed());
        assertTrue(ElarionMountType.GHAST.movementProfile().verticalDrag()
                > ElarionMountType.WYVERN.movementProfile().verticalDrag());
    }

    @Test
    void boostAndCameraProfilesAreExplicitForEveryMount() {
        for (ElarionMountType type : ElarionMountType.values()) {
            assertTrue(type.movementProfile().boostMultiplier() > 1.0D, type.id());
            assertTrue(type.movementProfile().boostedForwardSpeed()
                    > type.movementProfile().maxForwardSpeed(), type.id());
            assertTrue(type.cameraProfile().thirdPersonDistance() >= 5.0F, type.id());
            assertTrue(type.cameraProfile().boostDistanceBonus() > 0.0F, type.id());
            assertTrue(type.cameraProfile().boostFovBonus() > 0.0F, type.id());
        }
    }

    @Test
    void allMountsKeepThreeBlockTerrainClearance() {
        for (ElarionMountType type : ElarionMountType.values()) {
            assertEquals(3.0D, type.movementProfile().minClearanceBlocks(), type.id());
        }
    }

}
