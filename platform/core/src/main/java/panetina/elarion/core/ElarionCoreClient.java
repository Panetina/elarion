package panetina.elarion.core;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import panetina.elarion.core.client.ClientIdentityCache;
import panetina.elarion.core.client.ElarionNotificationHud;
import panetina.elarion.core.network.IdentitySyncRequestPayload;
import panetina.elarion.core.network.IdentitySyncPayload;

public final class ElarionCoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ElarionNotificationHud.initialize();
        ClientPlayNetworking.registerGlobalReceiver(IdentitySyncPayload.ID, (payload, context) ->
                context.client().execute(() -> ClientIdentityCache.update(payload)));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientIdentityCache.clear();
            ClientPlayNetworking.send(IdentitySyncRequestPayload.INSTANCE);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientIdentityCache.clear());
    }
}
