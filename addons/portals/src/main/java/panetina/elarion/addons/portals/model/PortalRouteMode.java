package panetina.elarion.addons.portals.model;

import java.util.Locale;

public enum PortalRouteMode {
    SCHEDULED_TICKETED("scheduled_ticketed", true, true, true, false),
    FEE_PASSAGE("fee_passage", false, true, false, true),
    ALWAYS_OPEN("always_open", false, false, false, false);

    private final String configId;
    private final boolean usesSchedule;
    private final boolean requiresUnlock;
    private final boolean requiresTicket;
    private final boolean chargesPassage;

    PortalRouteMode(
            String configId,
            boolean usesSchedule,
            boolean requiresUnlock,
            boolean requiresTicket,
            boolean chargesPassage
    ) {
        this.configId = configId;
        this.usesSchedule = usesSchedule;
        this.requiresUnlock = requiresUnlock;
        this.requiresTicket = requiresTicket;
        this.chargesPassage = chargesPassage;
    }

    public String configId() {
        return configId;
    }

    public boolean usesSchedule() {
        return usesSchedule;
    }

    public boolean requiresUnlock() {
        return requiresUnlock;
    }

    public boolean requiresTicket() {
        return requiresTicket;
    }

    public boolean chargesPassage() {
        return chargesPassage;
    }

    public static PortalRouteMode parse(String raw) {
        String normalized = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        for (PortalRouteMode mode : values()) {
            if (mode.configId.equals(normalized)) return mode;
        }
        throw new IllegalArgumentException(
                "Portal mode must be scheduled_ticketed, fee_passage, or always_open.");
    }
}
