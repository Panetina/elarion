package panetina.elarion.addons.offerings.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ShrineContributionSubmitPayloadTest {
    @Test
    void roundTripsContributionRequest() {
        ShrineContributionSubmitPayload payload = new ShrineContributionSubmitPayload(
                "offering_realm_oak_1", "item:minecraft:stone_bricks", "64");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        ShrineContributionSubmitPayload.CODEC.encode(buffer, payload);

        assertEquals(payload, ShrineContributionSubmitPayload.CODEC.decode(buffer));
    }
}
