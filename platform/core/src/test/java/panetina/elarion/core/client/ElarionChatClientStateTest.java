package panetina.elarion.core.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ElarionChatChannel;
import panetina.elarion.core.network.ChatRecipientSnapshotPayload;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class ElarionChatClientStateTest {
    @AfterEach void reset() {
        ElarionChatChannelClientState.reset();
        ElarionChatRecipientClientState.clear();
    }

    @Test void cyclesOnlyServerProjectedChannels() {
        ElarionChatChannelClientState.updateAvailable(List.of(
                ElarionChatChannel.LOCAL, ElarionChatChannel.GUILD, ElarionChatChannel.PRIVATE));

        ElarionChatChannelClientState.cycle(1);
        assertEquals(ElarionChatChannel.GUILD, ElarionChatChannelClientState.selected());
        ElarionChatChannelClientState.cycle(-1);
        assertEquals(ElarionChatChannel.LOCAL, ElarionChatChannelClientState.selected());

        ElarionChatChannelClientState.select(ElarionChatChannel.ALLIANCE);
        assertEquals(ElarionChatChannel.LOCAL, ElarionChatChannelClientState.selected());
    }

    @Test void acceptsOnlyProjectedPrivateMessageTargets() {
        UUID eligible = UUID.randomUUID();
        ElarionChatRecipientClientState.update(new ChatRecipientSnapshotPayload(
                List.of(new ChatRecipientSnapshotPayload.Entry(eligible, "Aster"))));

        ElarionChatRecipientClientState.select(UUID.randomUUID());
        assertNull(ElarionChatRecipientClientState.selected());
        ElarionChatRecipientClientState.select(eligible);
        assertEquals(eligible, ElarionChatRecipientClientState.selected());
    }
}
