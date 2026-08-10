package panetina.elarion.addons.guilds.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.addons.guilds.network.GuildScreenOpenRequestPayload;
import panetina.elarion.addons.guilds.network.GuildEmptyScreenPayload;
import panetina.elarion.addons.guilds.network.GuildScreenOpenPayload;
import panetina.elarion.addons.guilds.network.GuildRegistrarOpenPayload;
import panetina.elarion.addons.guilds.network.GuildScreenClosePayload;
import panetina.elarion.addons.guilds.network.GuildUiFeedbackPayload;
import panetina.elarion.addons.guilds.network.GuildInvitationPromptPayload;

/** Client entry point for the server-authoritative Guild management surface. */
public final class ElarionGuildsClient implements ClientModInitializer {
    private static KeyBinding guildKey;
    @Override
    public void onInitializeClient() {
        guildKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.elarion_guilds.open",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.elarion_core.ui"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || guildKey == null) return;
            while (guildKey.wasPressed()) {
                if (client.currentScreen instanceof GuildScreen || client.currentScreen instanceof GuildEmptyScreen) client.setScreen(null);
                else if (client.currentScreen == null) ClientPlayNetworking.send(GuildScreenOpenRequestPayload.INSTANCE);
            }
        });
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
        ClientPlayNetworking.registerGlobalReceiver(GuildEmptyScreenPayload.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(new GuildEmptyScreen())));
        ClientPlayNetworking.registerGlobalReceiver(GuildInvitationPromptPayload.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(new GuildInvitationPromptScreen(payload))));
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
