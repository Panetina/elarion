package panetina.elarion.addons.underworld.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnderworldDynamicLightsInitializerTest {
    @Test
    void localDeadOrBanishedPlayerEmitsBoundedLight() {
        assertEquals(6, UnderworldSpectralLight.luminance(
                true, true, UnderworldSoulSight.PlayerAppearance.NORMAL, false));
        assertEquals(6, UnderworldSpectralLight.luminance(
                true, false, UnderworldSoulSight.PlayerAppearance.NORMAL, true));
        assertEquals(0, UnderworldSpectralLight.luminance(
                true, false, UnderworldSoulSight.PlayerAppearance.NORMAL, false));
    }

    @Test
    void visibleSpectralPlayersEmitAndOrdinaryPlayersDoNot() {
        assertEquals(6, UnderworldSpectralLight.luminance(
                false, true, UnderworldSoulSight.PlayerAppearance.SHADOW, false));
        assertEquals(6, UnderworldSpectralLight.luminance(
                false, false, UnderworldSoulSight.PlayerAppearance.BANISHED, true));
        assertEquals(0, UnderworldSpectralLight.luminance(
                false, true, UnderworldSoulSight.PlayerAppearance.NORMAL, false));
    }
}
