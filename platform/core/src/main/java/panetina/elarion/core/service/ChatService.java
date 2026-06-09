package panetina.elarion.core.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.PlayerIdentity;

public final class ChatService {
    private final CoreConfigManager config;
    private final CitizenService citizens;
    private final RealmService realms;
    private final IdentityService identities;

    public ChatService(
            CoreConfigManager config,
            CitizenService citizens,
            RealmService realms,
            IdentityService identities
    ) {
        this.config = config;
        this.citizens = citizens;
        this.realms = realms;
        this.identities = identities;
    }

    public boolean sendRealmMessage(ServerPlayerEntity sender, String message) {
        CitizenRecord senderCitizen = citizens.getOrCreate(sender);
        RealmDefinition realm = realms.forCitizen(senderCitizen).orElse(null);
        if (realm == null) {
            sender.sendMessage(Text.literal("You are not assigned to a realm."), false);
            return false;
        }

        PlayerIdentity identity = identities.resolve(sender);
        Text output = renderMessage(config.realmChatFormat(), realm, identity, message);
        MinecraftServer server = sender.getServer();
        for (ServerPlayerEntity recipient : server.getPlayerManager().getPlayerList()) {
            CitizenRecord recipientCitizen = citizens.getOrCreate(recipient);
            if (realm.id().equals(recipientCitizen.realmId())) {
                recipient.sendMessage(output, false);
            }
        }
        return true;
    }

    private static Text renderMessage(
            String format,
            RealmDefinition realm,
            PlayerIdentity identity,
            String message
    ) {
        MutableText output = Text.empty();
        int cursor = 0;
        while (cursor < format.length()) {
            int tokenStart = format.indexOf('%', cursor);
            if (tokenStart < 0) {
                output.append(Text.literal(format.substring(cursor)));
                break;
            }

            if (tokenStart > cursor) {
                output.append(Text.literal(format.substring(cursor, tokenStart)));
            }

            int tokenEnd = format.indexOf('%', tokenStart + 1);
            if (tokenEnd < 0) {
                output.append(Text.literal(format.substring(tokenStart)));
                break;
            }

            String token = format.substring(tokenStart, tokenEnd + 1);
            switch (token) {
                case "%realm_short%" ->
                        output.append(Text.literal(realm.shortName()).formatted(identity.color()));
                case "%realm%" ->
                        output.append(Text.literal(realm.displayName()).formatted(identity.color()));
                case "%player%" -> output.append(identity.chatName());
                case "%message%" -> output.append(Text.literal(message));
                default -> output.append(Text.literal(token));
            }
            cursor = tokenEnd + 1;
        }
        return output;
    }
}
