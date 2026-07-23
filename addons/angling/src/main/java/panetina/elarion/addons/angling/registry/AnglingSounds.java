package panetina.elarion.addons.angling.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;

import java.util.List;

/** Eleven one-to-one sound-event registrations backed by the authorized OGG files. */
public final class AnglingSounds {
    public static final SoundEvent KING_CRY = register("king_cry");
    public static final SoundEvent KING_HEHEHA = register("king_heheha");
    public static final SoundEvent KING_GRR = register("king_grr");
    public static final SoundEvent VALLEY_BITING = register("valley_biting");
    public static final SoundEvent VALLEY_CAST = register("valley_cast");
    public static final SoundEvent VALLEY_MISSED = register("valley_missed");
    public static final SoundEvent VALLEY_MINIGAME_STARTS = register("valley_minigame_starts");
    public static final SoundEvent VALLEY_BOOP = register("valley_boop");
    public static final SoundEvent VALLEY_REEL = register("valley_reel");
    public static final SoundEvent SURVIVOR_BITING = register("survivor_biting");
    public static final SoundEvent SURVIVOR_MINIGAME_STARTS = register("survivor_minigame_starts");

    public static final List<SoundEvent> ALL = List.of(
            KING_CRY, KING_HEHEHA, KING_GRR,
            VALLEY_BITING, VALLEY_CAST, VALLEY_MISSED, VALLEY_MINIGAME_STARTS, VALLEY_BOOP, VALLEY_REEL,
            SURVIVOR_BITING, SURVIVOR_MINIGAME_STARTS
    );

    private AnglingSounds() {
    }

    public static void initialize() {
        // Class initialization performs registry bootstrap.
    }

    private static SoundEvent register(String path) {
        Identifier id = Identifier.of(ElarionAnglingAddon.MOD_ID, path);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
