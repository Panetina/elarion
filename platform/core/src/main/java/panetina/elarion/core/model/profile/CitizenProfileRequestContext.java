package panetina.elarion.core.model.profile;

import java.util.Objects;
import java.util.UUID;

public record CitizenProfileRequestContext(
        UUID viewerId,
        UUID targetId,
        boolean administrator
) {
    public CitizenProfileRequestContext {
        targetId = Objects.requireNonNull(targetId, "targetId");
    }

    public static CitizenProfileRequestContext self(UUID targetId) {
        return new CitizenProfileRequestContext(targetId, targetId, false);
    }

    public static CitizenProfileRequestContext publicView(UUID viewerId, UUID targetId) {
        return new CitizenProfileRequestContext(viewerId, targetId, false);
    }

    public static CitizenProfileRequestContext admin(UUID viewerId, UUID targetId) {
        return new CitizenProfileRequestContext(viewerId, targetId, true);
    }

    public boolean self() {
        return viewerId != null && viewerId.equals(targetId);
    }
}
