package panetina.elarion.addons.government.model;

public record GovernmentSettings(
        int authorityInactivityDays,
        int authorityInactivityCheckIntervalSeconds
) {
    public GovernmentSettings {
        authorityInactivityDays = Math.max(1, authorityInactivityDays);
        authorityInactivityCheckIntervalSeconds = Math.max(60, authorityInactivityCheckIntervalSeconds);
    }

    public static GovernmentSettings defaults() {
        return new GovernmentSettings(7, 600);
    }

    public long authorityInactivityMillis() {
        return java.time.Duration.ofDays(authorityInactivityDays).toMillis();
    }

    public int authorityInactivityCheckIntervalTicks() {
        return authorityInactivityCheckIntervalSeconds * 20;
    }
}
