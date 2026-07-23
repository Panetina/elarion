package panetina.elarion.addons.angling.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import panetina.elarion.addons.angling.minigame.AnglingMinigameInputGate;
import panetina.elarion.addons.angling.minigame.AnglingMinigameSessionHost;

/** Payload registration only; live receivers attach with the bobber-owned session runtime. */
public final class AnglingNetworking {
    private static boolean registered;

    private AnglingNetworking() {
    }

    public static synchronized void registerPayloadTypes() {
        if (registered) return;
        PayloadTypeRegistry.playC2S().register(AnglingMinigameInputPayload.ID, AnglingMinigameInputPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AnglingMinigameStartPayload.ID, AnglingMinigameStartPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AnglingMinigameStatePayload.ID, AnglingMinigameStatePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(AnglingMinigameInputPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    var player = context.player();
                    var entity = player.getWorld().getEntityById(payload.bobberEntityId());
                    if (!(entity instanceof AnglingMinigameSessionHost host)
                            || !player.getUuid().equals(host.anglingOwnerId())) {
                        return;
                    }
                    var result = host.acceptAnglingInput(
                            player.getUuid(), payload, context.server().getTicks());
                    if (result == AnglingMinigameInputGate.Result.ACCEPTED) {
                        ServerPlayNetworking.send(player,
                                new AnglingMinigameStatePayload(host.anglingMinigameSnapshot()));
                    }
                }));
        registered = true;
    }
}
