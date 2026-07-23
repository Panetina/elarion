package panetina.elarion.addons.atlas.api;

import java.util.Set;

/**
 * Stable marker for the Atlas addon shell. It deliberately exposes no map
 * data, mutation, storage, or integration service.
 */
public interface ElarionAtlasApi {
    static ElarionAtlasApi get() {
        return Holder.INSTANCE;
    }

    static Set<String> implementedCapabilities() {
        return Set.of();
    }

    final class Holder {
        private static final ElarionAtlasApi INSTANCE = new ElarionAtlasApi() {
        };

        private Holder() {
        }
    }
}
