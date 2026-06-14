package panetina.elarion.addons.government.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GovernmentUiOpenPayloadTest {
    @Test
    void codecRoundTripsGovernmentUiSnapshot() {
        GovernmentUiOpenPayload payload = new GovernmentUiOpenPayload(
                "civic_forum",
                "Civic Forum",
                "Founding path for Realm 1",
                "realm1",
                "Realm 1",
                "default",
                840,
                560,
                70,
                List.of(new GovernmentUiOpenPayload.Row(
                        "name_vote", "Realm Name", "Choose a name.", "Unlocked", true, false)),
                List.of(new GovernmentUiOpenPayload.Row(
                        "republic", "Republic", "Elected leadership.", "Available", true, false)),
                List.of(new GovernmentUiOpenPayload.Row(
                        "president", "President", "Matie", "1 holder(s)", true, true)),
                List.of(new GovernmentUiOpenPayload.Row(
                        "laws", "Laws", "Future law module.", "Future", false, false)));

        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        GovernmentUiOpenPayload.CODEC.encode(buffer, payload);

        assertEquals(payload, GovernmentUiOpenPayload.CODEC.decode(buffer));
    }
}
