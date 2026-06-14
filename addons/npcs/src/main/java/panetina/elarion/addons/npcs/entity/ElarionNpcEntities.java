package panetina.elarion.addons.npcs.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ElarionNpcEntities {
    public static final Identifier NPC_ID = Identifier.of("elarion_npcs", "npc");
    public static final EntityType<ElarionNpcEntity> NPC = Registry.register(
            Registries.ENTITY_TYPE,
            NPC_ID,
            FabricEntityType.Builder.createMob(
                            ElarionNpcEntity::new,
                            SpawnGroup.MISC,
                            builder -> builder.defaultAttributes(MobEntity::createMobAttributes))
                    .dimensions(0.6F, 1.8F)
                    .maxTrackingRange(10)
                    .trackingTickInterval(3)
                    .build());

    private static boolean initialized;

    private ElarionNpcEntities() {
    }

    public static void initialize() {
        if (initialized) return;
        initialized = true;
    }
}
