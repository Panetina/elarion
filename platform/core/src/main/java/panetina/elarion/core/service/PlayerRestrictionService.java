package panetina.elarion.core.service;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PlayerRestrictionService {
    public static final String CHAT = "chat";
    public static final String PRIVATE_MESSAGE = "private_message";
    public static final String GROUP_CHAT = "group_chat";
    public static final String PORTAL_TRAVEL = "portal_travel";
    public static final String TELEPORT = "teleport";
    public static final String NAMEPLATE = "nameplate";
    public static final String BREAK_BLOCK = "break_block";
    public static final String ATTACK_BLOCK = "attack_block";
    public static final String ATTACK_ENTITY = "attack_entity";
    public static final String INTERACT_BLOCK = "interact_block";
    public static final String INTERACT_ENTITY = "interact_entity";
    public static final String USE_ITEM = "use_item";
    /** Consulted by the future admission queue before admitting a queued account. */
    public static final String QUEUED_ADMISSION = "queued_admission";

    private final List<PlayerRestrictionProvider> providers = new CopyOnWriteArrayList<>();
    private final List<AccountRestrictionProvider> accountProviders = new CopyOnWriteArrayList<>();

    public void register(PlayerRestrictionProvider provider) {
        if (provider != null) providers.add(provider);
    }

    public void registerAccountProvider(AccountRestrictionProvider provider) {
        if (provider != null) accountProviders.add(provider);
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
        return restriction(player.getUuid(), action);
    }

    /**
     * UUID-only restriction lookup for admission paths that run before a player entity is usable.
     */
    public Optional<PlayerRestriction> restriction(UUID playerId, String action) {
        if (playerId == null || action == null || action.isBlank()) return Optional.empty();
        for (AccountRestrictionProvider provider : accountProviders) {
            try {
                Optional<PlayerRestriction> result = provider.restriction(playerId, action);
                if (result.isPresent()) return result;
            } catch (RuntimeException ignored) {
                // Admission restriction providers must fail open instead of breaking login.
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

    @FunctionalInterface
    public interface AccountRestrictionProvider {
        Optional<PlayerRestriction> restriction(UUID playerId, String action);
    }
}
