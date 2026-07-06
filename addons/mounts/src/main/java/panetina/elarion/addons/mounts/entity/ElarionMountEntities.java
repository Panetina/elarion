package panetina.elarion.addons.mounts.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ElarionMountEntities {
    public static final Identifier MOUNT_ID = Identifier.of("elarion_mounts", "mount");
    public static final EntityType<ElarionMountEntity> MOUNT = Registry.register(
            Registries.ENTITY_TYPE,
            MOUNT_ID,
            FabricEntityType.Builder.createMob(
                            ElarionMountEntity::new,
                            SpawnGroup.MISC,
                            builder -> builder.defaultAttributes(ElarionMountEntity::createAttributes))
                    .dimensions(1.35F, 1.4F)
                    .maxTrackingRange(96)
                    .trackingTickInterval(1)
                    .build());

    private static boolean initialized;

    private ElarionMountEntities() {
    }

    public static void initialize() {
        if (initialized) return;
        initialized = true;
    }
}
