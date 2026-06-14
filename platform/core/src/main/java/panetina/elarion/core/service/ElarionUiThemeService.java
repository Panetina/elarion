package panetina.elarion.core.service;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.model.ElarionUiTheme;
import panetina.elarion.core.network.UiThemeSyncPayload;

public final class ElarionUiThemeService {
    private final CoreConfigManager config;

    public ElarionUiThemeService(CoreConfigManager config) {
        this.config = config;
    }

    public ElarionUiTheme current() {
        return config.uiTheme();
    }

    public void sync(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new UiThemeSyncPayload(current()));
    }

    public void syncAll(MinecraftServer server) {
        server.getPlayerManager().getPlayerList().forEach(this::sync);
    }
}
