package panetina.elarion.addons.government.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.gui.screen.Screen;
import panetina.elarion.addons.government.network.GovernmentUiOpenPayload;
import panetina.elarion.addons.government.network.GovernmentUiFeedbackPayload;
import panetina.elarion.addons.government.network.GovernmentHeraldrySnapshotPayload;
import panetina.elarion.addons.government.network.GovernmentTaxPolicySnapshotPayload;
import panetina.elarion.addons.government.client.seat.SeatOfRuleScreen;
import panetina.elarion.core.client.ElarionHeraldryClientRegistry;

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
        ClientPlayNetworking.registerGlobalReceiver(GovernmentHeraldrySnapshotPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    ElarionHeraldryClientRegistry.putRealm(payload.realmId(), payload.revision(), payload.pixels());
                    if (context.client().currentScreen instanceof SeatOfRuleScreen seat) seat.loadHeraldry(payload.pixels());
                }));
        ClientPlayNetworking.registerGlobalReceiver(GovernmentTaxPolicySnapshotPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    GovernmentTaxPolicyClientState.put(payload);
                    if (context.client().currentScreen instanceof SeatOfRuleScreen seat) {
                        seat.loadTaxPolicy(payload);
                    }
                }));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                GovernmentTaxPolicyClientState.clear());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                GovernmentTaxPolicyClientState.clear());
    }

    private static Screen screen(GovernmentUiOpenPayload payload) {
        String screenType = payload.screenType() == null ? "" : payload.screenType();
        if ("seat_of_rule".equals(screenType) || screenType.startsWith("seat_module_")) {
            return new SeatOfRuleScreen(payload);
        }
        return new CivicForumScreen(payload);
    }
}
