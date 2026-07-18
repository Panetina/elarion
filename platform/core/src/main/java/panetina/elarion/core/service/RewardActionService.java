package panetina.elarion.core.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.text.Text;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.model.CitizenStatus;
import panetina.elarion.core.model.RewardAction;

import java.util.List;
import java.util.Locale;
import panetina.elarion.core.placeholder.ElarionPlaceholderService;
import panetina.elarion.core.placeholder.PlaceholderRenderContext;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RewardActionService {
    static final int MAX_CUSTOM_ITEM_NAME_LENGTH = 80;

    private static final Set<String> BUILT_IN_ACTION_TYPES = Set.of(
            "message",
            "item",
            "broadcast",
            "server-command",
            "player-command",
            "status-change",
            "title-grant",
            "ability-grant",
            "history-event"
    );

    @FunctionalInterface
    public interface ActionHandler {
        boolean execute(Context context, RewardAction action);
    }

    public record Context(MinecraftServer server, ServerPlayerEntity player, String rewardId) {}

    private final CoreConfigManager config;
    private final CitizenService citizens;
    private final TitleService titles;
    private final AbilityService abilities;
    private final ElarionEventBus events;
    private final Map<String, ActionHandler> handlers = new ConcurrentHashMap<>();

    public RewardActionService(
            CoreConfigManager config,
            CitizenService citizens,
            TitleService titles,
            AbilityService abilities,
            ElarionEventBus events
    ) {
        this.config = config;
        this.citizens = citizens;
        this.titles = titles;
        this.abilities = abilities;
        this.events = events;
        registerBuiltIns();
    }

    public void registerHandler(String type, ActionHandler handler) {
        handlers.put(normalizeActionType(type), handler);
    }

    public static Set<String> builtInActionTypes() {
        return BUILT_IN_ACTION_TYPES;
    }

    public Set<String> rewardIds() {
        return config.rewards().keySet();
    }

    public List<RewardAction> actions(String rewardId) {
        return config.rewards().getOrDefault(normalizeRewardId(rewardId), List.of());
    }

    public boolean executeReward(String rewardId, ServerPlayerEntity player) {
        String normalizedRewardId = normalizeRewardId(rewardId);
        List<RewardAction> actions = config.rewards().get(normalizedRewardId);
        if (actions == null) return false;
        Context context = new Context(player.getServer(), player, normalizedRewardId);
        boolean success = true;
        for (RewardAction action : actions) {
            ActionHandler handler = handlers.get(normalizeActionType(action.type()));
            success &= handler != null && handler.execute(context, action);
        }
        events.emitProgression(new ElarionEventBus.ProgressionEvent(
                "reward.executed", player.getUuid(), normalizedRewardId));
        return success;
    }

    public boolean executeAction(String sourceId, RewardAction action, ServerPlayerEntity player) {
        if (action == null || player == null) return false;
        ActionHandler handler = handlers.get(normalizeActionType(action.type()));
        return handler != null && handler.execute(
                new Context(player.getServer(), player, normalizeRewardId(sourceId)), action);
    }

    private void registerBuiltIns() {
        registerHandler("message", (context, action) -> {
            context.player().sendMessage(Text.literal(action.parameters().getOrDefault("text", "")), false);
            return true;
        });
        registerHandler("item", (context, action) -> {
            Identifier id = Identifier.tryParse(action.parameters().getOrDefault("id", ""));
            if (id == null || !Registries.ITEM.containsId(id)) return false;
            int count;
            try {
                count = Math.max(1, Integer.parseInt(action.parameters().getOrDefault("count", "1")));
            } catch (NumberFormatException exception) {
                return false;
            }
            Item item = Registries.ITEM.get(id);
            ItemStack prototype = new ItemStack(item, count);
            applyCustomName(prototype, action.parameters());
            applyEnchantments(context, prototype, action.parameters());
            if (!canFit(context.player().getInventory(), prototype, count)) {
                return false;
            }
            int remaining = count;
            while (remaining > 0) {
                int stackCount = Math.min(remaining, item.getMaxCount());
                ItemStack stack = prototype.copy();
                stack.setCount(stackCount);
                if (!context.player().getInventory().insertStack(stack) || !stack.isEmpty()) {
                    return false;
                }
                remaining -= stackCount;
            }
            return true;
        });
        registerHandler("broadcast", (context, action) -> {
            context.server().getPlayerManager().broadcast(
                    Text.literal(action.parameters().getOrDefault("text", "")), false);
            return true;
        });
        registerHandler("server-command", (context, action) -> {
            String command = action.parameters().get("command");
            if (command == null || command.isBlank()) return false;
            context.server().getCommandManager().executeWithPrefix(context.server().getCommandSource(), interpolate(command, context));
            return true;
        });
        registerHandler("player-command", (context, action) -> {
            String command = action.parameters().get("command");
            if (command == null || command.isBlank()) return false;
            context.server().getCommandManager().executeWithPrefix(context.player().getCommandSource(), interpolate(command, context));
            return true;
        });
        registerHandler("status-change", (context, action) -> {
            try {
                CitizenStatus status = CitizenStatus.valueOf(action.parameters().getOrDefault("status", "").toUpperCase(Locale.ROOT));
                citizens.update(context.player(), "reward-status", citizen -> citizen.setStatus(status));
                return true;
            } catch (IllegalArgumentException exception) {
                return false;
            }
        });
        registerHandler("title-grant", (context, action) ->
                titles.assign(context.player(), action.parameters().get("title")));
        registerHandler("ability-grant", (context, action) -> {
            String ability = action.parameters().get("ability");
            if (ability == null) return false;
            citizens.update(context.player(), "reward-ability", citizen -> abilities.grant(citizen, ability));
            return true;
        });
        registerHandler("history-event", (context, action) -> {
            events.emitProgression(new ElarionEventBus.ProgressionEvent(
                    action.parameters().getOrDefault("event", "history.event"),
                    context.player().getUuid(),
                    action.parameters().getOrDefault("subject", context.rewardId())));
            return true;
        });
    }

    static void applyCustomName(ItemStack stack, Map<String, String> parameters) {
        customItemName(parameters).ifPresent(name ->
                stack.set(DataComponentTypes.CUSTOM_NAME, name));
    }

    static Optional<Text> customItemName(Map<String, String> parameters) {
        String raw = parameters.getOrDefault("name", "");
        if (raw == null || raw.isBlank()) return Optional.empty();
        String name = raw.trim();
        if (name.length() > MAX_CUSTOM_ITEM_NAME_LENGTH) {
            name = name.substring(0, MAX_CUSTOM_ITEM_NAME_LENGTH);
        }
        return Optional.of(Text.literal(name));
    }

    private static void applyEnchantments(Context context, ItemStack stack, Map<String, String> parameters) {
        String raw = parameters.getOrDefault("enchants", parameters.getOrDefault("enchantments", ""));
        if (raw == null || raw.isBlank()) return;
        for (String entry : raw.split(",")) {
            String trimmed = entry.trim();
            int separator = trimmed.lastIndexOf(':');
            if (separator <= 0 || separator >= trimmed.length() - 1) continue;
            Identifier id = Identifier.tryParse(trimmed.substring(0, separator).trim());
            if (id == null) continue;
            java.util.Optional<RegistryEntry.Reference<net.minecraft.enchantment.Enchantment>> enchantment =
                    context.server().getRegistryManager()
                            .getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                            .getOptional(RegistryKey.of(RegistryKeys.ENCHANTMENT, id));
            if (enchantment.isEmpty()) continue;
            try {
                int level = Math.max(1, Integer.parseInt(trimmed.substring(separator + 1).trim()));
                stack.addEnchantment(enchantment.get(), level);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static boolean canFit(PlayerInventory inventory, ItemStack prototype, int count) {
        if (prototype.isEmpty() || count < 1) return false;
        int remaining = count;
        for (ItemStack existing : inventory.main) {
            if (existing.isEmpty()) {
                remaining -= Math.min(remaining, prototype.getMaxCount());
            } else if (ItemStack.areItemsAndComponentsEqual(existing, prototype)) {
                remaining -= Math.max(0, Math.min(existing.getMaxCount(), prototype.getMaxCount()) - existing.getCount());
            }
            if (remaining <= 0) return true;
        }
        for (ItemStack existing : inventory.offHand) {
            if (ItemStack.areItemsAndComponentsEqual(existing, prototype)) {
                remaining -= Math.max(0, Math.min(existing.getMaxCount(), prototype.getMaxCount()) - existing.getCount());
            }
            if (remaining <= 0) return true;
        }
        return false;
    }

    private static String interpolate(String command, Context context) {
        return ElarionPlaceholderService.resolveSchema(command, "platform:core/rewards",
                PlaceholderRenderContext.COMMAND, Map.of(
                        "player", context.player().getGameProfile().getName(),
                        "uuid", context.player().getUuidAsString(),
                        "reward", context.rewardId())).text();
    }

    private static String normalizeActionType(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    static String normalizeRewardId(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
    }
}
