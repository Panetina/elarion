package panetina.elarion.core.model;

import java.util.Optional;
import java.util.UUID;

public record WorldheartAuthority(
        WorldheartAuthorityType type,
        UUID rulerId,
        String systemDisplayName,
        long changedAt
) {
    public static final String DEFAULT_SYSTEM_DISPLAY_NAME = "Hollow Emperor";

    public WorldheartAuthority {
        if (type == null) throw new IllegalArgumentException("Worldheart authority type is required");
        systemDisplayName = systemDisplayName == null ? "" : systemDisplayName.trim();
        if (systemDisplayName.isBlank()) {
            throw new IllegalArgumentException("Worldheart system authority display name is required");
        }
        if (type == WorldheartAuthorityType.SYSTEM && rulerId != null) {
            throw new IllegalArgumentException("System-governed Worldheart cannot have a player ruler");
        }
        if (type == WorldheartAuthorityType.PLAYER && rulerId == null) {
            throw new IllegalArgumentException("Player-governed Worldheart requires a ruler UUID");
        }
        changedAt = Math.max(0L, changedAt);
    }

    public static WorldheartAuthority defaultSystem() {
        return system(DEFAULT_SYSTEM_DISPLAY_NAME, 0L);
    }

    public static WorldheartAuthority system(String displayName, long changedAt) {
        return new WorldheartAuthority(WorldheartAuthorityType.SYSTEM, null, displayName, changedAt);
    }

    public static WorldheartAuthority player(UUID rulerId, String systemDisplayName, long changedAt) {
        return new WorldheartAuthority(WorldheartAuthorityType.PLAYER, rulerId, systemDisplayName, changedAt);
    }

    public Optional<UUID> playerRulerId() {
        return Optional.ofNullable(rulerId);
    }
}
