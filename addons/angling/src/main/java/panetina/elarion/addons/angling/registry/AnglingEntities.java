package panetina.elarion.addons.angling.registry;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import panetina.elarion.addons.angling.ElarionAnglingAddon;
import panetina.elarion.addons.angling.fishing.AnglingFishEntity;
import panetina.elarion.addons.angling.fishing.AnglingFishingBobberEntity;

public final class AnglingEntities {
    private static boolean initialized;
    public static final Identifier FISHING_BOBBER_ID = Identifier.of(
            ElarionAnglingAddon.MOD_ID, "fishing_bob");
    public static final EntityType<AnglingFishingBobberEntity> FISHING_BOBBER = Registry.register(
            Registries.ENTITY_TYPE,
            FISHING_BOBBER_ID,
            EntityType.Builder.create(AnglingFishingBobberEntity::new, SpawnGroup.MISC)
                    .dimensions(0.3F, 0.3F)
                    .disableSaving()
                    .disableSummon()
                    .maxTrackingRange(10)
                    .trackingTickInterval(1)
                    .build());
    public static final Identifier FISH_ID = Identifier.of(ElarionAnglingAddon.MOD_ID, "fish");
    public static final EntityType<AnglingFishEntity> FISH = Registry.register(
            Registries.ENTITY_TYPE,
            FISH_ID,
            EntityType.Builder.create(AnglingFishEntity::new, SpawnGroup.WATER_AMBIENT)
                    .dimensions(0.5F, 0.5F)
                    .maxTrackingRange(10)
                    .trackingTickInterval(3)
                    .build());

    private AnglingEntities() {
    }

    public static synchronized void initialize() {
        if (initialized) return;
        FabricDefaultAttributeRegistry.register(FISH, AnglingFishEntity.createAttributes());
        initialized = true;
    }
}
