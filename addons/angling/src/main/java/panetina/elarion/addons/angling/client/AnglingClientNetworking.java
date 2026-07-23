package panetina.elarion.addons.angling.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import panetina.elarion.addons.angling.client.screen.AnglingMinigameScreen;
import panetina.elarion.addons.angling.network.AnglingMinigameStartPayload;
import panetina.elarion.addons.angling.network.AnglingMinigameStatePayload;

/** Client receiver boundary; session identity is checked again by the active screen. */
public final class AnglingClientNetworking {
    private AnglingClientNetworking() {
    }

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(AnglingMinigameStartPayload.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(new AnglingMinigameScreen(payload))));
        ClientPlayNetworking.registerGlobalReceiver(AnglingMinigameStatePayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof AnglingMinigameScreen screen) {
                        screen.accept(payload.snapshot());
                    }
                }));
    }
}
