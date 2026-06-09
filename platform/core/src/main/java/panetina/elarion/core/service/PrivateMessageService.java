package panetina.elarion.core.service;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.VisibilityScope;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PrivateMessageService {
    private final RealmService realms;
    private final CitizenService citizens;
    private final IdentityService identities;
    private final Map<UUID, UUID> lastWhisperSender = new ConcurrentHashMap<>();

    public PrivateMessageService(
            RealmService realms,
            CitizenService citizens,
            IdentityService identities
    ) {
        this.realms = realms;
        this.citizens = citizens;
        this.identities = identities;
    }

    public boolean whisper(ServerPlayerEntity sender, ServerPlayerEntity recipient, String message) {
        RealmDefinition recipientRealm = realms.forCitizen(citizens.getOrCreate(recipient)).orElse(null);
        if (recipientRealm == null || recipientRealm.visibilityScope() != VisibilityScope.GLOBAL) {
            sender.sendMessage(Text.literal(
                    "You may only whisper to members of a GLOBAL Realm.").formatted(Formatting.RED), false);
            return false;
        }
        if (message == null || message.isBlank()) return false;

        Text senderName = identities.resolve(sender).chatName();
        Text recipientName = identities.resolve(recipient).chatName();
        sender.sendMessage(Text.literal("[You -> ")
                .append(recipientName)
                .append(Text.literal("] " + message).formatted(Formatting.GRAY)), false);
        recipient.sendMessage(Text.literal("[")
                .append(senderName)
                .append(Text.literal(" -> You] " + message).formatted(Formatting.GRAY)), false);
        lastWhisperSender.put(recipient.getUuid(), sender.getUuid());
        return true;
    }

    public boolean reply(ServerPlayerEntity sender, String message) {
        UUID recipientId = lastWhisperSender.get(sender.getUuid());
        if (recipientId == null) {
            sender.sendMessage(Text.literal("Nobody has whispered to you yet.").formatted(Formatting.RED), false);
            return false;
        }
        ServerPlayerEntity recipient = sender.getServer().getPlayerManager().getPlayer(recipientId);
        if (recipient == null) {
            sender.sendMessage(Text.literal("That player is no longer online.").formatted(Formatting.RED), false);
            return false;
        }
        return whisper(sender, recipient, message);
    }
}
