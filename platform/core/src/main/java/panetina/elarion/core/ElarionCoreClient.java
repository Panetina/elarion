package panetina.elarion.core;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.core.client.CitizenProfileClientState;
import panetina.elarion.core.client.ClientIdentityCache;
import panetina.elarion.core.client.ElarionAdminPanelScreen;
import panetina.elarion.core.client.ElarionCollectionScreen;
import panetina.elarion.core.client.ElarionConfigEditClientState;
import panetina.elarion.core.client.ElarionNotificationHud;
import panetina.elarion.core.client.ElarionUiComponentGalleryScreen;
import panetina.elarion.core.client.CharacterCreationFlow;
import panetina.elarion.core.client.CharacterRealmAssignmentScreen;
import panetina.elarion.core.network.CollectionOpenPayload;
import panetina.elarion.core.network.CollectionOpenRequestPayload;
import panetina.elarion.core.network.CitizenProfileSnapshotPayload;
import panetina.elarion.core.network.CitizenProfileOpenPayload;
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
import panetina.elarion.core.network.LauncherPassageTicketPayload;
import panetina.elarion.core.model.ElarionNotificationSnapshot;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.LauncherPassageTicketStore;

public final class ElarionCoreClient implements ClientModInitializer {
    private static KeyBinding collectionKey;
    private static boolean vanillaSaveHotbarBindingChecked;

    @Override
    public void onInitializeClient() {
        ElarionNotificationHud.initialize();
        CharacterCreationFlow.initialize();
        collectionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elarion_core.collection",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "category.elarion_core.ui"));
        registerCharacterMenuCommand();
        registerUiGalleryCommandAlias();
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
        ClientPlayNetworking.registerGlobalReceiver(CitizenProfileSnapshotPayload.ID, (payload, context) ->
                context.client().execute(() -> CitizenProfileClientState.update(payload.snapshot())));
        ClientPlayNetworking.registerGlobalReceiver(CitizenProfileOpenPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    CitizenProfileClientState.update(payload.profile());
                    context.client().setScreen(new ElarionCollectionScreen(payload.collection(), true));
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
        ClientPlayNetworking.registerGlobalReceiver(LauncherPassageTicketPayload.ID, (payload, context) ->
                context.client().execute(() -> LauncherPassageTicketStore.save(payload.uuid(), payload.ticket())));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!vanillaSaveHotbarBindingChecked && client.options != null) {
                vanillaSaveHotbarBindingChecked = unbindVanillaSaveHotbarActivator(client);
            }
            if (client.player == null || collectionKey == null) return;
            while (collectionKey.wasPressed()) {
                toggleCollection(client);
            }
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientIdentityCache.clear();
            ElarionUiThemes.clear();
            ElarionNotificationHud.update(ElarionNotificationSnapshot.EMPTY);
            CharacterCreationFlow.clear();
            CitizenProfileClientState.clear();
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
            CitizenProfileClientState.clear();
            ElarionConfigEditClientState.clear();
        });
    }

    private static void registerCharacterMenuCommand() {
        ClientSendMessageEvents.ALLOW_COMMAND.register(command -> {
            if (!opensCharacterMenuCommand(command)) return true;
            toggleCollection(MinecraftClient.getInstance());
            return false;
        });
    }

    private static void registerUiGalleryCommandAlias() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;
        ClientSendMessageEvents.ALLOW_COMMAND.register(command -> {
            if (!"elarion-ui-gallery".equals(rootCommand(command))) return true;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) client.setScreen(new ElarionUiComponentGalleryScreen());
            return false;
        });
    }

    private static boolean unbindVanillaSaveHotbarActivator(MinecraftClient client) {
        if (client == null || client.options == null) return false;
        KeyBinding key = client.options.saveToolbarActivatorKey;
        if (key == null) return true;
        if (!"key.keyboard.c".equals(key.getBoundKeyTranslationKey())) return true;
        key.setBoundKey(InputUtil.UNKNOWN_KEY);
        KeyBinding.updateKeysByCode();
        client.options.write();
        return true;
    }

    private static void toggleCollection(MinecraftClient client) {
        if (client == null || client.player == null) return;
        if (client.currentScreen instanceof ElarionCollectionScreen) {
            client.setScreen(null);
            return;
        }
        ClientPlayNetworking.send(CollectionOpenRequestPayload.INSTANCE);
    }

    private static String rootCommand(String command) {
        if (command == null) return "";
        String trimmed = command.trim().toLowerCase(java.util.Locale.ROOT);
        if (trimmed.startsWith("/")) trimmed = trimmed.substring(1).trim();
        int space = trimmed.indexOf(' ');
        return space < 0 ? trimmed : trimmed.substring(0, space);
    }

    static boolean opensCharacterMenuCommand(String command) {
        return "charactermenu".equals(rootCommand(command));
    }
}
