package panetina.elarion.addons.offerings.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import panetina.elarion.addons.offerings.network.ShrineUiOpenPayload;

public final class ElarionOfferingsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ShrineUiOpenPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof ShrineOfFoundationScreen screen
                            && screen.belongsTo(payload)) {
                        screen.applySnapshot(payload);
                    } else {
                        context.client().setScreen(new ShrineOfFoundationScreen(payload));
                    }
                }));
    }
}
