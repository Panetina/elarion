package panetina.elarion.addons.government.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import panetina.elarion.addons.government.client.foundation.FoundingElectionScreen;
import panetina.elarion.addons.government.client.foundation.GovernmentFormVotingScreen;
import panetina.elarion.addons.government.client.foundation.RealmNamingScreen;
import panetina.elarion.addons.government.network.GovernmentUiOpenPayload;
import panetina.elarion.addons.government.client.seat.SeatOfRuleScreen;

public final class ElarionGovernmentClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(GovernmentUiOpenPayload.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(screen(payload))));
    }

    private static Screen screen(GovernmentUiOpenPayload payload) {
        return switch (payload.screenType()) {
            case "civic_name" -> new RealmNamingScreen(payload);
            case "civic_form" -> new GovernmentFormVotingScreen(payload);
            case "civic_election" -> new FoundingElectionScreen(payload);
            case "seat_of_rule" -> new SeatOfRuleScreen(payload);
            default -> new GovernmentStatusScreen(payload);
        };
    }
}
