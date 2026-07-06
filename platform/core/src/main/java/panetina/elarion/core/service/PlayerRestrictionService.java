package panetina.elarion.core.service;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PlayerRestrictionService {
    public static final String CHAT = "chat";
    public static final String PRIVATE_MESSAGE = "private_message";
    public static final String GROUP_CHAT = "group_chat";
    public static final String PORTAL_TRAVEL = "portal_travel";
    public static final String TELEPORT = "teleport";
    public static final String NAMEPLATE = "nameplate";

    private final List<PlayerRestrictionProvider> providers = new CopyOnWriteArrayList<>();

    public void register(PlayerRestrictionProvider provider) {
        if (provider != null) providers.add(provider);
    }

    public Optional<PlayerRestriction> restriction(ServerPlayerEntity player, String action) {
        if (player == null || action == null || action.isBlank()) return Optional.empty();
        for (PlayerRestrictionProvider provider : providers) {
            try {
                Optional<PlayerRestriction> result = provider.restriction(player, action);
                if (result.isPresent()) return result;
            } catch (RuntimeException ignored) {
                // Restriction providers must not break unrelated gameplay paths.
            }
        }
        return Optional.empty();
    }

    public boolean isRestricted(ServerPlayerEntity player, String action) {
        return restriction(player, action).isPresent();
    }

    public boolean denyWithMessage(ServerPlayerEntity player, String action) {
        Optional<PlayerRestriction> restriction = restriction(player, action);
        if (restriction.isEmpty()) return false;
        String message = restriction.get().message();
        if (message != null && !message.isBlank()) {
            player.sendMessage(Text.literal(message).formatted(Formatting.RED), false);
        }
        return true;
    }

    public record PlayerRestriction(String source, String message) {
    }

    @FunctionalInterface
    public interface PlayerRestrictionProvider {
        Optional<PlayerRestriction> restriction(ServerPlayerEntity player, String action);
    }
}
