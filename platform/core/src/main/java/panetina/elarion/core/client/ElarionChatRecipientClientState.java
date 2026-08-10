package panetina.elarion.core.client;

import panetina.elarion.core.network.ChatRecipientSnapshotPayload;
import java.util.List;
import java.util.UUID;

public final class ElarionChatRecipientClientState {
    private static List<ChatRecipientSnapshotPayload.Entry> recipients = List.of();
    private static UUID selected;
    private ElarionChatRecipientClientState() { }
    public static void update(ChatRecipientSnapshotPayload payload) { recipients = payload == null ? List.of() : payload.recipients(); if (selected != null && recipients.stream().noneMatch(entry -> selected.equals(entry.id()))) selected = null; }
    public static List<ChatRecipientSnapshotPayload.Entry> recipients() { return recipients; }
    public static void select(UUID id) {
        selected = id != null && recipients.stream().anyMatch(entry -> id.equals(entry.id())) ? id : null;
    }
    public static UUID selected() { return selected; }
    public static void clear() { recipients = List.of(); selected = null; }
}
