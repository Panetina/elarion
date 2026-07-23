package panetina.elarion.addons.angling.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.minigame.AnglingServerMinigameSnapshot;
import panetina.elarion.addons.angling.minigame.AnglingServerMinigameSpec;
import panetina.elarion.addons.angling.minigame.AnglingServerMinigameStatus;
import panetina.elarion.addons.angling.minigame.AnglingSweetspotBehaviorType;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class AnglingMinigameStatePayloadTest {
    @Test
    void boundedAuthoritativeSnapshotRoundTrips() {
        AnglingMinigameStatePayload source = new AnglingMinigameStatePayload(
                new AnglingServerMinigameSnapshot(
                        UUID.randomUUID(), 7, 20, AnglingServerMinigameStatus.ACTIVE,
                        35.5F, 12.0F, -1, 1, 2, 45.0F, 43.5F,
                        30, false, 0, 4, 80,
                        List.of(new AnglingServerMinigameSnapshot.Sweetspot(
                                0, AnglingSweetspotBehaviorType.AQUA,
                                Identifier.of("elarion_angling", "textures/gui/minigame/spots/aqua.png"), 90.0F,
                                1, 24, 0.5F, 0xff387982))));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        AnglingMinigameStatePayload.CODEC.encode(buffer, source);
        assertEquals(source, AnglingMinigameStatePayload.CODEC.decode(buffer));
    }

    @Test
    void decoderRejectsUnboundedSweetspotCountBeforeAllocation() {
        PacketByteBuf buffer = headerThroughDarkness();
        buffer.writeVarInt(AnglingServerMinigameSpec.MAX_RUNTIME_SWEETSPOTS + 1);
        assertThrows(IllegalArgumentException.class, () -> AnglingMinigameStatePayload.CODEC.decode(buffer));
    }

    private static PacketByteBuf headerThroughDarkness() {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeUuid(UUID.randomUUID());
        buffer.writeVarLong(0);
        buffer.writeVarLong(0);
        buffer.writeByte(AnglingServerMinigameStatus.ACTIVE.ordinal());
        buffer.writeFloat(0);
        buffer.writeFloat(0);
        buffer.writeByte(1);
        buffer.writeVarInt(0);
        buffer.writeVarInt(0);
        buffer.writeFloat(20);
        buffer.writeFloat(20);
        buffer.writeVarInt(0);
        buffer.writeBoolean(true);
        buffer.writeVarInt(0);
        buffer.writeVarInt(0);
        buffer.writeByte(0);
        return buffer;
    }
}
