package panetina.elarion.core.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.model.ElarionNotificationEntry;
import panetina.elarion.core.model.RewardAction;
import panetina.elarion.core.model.ServerIdentityConfig;
import panetina.elarion.core.storage.RealmDeliveryStorage;
import panetina.elarion.core.storage.RealmDeliveryStorage.PendingDelivery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RealmDeliveryService {
    private final RealmDeliveryStorage storage;
    private final CitizenService citizens;
    private final RealmService realms;
    private final RewardActionService rewards;
    private final DeferredRewardGrantService deferredRewards;
    private final ElarionNotificationService notifications;
    private final HistoryService history;
    private final ServerIdentityConfig serverIdentity;
    private MinecraftServer server;
    private final List<PendingDelivery> pending = new ArrayList<>();

    public RealmDeliveryService(
            RealmDeliveryStorage storage,
            CitizenService citizens,
            RealmService realms,
            RewardActionService rewards,
            DeferredRewardGrantService deferredRewards,
            ElarionNotificationService notifications,
            HistoryService history,
            ServerIdentityConfig serverIdentity
    ) {
        this.storage = storage;
        this.citizens = citizens;
        this.realms = realms;
        this.rewards = rewards;
        this.deferredRewards = deferredRewards;
        this.notifications = notifications;
        this.history = history;
        this.serverIdentity = serverIdentity;
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        pending.clear();
        pending.addAll(storage.load(server));
        migrateLegacyNotifications();
    }

    public boolean rewardRealm(String realmId, String rewardId, UUID actorId) {
        if (realms.find(realmId).isEmpty()) return false;
        List<RewardAction> actions = rewards.actions(rewardId);
        if (actions.isEmpty()) return false;
        int queued = 0;
        for (CitizenRecord citizen : citizens.all()) {
            if (!realmId.equals(citizen.realmId())) continue;
            String grantId = "realm:" + realmId + ":reward:" + rewardId + ":" + citizen.uuid()
                    + ":" + System.currentTimeMillis();
            if (deferredRewards.enqueue(grantId, citizen.uuid(), "elarion_realms", rewardId, actions)) queued++;
        }
        save();
        history.record("realm", "realm-reward", actorId, "reward", rewardId, realmId,
                Map.of("queued", Integer.toString(queued)));
        return true;
    }

    public boolean giveItemRealm(String realmId, String itemId, int count, UUID actorId) {
        if (realms.find(realmId).isEmpty()) return false;
        Identifier id = Identifier.tryParse(itemId);
        if (id == null || !Registries.ITEM.containsId(id) || count < 1) return false;
        int queued = 0;
        for (CitizenRecord citizen : citizens.all()) {
            if (!realmId.equals(citizen.realmId())) continue;
            String grantId = "realm:" + realmId + ":item:" + id + ":" + count + ":" + citizen.uuid()
                    + ":" + System.currentTimeMillis();
            RewardAction action = new RewardAction("item", Map.of("id", id.toString(), "count", Integer.toString(count)));
            if (deferredRewards.enqueue(grantId, citizen.uuid(), "elarion_realms", id.toString(), List.of(action))) queued++;
        }
        save();
        history.record("realm", "realm-item-reward", actorId, "item", id.toString(), realmId,
                Map.of("count", Integer.toString(count), "queued", Integer.toString(queued)));
        return true;
    }

    public boolean announceRealm(String realmId, String message, UUID actorId) {
        return messageRealm(realmId, "announcement", serverIdentity.realmSingular() + " Announcement",
                message, actorId);
    }

    public boolean mailRealm(String realmId, String title, String message, UUID actorId) {
        return messageRealm(realmId, "mail", title, message, actorId);
    }

    public boolean mailRealm(String realmId, String message, UUID actorId) {
        return mailRealm(realmId, serverIdentity.realmSingular() + " Mail", message, actorId);
    }

    public boolean notifyRealm(String realmId, String title, String message, String type, UUID actorId) {
        return messageRealm(realmId, type == null || type.isBlank() ? "realm_event" : type,
                title, message, actorId);
    }

    public void deliverPending(ServerPlayerEntity player) {
        if (server == null) return;
        boolean changed = false;
        Iterator<PendingDelivery> iterator = pending.iterator();
        while (iterator.hasNext()) {
            PendingDelivery delivery = iterator.next();
            if (!player.getUuid().equals(delivery.playerId)) continue;
            CitizenRecord currentCitizen = citizens.getOrCreate(player);
            if (!delivery.realmId.equals(currentCitizen.realmId())) {
                history.record("realm", "offline-delivery-cancelled", null, "player",
                        player.getUuidAsString(), delivery.realmId,
                        Map.of("type", delivery.type, "reason", "realm-changed"));
                iterator.remove();
                changed = true;
                continue;
            }
            if ("reward".equals(delivery.type)) {
                List<RewardAction> actions = rewards.actions(delivery.payload);
                if (!actions.isEmpty()) {
                    deferredRewards.enqueue("realm:" + delivery.realmId + ":legacy-reward:" + delivery.payload
                            + ":" + player.getUuid() + ":" + delivery.createdAt,
                            player.getUuid(), "elarion_realms", delivery.payload, actions);
                }
            } else if ("item".equals(delivery.type)) {
                String[] parts = delivery.payload == null ? new String[0] : delivery.payload.split(" ", 2);
                Identifier id = parts.length > 0 ? Identifier.tryParse(parts[0]) : null;
                int count = parts.length > 1 ? parseCount(parts[1]) : 1;
                if (id != null && Registries.ITEM.containsId(id)) {
                    deferredRewards.enqueue("realm:" + delivery.realmId + ":legacy-item:" + id + ":" + count
                            + ":" + player.getUuid() + ":" + delivery.createdAt,
                            player.getUuid(), "elarion_realms", id.toString(),
                            List.of(new RewardAction("item",
                                    Map.of("id", id.toString(), "count", Integer.toString(count)))));
                }
            } else if (isRealmNotification(delivery.type)) {
                continue;
            }
            history.record("realm", "offline-delivery", null, "player",
                    player.getUuidAsString(), delivery.realmId, Map.of("type", delivery.type));
            iterator.remove();
            changed = true;
        }
        if (changed) save();
    }

    public List<ElarionNotificationEntry> notificationEntries(UUID recipientId) {
        return List.of();
    }

    public boolean dismissNotification(UUID recipientId, String notificationId) {
        if (recipientId == null || notificationId == null || notificationId.isBlank()) return false;
        return false;
    }

    private boolean messageRealm(String realmId, String type, String title, String message, UUID actorId) {
        if (realms.find(realmId).isEmpty() || message == null || message.isBlank()) return false;
        int queued = notifications.publishRealm(
                realmId,
                "government".equals(type) ? ElarionNotificationCategory.GOVERNMENT
                        : ElarionNotificationCategory.REALM,
                "elarion_realms",
                type,
                type + ":" + System.currentTimeMillis(),
                cleanTitle(type, title),
                message,
                status(type),
                icon(type),
                List.of(new ElarionNotificationAction(ElarionNotificationService.DISMISS, "Dismiss", true)),
                Map.of("realmId", realmId),
                notifications.defaultExpiry());
        history.record("realm", "realm-" + type, actorId, "realm", realmId, realmId,
                Map.of("queued", Integer.toString(queued)));
        return true;
    }

    private void save() {
        if (server != null) storage.save(server, pending);
    }

    private ServerPlayerEntity online(UUID uuid) {
        return server == null ? null : server.getPlayerManager().getPlayer(uuid);
    }

    private static int parseCount(String value) {
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private String status(String type) {
        return switch (type) {
            case "announcement" -> serverIdentity.realmSingular() + " announcement";
            case "mail" -> "Mail from admin";
            default -> serverIdentity.realmSingular() + " event";
        };
    }

    private static String icon(String type) {
        return switch (type) {
            case "announcement" -> "item:minecraft:bell";
            case "mail" -> "item:minecraft:writable_book";
            default -> "item:minecraft:paper";
        };
    }

    private String cleanTitle(String type, String title) {
        if (title != null && !title.isBlank()) return title.trim();
        return switch (type == null ? "" : type) {
            case "announcement" -> serverIdentity.realmSingular() + " Announcement";
            case "mail" -> serverIdentity.realmSingular() + " Mail";
            default -> serverIdentity.realmSingular() + " Event";
        };
    }

    private static boolean isRealmNotification(String type) {
        return "announcement".equals(type) || "mail".equals(type) || "realm_event".equals(type)
                || "government".equals(type) || "offering".equals(type);
    }

    private void migrateLegacyNotifications() {
        boolean changed = false;
        Iterator<PendingDelivery> iterator = pending.iterator();
        while (iterator.hasNext()) {
            PendingDelivery delivery = iterator.next();
            if (!isRealmNotification(delivery.type) || delivery.playerId == null) continue;
            notifications.publishPersonal(
                    delivery.playerId,
                    "government".equals(delivery.type)
                            ? ElarionNotificationCategory.GOVERNMENT : ElarionNotificationCategory.REALM,
                    "elarion_realms",
                    delivery.type,
                    "legacy:" + delivery.realmId + ":" + delivery.createdAt + ":" + delivery.playerId,
                    cleanTitle(delivery.type, delivery.title),
                    delivery.payload,
                    status(delivery.type),
                    icon(delivery.type),
                    List.of(new ElarionNotificationAction(ElarionNotificationService.DISMISS, "Dismiss", true)),
                    Map.of("realmId", delivery.realmId),
                    notifications.defaultExpiry());
            iterator.remove();
            changed = true;
        }
        if (changed) save();
    }
}
