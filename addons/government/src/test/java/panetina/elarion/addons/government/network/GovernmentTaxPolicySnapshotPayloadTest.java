package panetina.elarion.addons.government.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class GovernmentTaxPolicySnapshotPayloadTest {
    @Test void roundTripsTypedRatesAndRevision() {
        GovernmentTaxPolicySnapshotPayload payload = new GovernmentTaxPolicySnapshotPayload(
                "elarion", 7L, "Realm Treasury", List.of(
                new GovernmentTaxPolicySnapshotPayload.Entry("trade", "Trade", 250),
                new GovernmentTaxPolicySnapshotPayload.Entry("income", "Income", 500)));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        GovernmentTaxPolicySnapshotPayload.CODEC.encode(buffer, payload);
        GovernmentTaxPolicySnapshotPayload decoded = GovernmentTaxPolicySnapshotPayload.CODEC.decode(buffer);

        assertEquals(payload, decoded);
        assertEquals(500, decoded.basisPoints("income"));
    }

    @Test void rejectsOutOfPolicyRate() {
        assertThrows(IllegalArgumentException.class,
                () -> new GovernmentTaxPolicySnapshotPayload.Entry("trade", "Trade", 2501));
    }
}
