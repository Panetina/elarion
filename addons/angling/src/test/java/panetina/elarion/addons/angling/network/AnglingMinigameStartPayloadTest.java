package panetina.elarion.addons.angling.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.minigame.AnglingNativeModifier;
import panetina.elarion.addons.angling.minigame.AnglingServerMinigameSpec;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingMinigameStartPayloadTest {
    @Test
    void startConfigurationRoundTripsWithoutAnyOutcomeFields() {
        var spec = new AnglingServerMinigameSpec(
                100, 12, 5, 1, 0.5F, 20, true,
                List.of(new AnglingNativeModifier.DisableHitSounds(""),
                        new AnglingNativeModifier.PullDown("")),
                List.of());
        AnglingMinigameStartPayload source = AnglingMinigameStartPayload.create(
                UUID.randomUUID(), 9, 55L, Identifier.of("elarion_angling", "aurorafin"), spec);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        AnglingMinigameStartPayload.CODEC.encode(buffer, source);
        assertEquals(source, AnglingMinigameStartPayload.CODEC.decode(buffer));
        assertTrue(source.disableHitSounds());
        assertTrue(source.pullDownPresentation());
    }
}
