package panetina.elarion.addons.government.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import panetina.elarion.addons.government.network.GovernmentUiOpenPayload;
import panetina.elarion.addons.government.network.GovernmentUiFeedbackPayload;
import panetina.elarion.addons.government.client.seat.SeatOfRuleScreen;

public final class ElarionGovernmentClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(GovernmentUiOpenPayload.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(screen(payload))));
        ClientPlayNetworking.registerGlobalReceiver(GovernmentUiFeedbackPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof CivicForumScreen civic) {
                        civic.setFeedbackMessage(payload.message());
                    }
                }));
    }

    private static Screen screen(GovernmentUiOpenPayload payload) {
        String screenType = payload.screenType() == null ? "" : payload.screenType();
        if ("seat_of_rule".equals(screenType) || screenType.startsWith("seat_module_")) {
            return new SeatOfRuleScreen(payload);
        }
        return new CivicForumScreen(payload);
    }
}
