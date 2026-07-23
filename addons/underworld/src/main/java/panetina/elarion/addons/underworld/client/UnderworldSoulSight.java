package panetina.elarion.addons.underworld.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;

import java.util.UUID;

public final class UnderworldSoulSight {
    public enum PlayerAppearance { NORMAL, SHADOW, BANISHED }
    static final String UNDERWORLD_ID = "elarion:underworld";

    private UnderworldSoulSight() {
    }

    public static boolean active() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.world != null
                && isActive(UnderworldClientStatus.current().active(),
                client.world.getRegistryKey().getValue().toString());
    }

    public static boolean shouldRenderShadow(AbstractClientPlayerEntity player) {
        return appearance(player) == PlayerAppearance.SHADOW;
    }

    public static PlayerAppearance appearance(AbstractClientPlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (player == null || client.player == null || !isOtherPlayer(client.player.getUuid(), player.getUuid())) {
            return PlayerAppearance.NORMAL;
        }
        if (UnderworldBanishmentStatus.isBanished(player.getUuid())) return PlayerAppearance.BANISHED;
        return active() ? PlayerAppearance.SHADOW : PlayerAppearance.NORMAL;
    }

    public static boolean shouldHideName(AbstractClientPlayerEntity player) {
        return active() && appearance(player) != PlayerAppearance.NORMAL;
    }

    static boolean isActive(boolean dead, String worldId) {
        return dead && UNDERWORLD_ID.equals(worldId);
    }

    static boolean isOtherPlayer(UUID viewer, UUID target) {
        return viewer != null && target != null && !viewer.equals(target);
    }

    static PlayerAppearance appearance(boolean active, boolean banished, boolean otherPlayer) {
        if (!otherPlayer) return PlayerAppearance.NORMAL;
        if (banished) return PlayerAppearance.BANISHED;
        return active ? PlayerAppearance.SHADOW : PlayerAppearance.NORMAL;
    }
}
