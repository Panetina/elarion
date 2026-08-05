package panetina.elarion.core.service;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.DeferredRewardGrant;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.model.ElarionNotificationEntry;
import panetina.elarion.core.model.ElarionNotificationRewardPreview;
import panetina.elarion.core.model.ElarionNotificationSnapshot;
import panetina.elarion.core.model.RewardAction;
import panetina.elarion.core.network.NotificationSnapshotPayload;
import panetina.elarion.core.storage.DeferredRewardGrantStorage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class DeferredRewardGrantService {
    private final DeferredRewardGrantStorage storage;
    private final RewardActionService rewards;
    private final HistoryService history;
    private final Map<String, DeferredRewardGrant> grants = new LinkedHashMap<>();
    private final DeferredRewardGrantRuntimeIndex pendingIndex = new DeferredRewardGrantRuntimeIndex();
    private Consumer<ServerPlayerEntity> notificationSync = ignored -> {};
    private MinecraftServer server;

    public DeferredRewardGrantService(
            DeferredRewardGrantStorage storage,
            RewardActionService rewards,
            HistoryService history
    ) {
        this.storage = storage;
        this.rewards = rewards;
        this.history = history;
    }

    public synchronized void bind(MinecraftServer server) {
        this.server = server;
        grants.clear();
        pendingIndex.clear();
        for (Map.Entry<String, DeferredRewardGrant> entry : storage.load(server).entrySet()) {
            grants.put(entry.getKey(), entry.getValue());
            pendingIndex.add(entry.getValue());
        }
    }

    public synchronized boolean enqueue(
            String grantId,
            UUID recipientId,
            String sourceSystem,
            String sourceId,
            List<RewardAction> actions
    ) {
        return enqueueIdempotent(grantId, recipientId, sourceSystem, sourceId, actions) == EnqueueResult.ENQUEUED;
    }

    /**
     * Durable idempotency boundary for crash-replayed addon transactions.
     * An existing ID succeeds only when its immutable recipient/source/actions
     * are identical; a conflicting reuse fails closed.
     */
    public synchronized EnqueueResult enqueueIdempotent(
            String grantId,
            UUID recipientId,
            String sourceSystem,
            String sourceId,
            List<RewardAction> actions
    ) {
        if (grantId == null || grantId.isBlank() || recipientId == null || actions == null || actions.isEmpty()) {
            return EnqueueResult.INVALID;
        }
        DeferredRewardGrant existing = grants.get(grantId);
        if (existing != null) {
            return sameRequest(existing, recipientId, sourceSystem, sourceId, actions)
                    ? EnqueueResult.EXACT_RETRY
                    : EnqueueResult.CONFLICT;
        }
        DeferredRewardGrant grant = new DeferredRewardGrant(
                grantId, recipientId, sourceSystem, sourceId, actions, java.util.Set.of(),
                System.currentTimeMillis(), 0L);
        grants.put(grantId, grant);
        pendingIndex.add(grant);
        save();
        MinecraftServer boundServer = server;
        if (boundServer != null) {
            boundServer.execute(() -> {
                ServerPlayerEntity online = boundServer.getPlayerManager().getPlayer(recipientId);
                if (online != null) notificationSync.accept(online);
            });
        }
        return EnqueueResult.ENQUEUED;
    }

    public synchronized void setNotificationSync(Consumer<ServerPlayerEntity> notificationSync) {
        this.notificationSync = notificationSync == null ? (ignored -> {}) : notificationSync;
    }

    public synchronized ClaimResult claim(ServerPlayerEntity player, String grantId) {
        if (server == null || player == null) return ClaimResult.FAILED;
        DeferredRewardGrant original = grants.get(grantId);
        if (original == null || original.delivered() || !player.getUuid().equals(original.recipientId())) {
            player.sendMessage(Text.literal("Reward is no longer available."), false);
            notificationSync.accept(player);
            return ClaimResult.NOT_FOUND;
        }

        boolean changed = false;
        DeferredRewardGrant current = original;
        for (int index = 0; index < current.actions().size(); index++) {
            if (current.completedActions().contains(index)) continue;
            RewardAction action = current.actions().get(index);
            if (!rewards.executeAction(current.sourceId(), action, player)) {
                if (changed) save();
                player.sendMessage(Text.literal("Could not claim reward. Make room in your inventory and try again."),
                        false);
                notificationSync.accept(player);
                return ClaimResult.FAILED;
            }
            current = current.completeAction(index);
            grants.put(current.id(), current);
            save();
            changed = true;
        }
        if (current.complete()) {
            DeferredRewardGrant pending = current;
            current = current.markDelivered(System.currentTimeMillis());
            grants.put(current.id(), current);
            pendingIndex.update(pending, current);
            history.record("reward", "deferred-grant-delivered", player.getUuid(),
                    "reward_grant", current.id(), "",
                    Map.of("sourceSystem", current.sourceSystem(), "sourceId", current.sourceId()));
            player.sendMessage(Text.literal("Reward claimed."), false);
            changed = true;
        }
        if (changed) save();
        notificationSync.accept(player);
        return ClaimResult.CLAIMED;
    }

    public synchronized void deliverPending(ServerPlayerEntity player) {
        notificationSync.accept(player);
    }

    public synchronized void sync(ServerPlayerEntity player) {
        notificationSync.accept(player);
    }

    public synchronized ElarionNotificationSnapshot snapshot(UUID recipientId) {
        if (recipientId == null) return ElarionNotificationSnapshot.EMPTY;
        List<ElarionNotificationEntry> entries = new ArrayList<>();
        for (String grantId : pendingIndex.pendingIds(recipientId)) {
            DeferredRewardGrant grant = grants.get(grantId);
            if (grant == null || grant.delivered()) continue;
            entries.add(new ElarionNotificationEntry(
                    grant.id(),
                    ElarionNotificationCategory.REWARD,
                    "Reward available",
                    rewardBody(grant),
                    "Claim from mail",
                    "item:minecraft:chest",
                    true,
                    List.of(new ElarionNotificationAction("elarion_core:claim_reward", "Claim", true)),
                    rewardPreviews(grant),
                    grant.createdAt()));
        }
        return new ElarionNotificationSnapshot(entries);
    }

    public synchronized List<ElarionNotificationEntry> snapshotEntries(UUID recipientId) {
        return snapshot(recipientId).entries();
    }

    public synchronized int pendingCount(UUID recipientId) {
        return pendingIndex.pendingCount(recipientId);
    }

    public synchronized boolean isClaimable(String grantId, UUID recipientId) {
        DeferredRewardGrant grant = grants.get(grantId);
        return grant != null && !grant.delivered() && grant.recipientId().equals(recipientId);
    }

    public synchronized void save() {
        if (server != null) storage.save(server, grants);
    }

    public synchronized int resetAllPlayerState() {
        int count = grants.size();
        grants.clear();
        pendingIndex.clear();
        save();
        return count;
    }

    private static String rewardBody(DeferredRewardGrant grant) {
        return switch (grant.sourceSystem()) {
            case "elarion_realms" -> "Rewards from the Realm Master.";
            case "elarion_offerings" -> "Rewards from the Shrine of Foundation.";
            default -> "A reward is waiting to be claimed.";
        };
    }

    private static List<ElarionNotificationRewardPreview> rewardPreviews(DeferredRewardGrant grant) {
        List<ElarionNotificationRewardPreview> previews = new ArrayList<>();
        for (RewardAction action : grant.actions()) {
            String type = action.type() == null ? "" : action.type().trim().toLowerCase(java.util.Locale.ROOT);
            if ("item".equals(type)) {
                String id = action.parameters().getOrDefault("id", "");
                int count = parsePositive(action.parameters().getOrDefault("count", "1"), 1);
                previews.add(new ElarionNotificationRewardPreview(
                        previewItemLabel(id, action.parameters()),
                        "item:" + id,
                        count,
                        itemTooltipLines(action.parameters())));
            } else if (type.contains("currency") && type.contains("reward")) {
                int amount = parsePositive(action.parameters().getOrDefault("amount", "0"), 0);
                previews.add(new ElarionNotificationRewardPreview("Sigils",
                        "bank", amount));
            }
        }
        return List.copyOf(previews);
    }

    private static int parsePositive(String raw, int fallback) {
        try {
            return Math.max(0, Integer.parseInt(raw));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static String shortItemName(String id) {
        if (id == null || id.isBlank()) return "Item";
        int separator = id.indexOf(':');
        String path = separator >= 0 ? id.substring(separator + 1) : id;
        String[] words = path.split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) continue;
            if (!builder.isEmpty()) builder.append(' ');
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.isEmpty() ? path : builder.toString();
    }

    private static String previewItemLabel(String id, Map<String, String> parameters) {
        String display = parameters.getOrDefault("display-label", parameters.getOrDefault("name", ""));
        return display == null || display.isBlank() ? shortItemName(id) : display.trim();
    }

    private static List<String> itemTooltipLines(Map<String, String> parameters) {
        List<String> lines = new ArrayList<>();
        String customName = parameters.getOrDefault("name", "");
        if (customName != null && !customName.isBlank()) lines.add(customName.trim());
        String raw = parameters.getOrDefault("enchants", parameters.getOrDefault("enchantments", ""));
        if (raw != null && !raw.isBlank()) {
            for (String entry : raw.split(",")) {
                String visible = enchantmentTooltipLine(entry);
                if (!visible.isBlank()) lines.add(visible);
                if (lines.size() >= 8) break;
            }
        }
        return List.copyOf(lines);
    }

    private static String enchantmentTooltipLine(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        int separator = trimmed.lastIndexOf(':');
        if (separator <= 0 || separator >= trimmed.length() - 1) return "";
        Identifier id = Identifier.tryParse(trimmed.substring(0, separator).trim());
        String level = trimmed.substring(separator + 1).trim();
        if (id == null || level.isBlank()) return "";
        return shortItemName(id.getPath()) + " " + enchantmentLevelLabel(level);
    }

    private static String enchantmentLevelLabel(String raw) {
        try {
            return switch (Integer.parseInt(raw)) {
                case 1 -> "I";
                case 2 -> "II";
                case 3 -> "III";
                case 4 -> "IV";
                case 5 -> "V";
                case 6 -> "VI";
                case 7 -> "VII";
                case 8 -> "VIII";
                case 9 -> "IX";
                case 10 -> "X";
                default -> raw;
            };
        } catch (NumberFormatException exception) {
            return raw;
        }
    }

    public enum ClaimResult {
        CLAIMED,
        FAILED,
        NOT_FOUND
    }

    public enum EnqueueResult {
        ENQUEUED,
        EXACT_RETRY,
        CONFLICT,
        INVALID
    }

    private static boolean sameRequest(
            DeferredRewardGrant existing,
            UUID recipientId,
            String sourceSystem,
            String sourceId,
            List<RewardAction> actions
    ) {
        return existing.recipientId().equals(recipientId)
                && existing.sourceSystem().equals(sourceSystem == null ? "" : sourceSystem)
                && existing.sourceId().equals(sourceId == null ? "" : sourceId)
                && existing.actions().equals(List.copyOf(actions));
    }
}
