package panetina.elarion.addons.portals.service;

import panetina.elarion.addons.portals.model.PortalFreePassageState;
import panetina.elarion.addons.portals.model.PortalTravelDirection;

final class PortalFreePassagePolicy {
    private PortalFreePassagePolicy() {
    }

    static boolean isFree(
            boolean enabled,
            PortalFreePassageState state,
            PortalTravelDirection direction
    ) {
        if (!enabled) return false;
        return direction == PortalTravelDirection.OUTBOUND
                ? state == null
                : state == PortalFreePassageState.RETURN_AVAILABLE;
    }

    static PortalFreePassageState afterSuccessfulTravel(
            PortalFreePassageState state,
            PortalTravelDirection direction
    ) {
        if (direction == PortalTravelDirection.OUTBOUND && state == null) {
            return PortalFreePassageState.RETURN_AVAILABLE;
        }
        if (direction == PortalTravelDirection.RETURN
                && state == PortalFreePassageState.RETURN_AVAILABLE) {
            return PortalFreePassageState.COMPLETED;
        }
        return state;
    }
}
