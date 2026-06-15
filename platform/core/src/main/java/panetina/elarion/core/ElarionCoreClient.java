package panetina.elarion.core;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import panetina.elarion.core.client.ClientIdentityCache;
import panetina.elarion.core.client.ElarionNotificationHud;
import panetina.elarion.core.network.IdentitySyncRequestPayload;
import panetina.elarion.core.network.IdentitySyncPayload;
import panetina.elarion.core.network.NotificationSnapshotPayload;
import panetina.elarion.core.network.UiThemeSyncPayload;
import panetina.elarion.core.model.ElarionNotificationSnapshot;
import panetina.elarion.core.client.ui.ElarionUiThemes;

public final class ElarionCoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ElarionNotificationHud.initialize();
        ClientPlayNetworking.registerGlobalReceiver(IdentitySyncPayload.ID, (payload, context) ->
                context.client().execute(() -> ClientIdentityCache.update(payload)));
        ClientPlayNetworking.registerGlobalReceiver(UiThemeSyncPayload.ID, (payload, context) ->
                context.client().execute(() -> ElarionUiThemes.update(payload.theme())));
        ClientPlayNetworking.registerGlobalReceiver(NotificationSnapshotPayload.ID, (payload, context) ->
                context.client().execute(() -> ElarionNotificationHud.update(payload.snapshot())));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientIdentityCache.clear();
            ElarionUiThemes.clear();
            ElarionNotificationHud.update(ElarionNotificationSnapshot.EMPTY);
            ClientPlayNetworking.send(IdentitySyncRequestPayload.INSTANCE);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientIdentityCache.clear();
            ElarionUiThemes.clear();
            ElarionNotificationHud.update(ElarionNotificationSnapshot.EMPTY);
        });
    }
}
