package panetina.elarion.addons.portals.client;

final class PortalWindowProgress {
    private PortalWindowProgress() {
    }

    static float remaining(long now, long startsAt, long endsAt, boolean active) {
        if (!active || endsAt <= startsAt) return 0.0F;
        long duration = endsAt - startsAt;
        long remaining = Math.max(0L, Math.min(duration, endsAt - now));
        return remaining / (float) duration;
    }
}
