package panetina.elarion.addons.mounts.client;

import panetina.elarion.addons.mounts.entity.ElarionMountType;
import panetina.elarion.addons.mounts.model.GeoModelDefinition;
import panetina.elarion.addons.mounts.model.GeoMountModels;

final class GeoModelCache {
    private GeoModelCache() {
    }

    static GeoModelDefinition chineseDragon() {
        return GeoMountModels.chineseDragon();
    }

    static GeoModelDefinition forType(ElarionMountType type) {
        return GeoMountModels.forType(type);
    }
}
