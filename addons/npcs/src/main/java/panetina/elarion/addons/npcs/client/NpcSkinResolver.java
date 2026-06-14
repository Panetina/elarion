package panetina.elarion.addons.npcs.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NpcSkinResolver {
    private static final Map<String, SkinTextures> PLAYER_SKINS = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> REQUESTED = new ConcurrentHashMap<>();

    private NpcSkinResolver() {
    }

    public static Optional<SkinTextures> playerSkin(String playerName) {
        if (playerName == null || playerName.isBlank()) return Optional.empty();
        String key = playerName.toLowerCase(Locale.ROOT);
        SkinTextures cached = PLAYER_SKINS.get(key);
        if (cached != null) return Optional.of(cached);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
                if (entry.getProfile().getName().equalsIgnoreCase(playerName)) {
                    PLAYER_SKINS.put(key, entry.getSkinTextures());
                    return Optional.of(entry.getSkinTextures());
                }
            }
        }

        if (REQUESTED.putIfAbsent(key, true) == null) {
            GameProfile profile = new GameProfile(offlineUuid(playerName), playerName);
            client.getSkinProvider().fetchSkinTextures(profile)
                    .thenAccept(skin -> PLAYER_SKINS.put(key, skin));
        }
        return Optional.of(DefaultSkinHelper.getSkinTextures(offlineUuid(playerName)));
    }

    public static Optional<Identifier> texture(String texture) {
        if (texture == null || texture.isBlank()) return Optional.empty();
        try {
            return Optional.of(Identifier.of(texture));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static UUID offlineUuid(String playerName) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8));
    }
}
