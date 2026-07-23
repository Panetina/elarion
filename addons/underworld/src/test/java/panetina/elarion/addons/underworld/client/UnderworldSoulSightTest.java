package panetina.elarion.addons.underworld.client;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UnderworldSoulSightTest {
    @Test
    void effectRequiresBothDeathStateAndUnderworldDimension() {
        assertTrue(UnderworldSoulSight.isActive(true, "elarion:underworld"));
        assertFalse(UnderworldSoulSight.isActive(false, "elarion:underworld"));
        assertFalse(UnderworldSoulSight.isActive(true, "minecraft:overworld"));
    }

    @Test
    void onlyOtherPlayersBecomeShadows() {
        UUID viewer = UUID.randomUUID();
        assertFalse(UnderworldSoulSight.isOtherPlayer(viewer, viewer));
        assertTrue(UnderworldSoulSight.isOtherPlayer(viewer, UUID.randomUUID()));
    }

    @Test
    void banishmentRedAppearanceOverridesDeadViewerShadow() {
        assertTrue(UnderworldSoulSight.appearance(true, true, true)
                == UnderworldSoulSight.PlayerAppearance.BANISHED);
        assertTrue(UnderworldSoulSight.appearance(true, false, true)
                == UnderworldSoulSight.PlayerAppearance.SHADOW);
        assertTrue(UnderworldSoulSight.appearance(false, false, true)
                == UnderworldSoulSight.PlayerAppearance.NORMAL);
        assertTrue(UnderworldSoulSight.appearance(true, true, false)
                == UnderworldSoulSight.PlayerAppearance.NORMAL);
    }
}
