package panetina.elarion.addons.portals.service;

import panetina.elarion.addons.portals.model.PortalFreePassageState;
import panetina.elarion.addons.portals.model.PortalReturnEntitlement;
import panetina.elarion.addons.portals.model.PortalRouteState;
import panetina.elarion.addons.portals.storage.PortalState;

import java.util.LinkedHashMap;
import java.util.Map;

final class PortalStateMigration {
    private PortalStateMigration() {
    }

    static void migrateLegacyRouteIds(PortalState state) {
        migrateLegacyRouteId(state, "ancient_oak", "realm1");
        migrateLegacyRouteId(state, "ancient_sky", "realm2");
        migrateLegacyRouteId(state, "ancient_earth", "realm3");
    }

    private static void migrateLegacyRouteId(PortalState state, String legacyId, String routeId) {
        PortalRouteState legacy = state.routes.remove(legacyId);
        if (legacy != null && !state.routes.containsKey(routeId)) {
            legacy.routeId = routeId;
            state.routes.put(routeId, legacy);
        }
        Map<String, PortalReturnEntitlement> migratedEntitlements = new LinkedHashMap<>();
        state.entitlements.entrySet().removeIf(entry -> {
            PortalReturnEntitlement entitlement = entry.getValue();
            if (!legacyId.equals(entitlement.routeId())) return false;
            PortalReturnEntitlement migrated = new PortalReturnEntitlement(
                    entitlement.playerId(), routeId, entitlement.createdAt(), entitlement.sourceWindowStart());
            migratedEntitlements.put(entitlementKey(entitlement.playerId().toString(), routeId), migrated);
            return true;
        });
        state.entitlements.putAll(migratedEntitlements);

        Map<String, PortalFreePassageState> migratedPassages = new LinkedHashMap<>();
        state.freePassages.entrySet().removeIf(entry -> {
            if (!entry.getKey().endsWith("|" + legacyId)) return false;
            String migratedKey = entry.getKey().substring(
                    0, entry.getKey().length() - legacyId.length()) + routeId;
            migratedPassages.put(migratedKey, entry.getValue());
            return true;
        });
        state.freePassages.putAll(migratedPassages);
    }

    private static String entitlementKey(String playerId, String routeId) {
        return playerId + "|" + routeId;
    }
}
