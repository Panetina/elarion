package panetina.elarion.core.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.RealmRelationship;
import panetina.elarion.core.model.PlayerIdentity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ChatService {
    private final CoreConfigManager config;
    private final CitizenService citizens;
    private final RealmService realms;
    private final IdentityService identities;
    private final HistoryService history;
    private final RealmGovernanceService governance;
    private final Map<UUID, Long> lastYellAt = new ConcurrentHashMap<>();
    private final Set<UUID> chatSpies = ConcurrentHashMap.newKeySet();

    public ChatService(
            CoreConfigManager config,
            CitizenService citizens,
            RealmService realms,
            IdentityService identities,
            HistoryService history,
            RealmGovernanceService governance
    ) {
        this.config = config;
        this.citizens = citizens;
        this.realms = realms;
        this.identities = identities;
        this.history = history;
        this.governance = governance;
    }

    public boolean sendRealmMessage(ServerPlayerEntity sender, String message) {
        if (message == null || message.isBlank()) return false;
        CitizenRecord senderCitizen = citizens.getOrCreate(sender);
        RealmDefinition realm = realms.forCitizen(senderCitizen).orElse(null);
        if (realm == null) {
            sender.sendMessage(Text.literal("You are not assigned to a " + config.serverIdentity().realmSingular() + "."), false);
            return false;
        }

        PlayerIdentity identity = identities.resolve(sender);
        Text output = renderMessage(config.realmChatFormat(), realm, identity, message);
        MinecraftServer server = sender.getServer();
        for (ServerPlayerEntity recipient : server.getPlayerManager().getPlayerList()) {
            CitizenRecord recipientCitizen = citizens.getOrCreate(recipient);
            if (realm.id().equals(recipientCitizen.realmId())) {
                recipient.sendMessage(output, false);
            } else if (isChatSpy(recipient)) {
                recipient.sendMessage(spyMessage(config.serverIdentity().realmChatLabel() + ":" + realms.shortName(realm),
                        identity, message), false);
            }
        }
        history.record("chat", "realm-message", sender.getUuid(), "player",
                sender.getUuidAsString(), realm.id(), Map.of("channel", "realm"));
        return true;
    }

    public boolean sendAllianceMessage(ServerPlayerEntity sender, String message) {
        if (message == null || message.isBlank()) return false;
        CitizenRecord senderCitizen = citizens.getOrCreate(sender);
        RealmDefinition realm = realms.forCitizen(senderCitizen).orElse(null);
        if (realm == null) {
            sender.sendMessage(Text.literal("You are not assigned to a " + config.serverIdentity().realmSingular() + "."), false);
            return false;
        }
        PlayerIdentity identity = identities.resolve(sender);
        Text output = renderMessage(config.allianceChatFormat(), realm, identity, message);
        Set<String> recipientRealms = allianceChatRealms(realm.id());
        int recipients = 0;
        for (ServerPlayerEntity recipient : sender.getServer().getPlayerManager().getPlayerList()) {
            CitizenRecord recipientCitizen = citizens.getOrCreate(recipient);
            if (recipientRealms.contains(recipientCitizen.realmId())) {
                recipient.sendMessage(output, false);
                recipients++;
            } else if (isChatSpy(recipient)) {
                recipient.sendMessage(spyMessage(
                        config.serverIdentity().allianceChatLabel() + ":" + realms.shortName(realm),
                        identity,
                        message
                ), false);
            }
        }
        if (recipients <= 1) {
            sender.sendMessage(Text.literal("Your " + config.serverIdentity().realmSingular()
                            + " has no online allied citizens to receive alliance chat.")
                    .formatted(Formatting.RED), false);
        }
        history.record("chat", "alliance-message", sender.getUuid(), "player",
                sender.getUuidAsString(), realm.id(), Map.of("channel", "alliance"));
        return true;
    }

    public boolean toggleChatSpy(ServerPlayerEntity player) {
        if (!config.localChatAdminSpy()) {
            player.sendMessage(Text.literal("Chat spy is disabled in chat.yml.")
                    .formatted(Formatting.RED), false);
            return false;
        }
        UUID id = player.getUuid();
        boolean enabled;
        if (chatSpies.contains(id)) {
            chatSpies.remove(id);
            enabled = false;
        } else {
            chatSpies.add(id);
            enabled = true;
        }
        player.sendMessage(Text.literal("Chat spy " + (enabled ? "enabled" : "disabled") + ".")
                .formatted(enabled ? Formatting.GREEN : Formatting.YELLOW), false);
        return enabled;
    }

    public boolean isChatSpy(ServerPlayerEntity player) {
        return config.localChatAdminSpy() && player.hasPermissionLevel(4) && chatSpies.contains(player.getUuid());
    }

    public void spyPrivateMessage(
            ServerPlayerEntity sender,
            ServerPlayerEntity recipient,
            String message
    ) {
        PlayerIdentity senderIdentity = identities.resolve(sender);
        PlayerIdentity recipientIdentity = identities.resolve(recipient);
        Text spy = Text.literal("[Spy:PM] ")
                .append(senderIdentity.chatName())
                .append(Text.literal(" -> "))
                .append(recipientIdentity.chatName())
                .append(Text.literal(": " + message).formatted(Formatting.GRAY));
        for (ServerPlayerEntity viewer : sender.getServer().getPlayerManager().getPlayerList()) {
            if (viewer.getUuid().equals(sender.getUuid()) || viewer.getUuid().equals(recipient.getUuid())) continue;
            if (isChatSpy(viewer)) viewer.sendMessage(spy, false);
        }
    }

    public boolean sendLocalMessage(ServerPlayerEntity sender, String message) {
        if (message == null || message.isBlank()) return false;
        return sendProximityMessage(
                sender,
                message,
                config.localChatFormat(),
                config.localChatRadius(),
                null,
                "local-message",
                "local"
        );
    }

    public boolean sendWhisperMessage(ServerPlayerEntity sender, String message) {
        if (message == null || message.isBlank()) return false;
        return sendProximityMessage(
                sender,
                message,
                config.whisperChatFormat(),
                config.whisperChatRadius(),
                Formatting.ITALIC,
                "whisper-message",
                "whisper"
        );
    }

    public boolean sendYellMessage(ServerPlayerEntity sender, String message) {
        if (message == null || message.isBlank()) return false;
        long now = System.currentTimeMillis();
        long cooldownMillis = config.yellChatCooldownSeconds() * 1000L;
        long availableAt = lastYellAt.getOrDefault(sender.getUuid(), 0L) + cooldownMillis;
        if (now < availableAt) {
            long seconds = Math.max(1L, (availableAt - now + 999L) / 1000L);
            sender.sendMessage(Text.literal(
                    "You can yell again in " + seconds + " seconds.").formatted(Formatting.RED), false);
            return false;
        }
        boolean sent = sendProximityMessage(
                sender,
                message,
                config.yellChatFormat(),
                config.yellChatRadius(),
                Formatting.BOLD,
                "yell-message",
                "yell"
        );
        if (sent) lastYellAt.put(sender.getUuid(), now);
        return sent;
    }

    private boolean sendProximityMessage(
            ServerPlayerEntity sender,
            String message,
            String format,
            int radius,
            Formatting style,
            String historyType,
            String channel
    ) {
        PlayerIdentity identity = identities.resolve(sender);
        MutableText output = renderMessage(format, null, identity, message).copy();
        if (style != null) output.formatted(style);
        double rangeSquared = (double) radius * radius;
        for (ServerPlayerEntity recipient : sender.getServer().getPlayerManager().getPlayerList()) {
            boolean sameWorld = sender.getServerWorld() == recipient.getServerWorld();
            boolean inRange = sender.squaredDistanceTo(recipient) <= rangeSquared;
            boolean naturalRecipient = (!config.localChatSameWorldOnly() || sameWorld) && inRange;
            if (naturalRecipient) {
                recipient.sendMessage(output, false);
            } else if (isChatSpy(recipient)) {
                recipient.sendMessage(spyMessage(channelLabel(channel), identity, message), false);
            }
        }
        CitizenRecord citizen = citizens.getOrCreate(sender);
        history.record("chat", historyType, sender.getUuid(), "player",
                sender.getUuidAsString(), citizen.realmId(), Map.of(
                        "channel", channel,
                        "radius", Integer.toString(radius),
                        "world", sender.getServerWorld().getRegistryKey().getValue().toString()
                ));
        return true;
    }

    public void sendJoinNotice(ServerPlayerEntity player) {
        sendScopedNotice(player, "joined", "join");
    }

    public void sendLeaveNotice(ServerPlayerEntity player) {
        sendScopedNotice(player, "left", "leave");
    }

    private void sendScopedNotice(ServerPlayerEntity player, String action, String historyType) {
        if (!config.scopedJoinLeaveNotices()) return;
        CitizenRecord citizen = citizens.getOrCreate(player);
        RealmDefinition realm = realms.forCitizen(citizen).orElse(null);
        PlayerIdentity identity = identities.resolve(player);
        Text realmNotice = renderNotice(config.realmNoticeFormat().replace("joined", action), realm, identity);
        Text adminNotice = renderNotice(config.adminNoticeFormat().replace("joined", action), realm, identity)
                .copy().formatted(Formatting.GRAY);
        for (ServerPlayerEntity recipient : player.getServer().getPlayerManager().getPlayerList()) {
            if (recipient == player) continue;
            CitizenRecord recipientCitizen = citizens.getOrCreate(recipient);
            if (realm != null && realm.id().equals(recipientCitizen.realmId())) {
                recipient.sendMessage(realmNotice, false);
            } else if (recipient.hasPermissionLevel(4)) {
                recipient.sendMessage(adminNotice, false);
            }
        }
        history.record("chat", historyType, player.getUuid(), "player",
                player.getUuidAsString(), citizen.realmId(), Map.of("channel", "notice"));
    }

    public boolean shouldBlockVanillaGameNotice(Text message) {
        if (!config.scopedJoinLeaveNotices()) return false;
        String text = message.getString();
        return text.endsWith(" joined the game") || text.endsWith(" left the game");
    }

    private Text renderMessage(
            String format,
            RealmDefinition realm,
            PlayerIdentity identity,
            String message
    ) {
        MutableText output = Text.empty();
        int cursor = 0;
        String renderedFormat = config.serverIdentity().replace(format);
        while (cursor < renderedFormat.length()) {
            int tokenStart = renderedFormat.indexOf('%', cursor);
            if (tokenStart < 0) {
                output.append(Text.literal(renderedFormat.substring(cursor)));
                break;
            }

            if (tokenStart > cursor) {
                output.append(Text.literal(renderedFormat.substring(cursor, tokenStart)));
            }

            int tokenEnd = renderedFormat.indexOf('%', tokenStart + 1);
            if (tokenEnd < 0) {
                output.append(Text.literal(renderedFormat.substring(tokenStart)));
                break;
            }

            String token = renderedFormat.substring(tokenStart, tokenEnd + 1);
            switch (token) {
                case "%realm_short%" ->
                        output.append(Text.literal(realm == null ? "" : realms.shortName(realm)).formatted(identity.color()));
                case "%realm%" ->
                        output.append(Text.literal(realm == null ? "" : realms.officialName(realm)).formatted(identity.color()));
                case "%player%" -> output.append(identity.chatName());
                case "%message%" -> output.append(Text.literal(message));
                default -> output.append(Text.literal(token));
            }
            cursor = tokenEnd + 1;
        }
        return output;
    }

    private Set<String> allianceChatRealms(String realmId) {
        return realms.all().stream()
                .map(RealmDefinition::id)
                .filter(candidate -> realmId.equals(candidate)
                        || governance.relationship(realmId, candidate) == RealmRelationship.ALLY)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Text spyMessage(String channel, PlayerIdentity identity, String message) {
        return Text.literal("[Spy:" + channel + "] ")
                .append(identity.chatName())
                .append(Text.literal(": " + message).formatted(Formatting.GRAY));
    }

    private String channelLabel(String channel) {
        return switch (channel) {
            case "whisper" -> "Whisper";
            case "yell" -> "Yell";
            default -> config.serverIdentity().localChatLabel();
        };
    }

    private Text renderNotice(String format, RealmDefinition realm, PlayerIdentity identity) {
        return renderMessage(format, realm, identity, "");
    }
}
