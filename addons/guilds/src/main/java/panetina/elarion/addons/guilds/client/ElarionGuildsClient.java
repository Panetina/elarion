package panetina.elarion.addons.guilds.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import panetina.elarion.addons.guilds.network.GuildScreenOpenPayload;
import panetina.elarion.addons.guilds.network.GuildRegistrarOpenPayload;
import panetina.elarion.addons.guilds.network.GuildScreenClosePayload;
import panetina.elarion.addons.guilds.network.GuildUiFeedbackPayload;

/** Client entry point for the server-authoritative Guild management surface. */
public final class ElarionGuildsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(GuildScreenOpenPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof GuildScreen screen
                            && screen.belongsTo(payload)) {
                        screen.updatePayload(payload);
                    } else {
                        context.client().setScreen(new GuildScreen(payload));
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(GuildRegistrarOpenPayload.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(new GuildCreateScreen(payload))));
        ClientPlayNetworking.registerGlobalReceiver(GuildScreenClosePayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof GuildScreen) {
                        context.client().setScreen(null);
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(GuildUiFeedbackPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof GuildCreateScreen registrar) {
                        registrar.feedback(payload);
                    } else if (context.client().currentScreen instanceof GuildScreen guild) {
                        guild.feedback(payload);
                    }
                }));
    }
}
