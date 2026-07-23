package panetina.elarion.addons.angling.component;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;

/** Fabric registrations for versioned Angling item data. */
public final class AnglingDataComponents {
    public static final ComponentType<AnglingSingleStackComponent> BUCKETED_FISH = registerStack("bucketed_fish");
    public static final ComponentType<AnglingSingleStackComponent> BOBBER = registerStack("bobber");
    public static final ComponentType<AnglingSingleStackComponent> BAIT = registerStack("bait");
    public static final ComponentType<AnglingSingleStackComponent> HOOK = registerStack("hook");
    public static final ComponentType<AnglingSecretNoteComponent> SECRET_NOTE = register(
            "secret_note", AnglingSecretNoteComponent.CODEC);
    public static final ComponentType<AnglingLetterMessageComponent> MESSAGE = register(
            "message", AnglingLetterMessageComponent.CODEC);
    public static final ComponentType<AnglingCaughtFishComponent> CAUGHT_FISH_INFO = Registry.register(
            Registries.DATA_COMPONENT_TYPE, id("caught_fish_info"),
            ComponentType.<AnglingCaughtFishComponent>builder()
                    .codec(AnglingCaughtFishComponent.CODEC)
                    .packetCodec(AnglingCaughtFishComponent.PACKET_CODEC)
                    .build()
    );
    public static final ComponentType<Boolean> NETHERITE_UPGRADE = register(
            "netherite_upgraded", Codec.BOOL);
    public static final ComponentType<AnglingTackleBoxComponent> TACKLE_BOX_FISHES = register(
            "tackle_box_fishes", AnglingTackleBoxComponent.CODEC);
    public static final ComponentType<AnglingSignedGuideComponent> SIGNED_GUIDE = register(
            "signed_guide", AnglingSignedGuideComponent.CODEC);
    /** Stable skin identity; behavior and rendering are resolved from the reload-safe skin registry. */
    public static final ComponentType<Identifier> TACKLE_SKIN = register(
            "tackle_skin", Identifier.CODEC);
    public static final ComponentType<AnglingModifierComponent> MODIFIERS = register(
            "modifiers", AnglingModifierComponent.CODEC);

    private AnglingDataComponents() {
    }

    public static void initialize() {
        // Class initialization performs the bounded registry bootstrap.
    }

    private static <T> ComponentType<T> register(String path, Codec<T> codec) {
        return Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                id(path),
                ComponentType.<T>builder().codec(codec).build()
        );
    }

    private static ComponentType<AnglingSingleStackComponent> registerStack(String path) {
        return Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                id(path),
                ComponentType.<AnglingSingleStackComponent>builder()
                        .codec(AnglingSingleStackComponent.CODEC)
                        .packetCodec(AnglingSingleStackComponent.PACKET_CODEC)
                        .build());
    }

    private static Identifier id(String path) {
        return Identifier.of(ElarionAnglingAddon.MOD_ID, path);
    }
}
