package panetina.elarion.addons.offerings.model;

import java.util.UUID;

public record OfferingAnchor(
        String id,
        String instanceId,
        String worldId,
        int x,
        int y,
        int z,
        UUID createdBy,
        long createdAt
) {
    public OfferingAnchor {
        id = id == null ? "" : id;
        instanceId = instanceId == null ? "" : instanceId;
        worldId = worldId == null ? "" : worldId;
        createdAt = createdAt <= 0 ? System.currentTimeMillis() : createdAt;
    }
}
