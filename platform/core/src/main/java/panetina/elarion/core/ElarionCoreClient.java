package panetina.elarion.core;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.core.client.ClientIdentityCache;
import panetina.elarion.core.client.ElarionAdminPanelScreen;
import panetina.elarion.core.client.ElarionCollectionScreen;
import panetina.elarion.core.client.ElarionConfigEditClientState;
import panetina.elarion.core.client.ElarionNotificationHud;
import panetina.elarion.core.client.CharacterCreationFlow;
import panetina.elarion.core.client.CharacterRealmAssignmentScreen;
import panetina.elarion.core.network.CollectionOpenPayload;
import panetina.elarion.core.network.CollectionOpenRequestPayload;
import panetina.elarion.core.network.AdminPanelOpenPayload;
import panetina.elarion.core.network.IdentitySyncRequestPayload;
import panetina.elarion.core.network.IdentitySyncPayload;
import panetina.elarion.core.network.NotificationSnapshotPayload;
import panetina.elarion.core.network.UiThemeSyncPayload;
import panetina.elarion.core.network.CharacterCreationRequirementPayload;
import panetina.elarion.core.network.CharacterRealmAssignmentPayload;
import panetina.elarion.core.network.CharacterCreationStatusRequestPayload;
import panetina.elarion.core.network.ElarionConfigEditOpenPayload;
import panetina.elarion.core.network.ElarionConfigEditResultPayload;
import panetina.elarion.core.model.ElarionNotificationSnapshot;
import panetina.elarion.core.client.ui.ElarionUiThemes;

public final class ElarionCoreClient implements ClientModInitializer {
    private static KeyBinding collectionKey;

    @Override
    public void onInitializeClient() {
        ElarionNotificationHud.initialize();
        CharacterCreationFlow.initialize();
        collectionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elarion_core.collection",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "category.elarion_core.ui"));
        ClientPlayNetworking.registerGlobalReceiver(IdentitySyncPayload.ID, (payload, context) ->
                context.client().execute(() -> ClientIdentityCache.update(payload)));
        ClientPlayNetworking.registerGlobalReceiver(UiThemeSyncPayload.ID, (payload, context) ->
                context.client().execute(() -> ElarionUiThemes.update(payload.theme())));
        ClientPlayNetworking.registerGlobalReceiver(NotificationSnapshotPayload.ID, (payload, context) ->
                context.client().execute(() -> ElarionNotificationHud.update(payload.snapshot())));
        ClientPlayNetworking.registerGlobalReceiver(CharacterCreationRequirementPayload.ID, (payload, context) ->
                context.client().execute(() -> CharacterCreationFlow.update(payload)));
        ClientPlayNetworking.registerGlobalReceiver(CharacterRealmAssignmentPayload.ID, (payload, context) ->
                context.client().execute(() ->
                        context.client().setScreen(new CharacterRealmAssignmentScreen(payload))));
        ClientPlayNetworking.registerGlobalReceiver(CollectionOpenPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof ElarionCollectionScreen screen) {
                        screen.update(payload.snapshot());
                    } else {
                        context.client().setScreen(new ElarionCollectionScreen(payload.snapshot()));
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(AdminPanelOpenPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof ElarionAdminPanelScreen screen) {
                        screen.update(payload.snapshot());
                    } else {
                        context.client().setScreen(new ElarionAdminPanelScreen(payload.snapshot()));
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(ElarionConfigEditResultPayload.ID, (payload, context) ->
                context.client().execute(() -> ElarionConfigEditClientState.update(payload)));
        ClientPlayNetworking.registerGlobalReceiver(ElarionConfigEditOpenPayload.ID, (payload, context) ->
                context.client().execute(() -> ElarionConfigEditClientState.open(payload.control())));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || collectionKey == null) return;
            while (collectionKey.wasPressed()) {
                if (client.currentScreen instanceof ElarionCollectionScreen) {
                    client.setScreen(null);
                } else {
                    ClientPlayNetworking.send(CollectionOpenRequestPayload.INSTANCE);
                }
            }
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientIdentityCache.clear();
            ElarionUiThemes.clear();
            ElarionNotificationHud.update(ElarionNotificationSnapshot.EMPTY);
            CharacterCreationFlow.clear();
            ElarionConfigEditClientState.clear();
            ClientPlayNetworking.send(IdentitySyncRequestPayload.INSTANCE);
            ClientPlayNetworking.send(CharacterCreationStatusRequestPayload.INSTANCE);
            CharacterCreationFlow.requestStatusWhenReady();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientIdentityCache.clear();
            ElarionUiThemes.clear();
            ElarionNotificationHud.update(ElarionNotificationSnapshot.EMPTY);
            CharacterCreationFlow.clear();
            ElarionConfigEditClientState.clear();
        });
    }
}
