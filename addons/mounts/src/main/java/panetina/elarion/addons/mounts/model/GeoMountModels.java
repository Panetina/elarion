package panetina.elarion.addons.mounts.model;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.mounts.entity.ElarionMountType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

public final class GeoMountModels {
    private static final Map<ElarionMountType, GeoModelDefinition> MODELS =
            new EnumMap<>(ElarionMountType.class);

    private GeoMountModels() {
    }

    public static synchronized GeoModelDefinition chineseDragon() {
        return forType(ElarionMountType.CHINESE_DRAGON);
    }

    public static synchronized GeoModelDefinition forType(ElarionMountType type) {
        GeoModelDefinition cached = MODELS.get(type);
        if (cached != null) {
            return cached;
        }
        try (InputStream geo = GeoMountModels.class.getResourceAsStream("/assets/elarion_mounts/geo/" + type.geoFileName());
             InputStream animation = GeoMountModels.class.getResourceAsStream("/assets/elarion_mounts/animations/" + type.animationFileName())) {
            if (geo == null) {
                return null;
            }
            try (InputStreamReader geoReader = new InputStreamReader(geo, StandardCharsets.UTF_8);
                 InputStreamReader animationReader = animation == null
                         ? null
                         : new InputStreamReader(animation, StandardCharsets.UTF_8)) {
                GeoModelDefinition model = GeoModelDefinition.parse(
                        geoReader,
                        animationReader,
                        Identifier.of("elarion_mounts", "textures/entity/" + type.textureFileName()));
                MODELS.put(type, model);
                return model;
            }
        } catch (Exception exception) {
            return null;
        }
    }
}
