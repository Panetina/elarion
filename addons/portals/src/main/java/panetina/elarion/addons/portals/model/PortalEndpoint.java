package panetina.elarion.addons.portals.model;

public record PortalEndpoint(String worldId, PortalBounds bounds) {
    public PortalEndpoint {
        worldId = worldId == null ? "" : worldId;
    }
}
