package panetina.elarion.addons.npcs.service;

public record NpcReputationTier(String id, String label, int color, int progress, int progressMaximum) {
    public static final int BAND_SIZE = 120;

    public NpcReputationTier {
        progressMaximum = Math.max(1, progressMaximum);
        progress = Math.max(0, Math.min(progressMaximum, progress));
    }

    public static String personalLabel(int score) {
        if (score <= -60) return "Hostile";
        if (score < 0) return "Wary";
        if (score < 20) return "Neutral";
        if (score < 50) return "Familiar";
        if (score < 80) return "Trusted";
        return "Close";
    }

    public static NpcReputationTier forScore(long score) {
        if (score < -BAND_SIZE) return tier("hated", "Hated", 0xFFB43A3A, score + BAND_SIZE * 2L);
        if (score < 0) return tier("disliked", "Disliked", 0xFFD46A45, score + BAND_SIZE);
        if (score < BAND_SIZE) return tier("neutral", "Neutral", 0xFFD0AA55, score);
        if (score < BAND_SIZE * 2L) return tier("liked", "Liked", 0xFF65B96E, score - BAND_SIZE);
        return tier("loved", "Loved", 0xFFE48BB7, score - BAND_SIZE * 2L);
    }

    public static long minimumScore(String standingId) {
        if (standingId == null) return Long.MAX_VALUE;
        return switch (standingId.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "hated" -> Long.MIN_VALUE;
            case "disliked" -> -BAND_SIZE;
            case "neutral" -> 0L;
            case "liked" -> BAND_SIZE;
            case "loved" -> BAND_SIZE * 2L;
            default -> Long.MAX_VALUE;
        };
    }

    private static NpcReputationTier tier(String id, String label, int color, long progress) {
        return new NpcReputationTier(id, label, color,
                (int) Math.max(0L, Math.min(BAND_SIZE, progress)), BAND_SIZE);
    }
}
