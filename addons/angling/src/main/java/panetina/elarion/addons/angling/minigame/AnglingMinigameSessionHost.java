package panetina.elarion.addons.angling.minigame;

import panetina.elarion.addons.angling.network.AnglingMinigameInputPayload;

import java.util.UUID;

/** Implemented by the live bobber entity; avoids a second global session registry. */
public interface AnglingMinigameSessionHost {
    UUID anglingOwnerId();

    AnglingMinigameInputGate.Result acceptAnglingInput(
            UUID senderId, AnglingMinigameInputPayload payload, long serverTick);

    AnglingServerMinigameSnapshot anglingMinigameSnapshot();
}
