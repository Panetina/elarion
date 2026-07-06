package panetina.elarion.addons.mounts.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MountInputPayloadTest {
    @Test
    void clampsNonFiniteAndExtremeMovementValues() {
        MountInputPayload payload = new MountInputPayload(
                12, Float.NaN, 9.0F, Float.POSITIVE_INFINITY, -8.0F,
                true, false, true, false);

        assertEquals(0.0F, payload.forward());
        assertEquals(1.0F, payload.sideways());
        assertEquals(0.0F, payload.lookYaw());
        assertEquals(-1.0F, payload.turnIntent());
    }
}
