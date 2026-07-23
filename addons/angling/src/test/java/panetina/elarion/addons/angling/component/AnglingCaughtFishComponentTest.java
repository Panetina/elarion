package panetina.elarion.addons.angling.component;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.definition.AnglingRarity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class AnglingCaughtFishComponentTest {
    @Test
    void persistentCodecRoundTripsServerComputedProperties() {
        AnglingCaughtFishComponent source = sample();
        JsonElement encoded = AnglingCaughtFishComponent.CODEC.encodeStart(JsonOps.INSTANCE, source).getOrThrow();
        AnglingCaughtFishComponent decoded = AnglingCaughtFishComponent.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(source, decoded);
    }

    @Test
    void boundedPacketCodecRoundTripsWithoutRawJson() {
        AnglingCaughtFishComponent source = sample();
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        AnglingCaughtFishComponent.PACKET_CODEC.encode(buffer, source);
        AnglingCaughtFishComponent decoded = AnglingCaughtFishComponent.PACKET_CODEC.decode(buffer);
        assertEquals(source, decoded);
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    void invalidPercentileCannotEnterItemState() {
        assertThrows(IllegalArgumentException.class, () -> new AnglingCaughtFishComponent(
                1, Identifier.of("elarion_angling", "aloe_bream"), 36, 2_000,
                10_001, AnglingRarity.RARE, false, false));
    }

    private static AnglingCaughtFishComponent sample() {
        return new AnglingCaughtFishComponent(
                AnglingCaughtFishComponent.CURRENT_SCHEMA_VERSION,
                Identifier.of("elarion_angling", "aloe_bream"),
                36,
                2_000,
                9_750,
                AnglingRarity.RARE,
                true,
                true
        );
    }
}
