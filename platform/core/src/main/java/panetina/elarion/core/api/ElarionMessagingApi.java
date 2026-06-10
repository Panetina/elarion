package panetina.elarion.core.api;

import panetina.elarion.core.service.ChatService;
import panetina.elarion.core.service.PrivateMessageService;

public final class ElarionMessagingApi {
    private final ChatService chat;
    private final PrivateMessageService privateMessages;

    ElarionMessagingApi(ChatService chat, PrivateMessageService privateMessages) {
        this.chat = chat;
        this.privateMessages = privateMessages;
    }

    public ChatService chat() {
        return chat;
    }

    public PrivateMessageService privateMessages() {
        return privateMessages;
    }
}
