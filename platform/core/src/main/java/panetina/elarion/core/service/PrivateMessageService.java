package panetina.elarion.core.service;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.RealmRelationship;
import panetina.elarion.core.model.ServerIdentityConfig;
import panetina.elarion.core.model.VisibilityScope;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PrivateMessageService {
    private final RealmService realms;
    private final CitizenService citizens;
    private final IdentityService identities;
    private final RealmGovernanceService governance;
    private final HistoryService history;
    private final ChatService chat;
    private final ServerIdentityConfig serverIdentity;
    private final Map<UUID, UUID> lastPrivateMessageSender = new ConcurrentHashMap<>();

    public PrivateMessageService(
            RealmService realms,
            CitizenService citizens,
            IdentityService identities,
            RealmGovernanceService governance,
            HistoryService history,
            ChatService chat,
            ServerIdentityConfig serverIdentity
    ) {
        this.realms = realms;
        this.citizens = citizens;
        this.identities = identities;
        this.governance = governance;
        this.history = history;
        this.chat = chat;
        this.serverIdentity = serverIdentity;
    }

    public boolean privateMessage(ServerPlayerEntity sender, ServerPlayerEntity recipient, String message) {
        if (message == null || message.isBlank()) return false;

        String senderRealmId = citizens.getOrCreate(sender).realmId();
        String recipientRealmId = citizens.getOrCreate(recipient).realmId();
        RealmDefinition recipientRealm = realms.forCitizen(citizens.getOrCreate(recipient)).orElse(null);
        boolean sameRealm = !senderRealmId.isBlank() && senderRealmId.equals(recipientRealmId);
        boolean recipientIsGlobal = recipientRealm != null && recipientRealm.visibilityScope() == VisibilityScope.GLOBAL;
        RealmRelationship relationship = governance.relationship(senderRealmId, recipientRealmId);
        boolean relationshipAllowsForeignMessage =
                relationship == RealmRelationship.ALLY || relationship == RealmRelationship.NEUTRAL;
        if (!sameRealm && (!recipientIsGlobal || !relationshipAllowsForeignMessage)) {
            sender.sendMessage(Text.literal(
                    "You may only message citizens in your " + serverIdentity.realmSingular()
                            + " or reachable members of a GLOBAL " + serverIdentity.realmSingular() + ".")
                    .formatted(Formatting.RED), false);
            return false;
        }

        Text senderName = identities.resolve(sender).chatName();
        Text recipientName = identities.resolve(recipient).chatName();
        sender.sendMessage(Text.literal("[You -> ")
                .append(recipientName)
                .append(Text.literal("] " + message).formatted(Formatting.GRAY)), false);
        recipient.sendMessage(Text.literal("[")
                .append(senderName)
                .append(Text.literal(" -> You] " + message).formatted(Formatting.GRAY)), false);
        chat.spyPrivateMessage(sender, recipient, message);
        lastPrivateMessageSender.put(recipient.getUuid(), sender.getUuid());
        history.record("chat", "private-message", sender.getUuid(), "player",
                recipient.getUuidAsString(), senderRealmId, Map.of(
                        "channel", "private-message",
                        "recipientRealm", recipientRealmId
                ));
        return true;
    }

    public boolean reply(ServerPlayerEntity sender, String message) {
        UUID recipientId = lastPrivateMessageSender.get(sender.getUuid());
        if (recipientId == null) {
            sender.sendMessage(Text.literal("Nobody has messaged you yet.").formatted(Formatting.RED), false);
            return false;
        }
        ServerPlayerEntity recipient = sender.getServer().getPlayerManager().getPlayer(recipientId);
        if (recipient == null) {
            sender.sendMessage(Text.literal("That player is no longer online.").formatted(Formatting.RED), false);
            return false;
        }
        return privateMessage(sender, recipient, message);
    }
}
