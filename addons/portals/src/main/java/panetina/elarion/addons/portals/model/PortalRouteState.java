package panetina.elarion.addons.portals.model;

public final class PortalRouteState {
    public String routeId = "";
    public boolean unlocked;
    public PortalEndpoint source;
    public PortalEndpoint returnEndpoint;
    public PortalArrival outboundArrival;
    public PortalArrival returnArrival;
    public Long forcedOpenUntil;
    public Long forcedClosedUntil;
    public long lastOpenedWindow;
    public long lastClosedWindow;

    public PortalRouteState() {
    }

    public PortalRouteState(String routeId) {
        this.routeId = routeId;
    }

    public PortalRouteState copy() {
        PortalRouteState copy = new PortalRouteState(routeId);
        copy.unlocked = unlocked;
        copy.source = source;
        copy.returnEndpoint = returnEndpoint;
        copy.outboundArrival = outboundArrival;
        copy.returnArrival = returnArrival;
        copy.forcedOpenUntil = forcedOpenUntil;
        copy.forcedClosedUntil = forcedClosedUntil;
        copy.lastOpenedWindow = lastOpenedWindow;
        copy.lastClosedWindow = lastClosedWindow;
        return copy;
    }

    public boolean complete() {
        return source != null && returnEndpoint != null && outboundArrival != null && returnArrival != null;
    }
}
