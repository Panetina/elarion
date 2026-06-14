package panetina.elarion.addons.portals.model;

public record PortalArrival(String worldId, double x, double y, double z, float yaw, float pitch) {
    public PortalArrival {
        worldId = worldId == null ? "" : worldId;
    }
}
