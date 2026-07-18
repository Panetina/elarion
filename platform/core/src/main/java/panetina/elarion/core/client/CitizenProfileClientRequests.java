package panetina.elarion.core.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import panetina.elarion.core.network.CitizenProfileRequestPayload;

import java.util.UUID;

public final class CitizenProfileClientRequests {
    private CitizenProfileClientRequests() {
    }

    public static void open(UUID serverAuthoredTargetId) {
        if (serverAuthoredTargetId == null) return;
        ClientPlayNetworking.send(new CitizenProfileRequestPayload(serverAuthoredTargetId, ""));
    }
}
