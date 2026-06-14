package panetina.elarion.addons.npcs.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ElarionUiSound {
    private ElarionUiSound() {
    }

    public static void play(String soundId) {
        if (soundId == null || soundId.isBlank()) return;
        try {
            MinecraftClient.getInstance().getSoundManager().play(
                    PositionedSoundInstance.master(SoundEvent.of(Identifier.of(soundId)), 1.0F));
        } catch (RuntimeException ignored) {
            // Optional UI sound metadata must never break interaction.
        }
    }
}
