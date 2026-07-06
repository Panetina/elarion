package panetina.elarion.addons.underworld.client;

import panetina.elarion.addons.underworld.network.UnderworldStatusSyncPayload;

public final class UnderworldClientStatus {
    private static volatile UnderworldStatusSyncPayload status = UnderworldStatusSyncPayload.clear();

    private UnderworldClientStatus() {
    }

    public static boolean update(UnderworldStatusSyncPayload payload) {
        UnderworldStatusSyncPayload next = payload == null ? UnderworldStatusSyncPayload.clear() : payload;
        UnderworldStatusSyncPayload previous = status;
        status = next;
        return previous.active() != next.active()
                || previous.fractures() != next.fractures()
                || previous.maxFractures() != next.maxFractures()
                || previous.trueDeath() != next.trueDeath();
    }

    public static void clear() {
        status = UnderworldStatusSyncPayload.clear();
    }

    public static UnderworldStatusSyncPayload current() {
        return status;
    }
}
