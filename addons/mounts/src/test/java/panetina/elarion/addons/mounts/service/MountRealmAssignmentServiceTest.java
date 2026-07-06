package panetina.elarion.addons.mounts.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.mounts.entity.ElarionMountType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MountRealmAssignmentServiceTest {
    @Test
    void fixedRealmAssignmentsMatchPlan() {
        MountRealmAssignmentService service = new MountRealmAssignmentService();

        assertEquals(ElarionMountType.AIRSHIP, service.mountForRealm("realm1").orElseThrow());
        assertEquals(ElarionMountType.HOT_AIR_BALLOON, service.mountForRealm("realm2").orElseThrow());
        assertEquals(ElarionMountType.GHAST, service.mountForRealm("realm3").orElseThrow());
        assertEquals("realm1", service.realmForMount(ElarionMountType.AIRSHIP).orElseThrow());
    }

    @Test
    void realmVendorMountsUseEqualMovementStatsForFairness() {
        assertEquals(ElarionMountType.AIRSHIP.movementProfile(), ElarionMountType.HOT_AIR_BALLOON.movementProfile());
        assertEquals(ElarionMountType.AIRSHIP.movementProfile(), ElarionMountType.GHAST.movementProfile());
        assertTrue(ElarionMountType.AIRSHIP.movementProfile().boostMultiplier() > 1.0D);
    }
}
