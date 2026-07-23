package panetina.elarion.addons.underworld.client;

/** Pure spectral-light policy, separated from the optional Lamb API boundary. */
final class UnderworldSpectralLight {
    static final int LUMINANCE = 6;

    private UnderworldSpectralLight() {
    }

    static int luminance(boolean localPlayer, boolean soulSightActive,
                         UnderworldSoulSight.PlayerAppearance appearance, boolean banished) {
        if (localPlayer) return soulSightActive || banished ? LUMINANCE : 0;
        return appearance != UnderworldSoulSight.PlayerAppearance.NORMAL ? LUMINANCE : 0;
    }
}
