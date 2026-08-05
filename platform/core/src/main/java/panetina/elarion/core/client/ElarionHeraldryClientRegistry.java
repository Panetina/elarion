package panetina.elarion.core.client;

import panetina.elarion.core.model.ElarionPixelAsset32;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Core client registry for server-authoritative heraldry presentation snapshots. */
public final class ElarionHeraldryClientRegistry {
    private static final Map<String, ElarionPixelAsset32> REALM_ASSETS = new ConcurrentHashMap<>();

    private ElarionHeraldryClientRegistry() { }

    public static void putRealm(String realmId, long revision, byte[] pixels) {
        if (realmId == null || realmId.isBlank()) return;
        REALM_ASSETS.compute(realmId, (ignored, current) -> current != null && current.revision() > revision
                ? current : new ElarionPixelAsset32(revision, pixels));
    }

    public static ElarionPixelAsset32 realm(String realmId) {
        return REALM_ASSETS.getOrDefault(realmId, ElarionPixelAsset32.blank());
    }

    public static void clear() { REALM_ASSETS.clear(); }
}
