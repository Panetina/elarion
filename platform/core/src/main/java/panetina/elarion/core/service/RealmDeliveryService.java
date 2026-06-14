package panetina.elarion.core.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.CitizenRecord;
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
    private final HistoryService history;
    private final ServerIdentityConfig serverIdentity;
    private MinecraftServer server;
    private final List<PendingDelivery> pending = new ArrayList<>();

    public RealmDeliveryService(
            RealmDeliveryStorage storage,
            CitizenService citizens,
            RealmService realms,
            RewardActionService rewards,
            HistoryService history,
            ServerIdentityConfig serverIdentity
    ) {
        this.storage = storage;
        this.citizens = citizens;
        this.realms = realms;
        this.rewards = rewards;
        this.history = history;
        this.serverIdentity = serverIdentity;
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        pending.clear();
        pending.addAll(storage.load(server));
    }

    public boolean rewardRealm(String realmId, String rewardId, UUID actorId) {
        if (realms.find(realmId).isEmpty()) return false;
        int queued = 0;
        int delivered = 0;
        for (CitizenRecord citizen : citizens.all()) {
            if (!realmId.equals(citizen.realmId())) continue;
            ServerPlayerEntity player = online(citizen.uuid());
            if (player != null && rewards.executeReward(rewardId, player)) {
                delivered++;
            } else {
                pending.add(new PendingDelivery(citizen.uuid(), "reward", realmId, rewardId));
                queued++;
            }
        }
        save();
        history.record("realm", "realm-reward", actorId, "reward", rewardId, realmId,
                Map.of("delivered", Integer.toString(delivered), "queued", Integer.toString(queued)));
        return true;
    }

    public boolean giveItemRealm(String realmId, String itemId, int count, UUID actorId) {
        if (realms.find(realmId).isEmpty()) return false;
        Identifier id = Identifier.tryParse(itemId);
        if (id == null || !Registries.ITEM.containsId(id) || count < 1) return false;
        int queued = 0;
        int delivered = 0;
        String payload = id + " " + count;
        for (CitizenRecord citizen : citizens.all()) {
            if (!realmId.equals(citizen.realmId())) continue;
            ServerPlayerEntity player = online(citizen.uuid());
            if (player != null && giveItem(player, id, count)) {
                delivered++;
            } else {
                pending.add(new PendingDelivery(citizen.uuid(), "item", realmId, payload));
                queued++;
            }
        }
        save();
        history.record("realm", "realm-item-reward", actorId, "item", id.toString(), realmId,
                Map.of("count", Integer.toString(count), "delivered", Integer.toString(delivered),
                        "queued", Integer.toString(queued)));
        return true;
    }

    public boolean announceRealm(String realmId, String message, UUID actorId) {
        return messageRealm(realmId, "announcement", message, actorId);
    }

    public boolean mailRealm(String realmId, String message, UUID actorId) {
        return messageRealm(realmId, "mail", message, actorId);
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
                rewards.executeReward(delivery.payload, player);
            } else if ("item".equals(delivery.type)) {
                String[] parts = delivery.payload == null ? new String[0] : delivery.payload.split(" ", 2);
                Identifier id = parts.length > 0 ? Identifier.tryParse(parts[0]) : null;
                int count = parts.length > 1 ? parseCount(parts[1]) : 1;
                if (id != null) giveItem(player, id, count);
            } else if ("announcement".equals(delivery.type)) {
                player.sendMessage(Text.literal("[" + serverIdentity.realmSingular() + " Announcement] "
                                + delivery.payload)
                        .formatted(Formatting.GOLD), false);
            } else if ("mail".equals(delivery.type)) {
                player.sendMessage(Text.literal("[" + serverIdentity.realmSingular() + " Mail] " + delivery.payload)
                        .formatted(Formatting.AQUA), false);
            }
            history.record("realm", "offline-delivery", null, "player",
                    player.getUuidAsString(), delivery.realmId, Map.of("type", delivery.type));
            iterator.remove();
            changed = true;
        }
        if (changed) save();
    }

    private boolean messageRealm(String realmId, String type, String message, UUID actorId) {
        if (realms.find(realmId).isEmpty() || message == null || message.isBlank()) return false;
        int queued = 0;
        int delivered = 0;
        for (CitizenRecord citizen : citizens.all()) {
            if (!realmId.equals(citizen.realmId())) continue;
            ServerPlayerEntity player = online(citizen.uuid());
            if (player != null) {
                player.sendMessage(Text.literal(prefix(type) + message).formatted(color(type)), false);
                delivered++;
            } else {
                pending.add(new PendingDelivery(citizen.uuid(), type, realmId, message));
                queued++;
            }
        }
        save();
        history.record("realm", "realm-" + type, actorId, "realm", realmId, realmId,
                Map.of("delivered", Integer.toString(delivered), "queued", Integer.toString(queued)));
        return true;
    }

    private ServerPlayerEntity online(UUID uuid) {
        return server == null ? null : server.getPlayerManager().getPlayer(uuid);
    }

    private void save() {
        if (server != null) storage.save(server, pending);
    }

    private static boolean giveItem(ServerPlayerEntity player, Identifier id, int count) {
        if (!Registries.ITEM.containsId(id) || count < 1) return false;
        Item item = Registries.ITEM.get(id);
        int remaining = count;
        while (remaining > 0) {
            int stackCount = Math.min(remaining, item.getMaxCount());
            ItemStack stack = new ItemStack(item, stackCount);
            if (!player.getInventory().insertStack(stack)) {
                player.dropItem(stack, false);
            }
            remaining -= stackCount;
        }
        return true;
    }

    private static int parseCount(String value) {
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private String prefix(String type) {
        return "announcement".equals(type)
                ? "[" + serverIdentity.realmSingular() + " Announcement] "
                : "[" + serverIdentity.realmSingular() + " Mail] ";
    }

    private static Formatting color(String type) {
        return "announcement".equals(type) ? Formatting.GOLD : Formatting.AQUA;
    }
}
