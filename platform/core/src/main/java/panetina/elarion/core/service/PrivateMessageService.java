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
    private final PlayerRestrictionService restrictions;
    private final Map<UUID, UUID> lastPrivateMessageSender = new ConcurrentHashMap<>();

    public PrivateMessageService(
            RealmService realms,
            CitizenService citizens,
            IdentityService identities,
            RealmGovernanceService governance,
            HistoryService history,
            ChatService chat,
            ServerIdentityConfig serverIdentity,
            PlayerRestrictionService restrictions
    ) {
        this.realms = realms;
        this.citizens = citizens;
        this.identities = identities;
        this.governance = governance;
        this.history = history;
        this.chat = chat;
        this.serverIdentity = serverIdentity;
        this.restrictions = restrictions;
    }

    public boolean privateMessage(ServerPlayerEntity sender, ServerPlayerEntity recipient, String message) {
        if (message == null || message.isBlank()) return false;
        if (restrictions.denyWithMessage(sender, PlayerRestrictionService.PRIVATE_MESSAGE)) return false;
        if (restrictions.isRestricted(recipient, PlayerRestrictionService.PRIVATE_MESSAGE)) {
            sender.sendMessage(Text.literal("That player cannot receive private messages right now.")
                    .formatted(Formatting.RED), false);
            return false;
        }

        String senderRealmId = citizens.getOrCreate(sender).realmId();
        String recipientRealmId = citizens.getOrCreate(recipient).realmId();
        if (!canMessage(sender, recipient)) {
            sender.sendMessage(Text.literal(
                    "You may only message Embers in your " + serverIdentity.realmSingular()
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

    public boolean canMessage(ServerPlayerEntity sender, ServerPlayerEntity recipient) {
        if (sender == null || recipient == null || sender.getUuid().equals(recipient.getUuid())) return false;
        if (restrictions.isRestricted(sender, PlayerRestrictionService.PRIVATE_MESSAGE)
                || restrictions.isRestricted(recipient, PlayerRestrictionService.PRIVATE_MESSAGE)) return false;
        String senderRealmId = citizens.getOrCreate(sender).realmId();
        String recipientRealmId = citizens.getOrCreate(recipient).realmId();
        if (!senderRealmId.isBlank() && senderRealmId.equals(recipientRealmId)) return true;
        RealmDefinition recipientRealm = realms.forCitizen(citizens.getOrCreate(recipient)).orElse(null);
        RealmRelationship relationship = governance.relationship(senderRealmId, recipientRealmId);
        return recipientRealm != null && recipientRealm.visibilityScope() == VisibilityScope.GLOBAL
                && (relationship == RealmRelationship.ALLY || relationship == RealmRelationship.NEUTRAL);
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
