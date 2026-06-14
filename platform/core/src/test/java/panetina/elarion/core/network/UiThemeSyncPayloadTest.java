package panetina.elarion.core.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ElarionUiTheme;
import panetina.elarion.core.model.ElarionUiThemeVariant;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class UiThemeSyncPayloadTest {
    @Test
    void roundTripsThemeSnapshot() {
        ElarionUiThemeVariant shrine = new ElarionUiThemeVariant(
                "shrine", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
                15, 16, 17, 18, 19, 20, 21, 22, "elarion:textures/gui/panel.png",
                "elarion:textures/gui/card.png", "tiled", 23);
        UiThemeSyncPayload payload = new UiThemeSyncPayload(
                new ElarionUiTheme(640, 420, 55, 18, 7, 20, 16, 5, Map.of("shrine", shrine)));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        UiThemeSyncPayload.CODEC.encode(buffer, payload);
        UiThemeSyncPayload decoded = UiThemeSyncPayload.CODEC.decode(buffer);

        assertEquals(payload, decoded);
        assertEquals(shrine, decoded.theme().variant("shrine"));
    }
}
