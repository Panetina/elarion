package panetina.elarion.core.service;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.ElarionChatChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/** Core-owned extension point for addon-owned chat channels. */
public final class ElarionChatChannelRouter {
    private static final Map<ElarionChatChannel, BiFunction<ServerPlayerEntity, String, Boolean>> ROUTES = new ConcurrentHashMap<>();
    private static final Map<ElarionChatChannel, Predicate<ServerPlayerEntity>> ELIGIBILITY = new ConcurrentHashMap<>();
    private ElarionChatChannelRouter() { }
    public static void register(ElarionChatChannel channel, BiFunction<ServerPlayerEntity, String, Boolean> route) {
        register(channel, route, player -> true);
    }
    public static void register(ElarionChatChannel channel, BiFunction<ServerPlayerEntity, String, Boolean> route,
                                Predicate<ServerPlayerEntity> eligibility) {
        if (channel == null || route == null) throw new IllegalArgumentException("Chat channel route is required.");
        if (ROUTES.putIfAbsent(channel, route) != null) throw new IllegalStateException("Chat channel already registered: " + channel);
        ELIGIBILITY.put(channel, eligibility == null ? player -> true : eligibility);
    }
    public static boolean route(ElarionChatChannel channel, ServerPlayerEntity sender, String message) {
        BiFunction<ServerPlayerEntity, String, Boolean> route = ROUTES.get(channel);
        return route != null && route.apply(sender, message);
    }
    public static boolean registered(ElarionChatChannel channel) { return channel != null && ROUTES.containsKey(channel); }
    public static boolean eligible(ElarionChatChannel channel, ServerPlayerEntity player) {
        return registered(channel) && ELIGIBILITY.getOrDefault(channel, ignored -> false).test(player);
    }
    public static java.util.List<ElarionChatChannel> available(ElarionApi api, ServerPlayerEntity player) {
        java.util.List<ElarionChatChannel> channels = new java.util.ArrayList<>();
        channels.add(ElarionChatChannel.LOCAL);
        if (api != null && player != null && !api.citizens().getOrCreate(player).realmId().isBlank()) {
            channels.add(ElarionChatChannel.REALM);
            channels.add(ElarionChatChannel.ALLIANCE);
        }
        if (eligible(ElarionChatChannel.GUILD, player)) channels.add(ElarionChatChannel.GUILD);
        channels.add(ElarionChatChannel.PRIVATE);
        return java.util.List.copyOf(channels);
    }
}
