package panetina.elarion.core.client;

import panetina.elarion.core.model.ElarionChatChannel;

public final class ElarionChatChannelClientState {
    private static ElarionChatChannel selected = ElarionChatChannel.LOCAL;
    private static java.util.List<ElarionChatChannel> available = java.util.List.of(ElarionChatChannel.LOCAL);
    private ElarionChatChannelClientState() { }
    public static void reset() { selected = ElarionChatChannel.LOCAL; available = java.util.List.of(ElarionChatChannel.LOCAL); }
    public static java.util.List<ElarionChatChannel> available() { return available; }
    public static void updateAvailable(java.util.List<ElarionChatChannel> channels) { available = channels == null || channels.isEmpty() ? java.util.List.of(ElarionChatChannel.LOCAL) : java.util.List.copyOf(channels); if (!available.contains(selected)) selected = available.getFirst(); }
    public static ElarionChatChannel selected() { return selected; }
    public static void select(ElarionChatChannel channel) {
        selected = channel != null && available.contains(channel) ? channel : available.getFirst();
    }
    public static void cycle(int direction) {
        if (available.size() < 2) return;
        int current = Math.max(0, available.indexOf(selected));
        selected = available.get(Math.floorMod(current + (direction < 0 ? -1 : 1), available.size()));
    }
    public static String label(ElarionChatChannel channel) {
        return switch (channel) {
            case LOCAL -> "Local";
            case REALM -> "Realm";
            case ALLIANCE -> "Alliance";
            case GUILD -> "Guild";
            case PRIVATE -> "PM";
        };
    }
}
