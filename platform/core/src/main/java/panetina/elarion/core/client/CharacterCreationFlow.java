package panetina.elarion.core.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import panetina.elarion.core.network.CharacterCreationRequirementPayload;
import panetina.elarion.core.network.CharacterCreationStatusRequestPayload;

public final class CharacterCreationFlow {
    private static final int BLOCKED_RETRY_TICKS = 200;
    private static CharacterCreationRequirementPayload pending;
    private static int blockedTicks;
    private static boolean statusRequestWhenReady;

    private CharacterCreationFlow() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(CharacterCreationFlow::tick);
    }

    public static void update(CharacterCreationRequirementPayload payload) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (payload == null || !payload.required()) {
            pending = null;
            blockedTicks = 0;
            if (client.currentScreen instanceof CharacterCreationScreen) client.setScreen(null);
            return;
        }
        pending = payload;
        if (client.currentScreen instanceof CharacterCreationScreen screen) {
            screen.updateRequirement(payload);
        } else {
            tryOpen(client);
        }
    }

    public static void clear() {
        pending = null;
        blockedTicks = 0;
        statusRequestWhenReady = false;
    }

    public static void requestStatusWhenReady() {
        statusRequestWhenReady = true;
    }

    public static CharacterCreationRequirementPayload pending() {
        return pending;
    }

    private static void tick(MinecraftClient client) {
        if (statusRequestWhenReady && client.player != null && client.world != null) {
            statusRequestWhenReady = false;
            ClientPlayNetworking.send(CharacterCreationStatusRequestPayload.INSTANCE);
        }
        if (pending == null || client.player == null || client.world == null) return;
        if (client.currentScreen instanceof CharacterCreationScreen) return;
        if (client.currentScreen != null) {
            if (blockedTicks < BLOCKED_RETRY_TICKS) blockedTicks++;
            return;
        }
        tryOpen(client);
    }

    private static void tryOpen(MinecraftClient client) {
        if (pending == null || client.player == null || client.world == null) return;
        if (client.currentScreen != null) return;
        blockedTicks = 0;
        client.setScreen(new CharacterCreationScreen(pending));
    }
}
