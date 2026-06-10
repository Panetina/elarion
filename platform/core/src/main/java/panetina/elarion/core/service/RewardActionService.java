package panetina.elarion.core.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.model.CitizenStatus;
import panetina.elarion.core.model.RewardAction;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RewardActionService {
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
        handlers.put(normalize(type), handler);
    }

    public static Set<String> builtInActionTypes() {
        return BUILT_IN_ACTION_TYPES;
    }

    public Set<String> rewardIds() {
        return config.rewards().keySet();
    }

    public boolean executeReward(String rewardId, ServerPlayerEntity player) {
        List<RewardAction> actions = config.rewards().get(normalize(rewardId));
        if (actions == null) return false;
        Context context = new Context(player.getServer(), player, normalize(rewardId));
        boolean success = true;
        for (RewardAction action : actions) {
            ActionHandler handler = handlers.get(normalize(action.type()));
            success &= handler != null && handler.execute(context, action);
        }
        events.emitProgression(new ElarionEventBus.ProgressionEvent(
                "reward.executed", player.getUuid(), normalize(rewardId)));
        return success;
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
            ItemStack stack = new ItemStack(item, count);
            if (!context.player().getInventory().insertStack(stack)) {
                context.player().dropItem(stack, false);
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

    private static String interpolate(String command, Context context) {
        return command
                .replace("{player}", context.player().getGameProfile().getName())
                .replace("{uuid}", context.player().getUuidAsString())
                .replace("{reward}", context.rewardId());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
