package panetina.elarion.addons.government.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import panetina.elarion.addons.government.network.GovernmentUiOpenPayload;

public final class ElarionGovernmentClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(GovernmentUiOpenPayload.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(new GovernmentStatusScreen(payload))));
    }
}
