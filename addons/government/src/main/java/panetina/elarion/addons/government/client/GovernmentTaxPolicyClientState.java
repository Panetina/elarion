package panetina.elarion.addons.government.client;

import panetina.elarion.addons.government.network.GovernmentTaxPolicySnapshotPayload;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Small revisioned client projection; Economy remains the canonical owner. */
public final class GovernmentTaxPolicyClientState {
    private static final Map<String, GovernmentTaxPolicySnapshotPayload> BY_REALM = new ConcurrentHashMap<>();

    private GovernmentTaxPolicyClientState() { }

    public static void put(GovernmentTaxPolicySnapshotPayload payload) {
        if (payload == null || payload.realmId().isBlank()) return;
        BY_REALM.compute(payload.realmId(), (ignored, current) ->
                current == null || payload.revision() >= current.revision() ? payload : current);
    }

    public static GovernmentTaxPolicySnapshotPayload get(String realmId) {
        return BY_REALM.getOrDefault(realmId,
                new GovernmentTaxPolicySnapshotPayload(realmId, 0L, "Realm treasury", java.util.List.of()));
    }

    public static void clear() { BY_REALM.clear(); }
}
