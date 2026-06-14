package panetina.elarion.addons.npcs.client.ui;

public final class ElarionTypingText {
    private final long startedAtMillis;
    private final boolean enabled;
    private final int charactersPerSecond;
    private boolean skipped;

    public ElarionTypingText(boolean enabled, int charactersPerSecond) {
        this.startedAtMillis = System.currentTimeMillis();
        this.enabled = enabled;
        this.charactersPerSecond = Math.max(1, charactersPerSecond);
    }

    public String visible(String text) {
        if (!enabled || skipped || text == null || text.isBlank()) return text == null ? "" : text;
        long elapsed = Math.max(0L, System.currentTimeMillis() - startedAtMillis);
        int visible = (int) Math.min(text.codePointCount(0, text.length()),
                (elapsed * charactersPerSecond) / 1000L);
        return firstCodePoints(text, visible);
    }

    public boolean completeIfTyping(String... texts) {
        if (!enabled || skipped || complete(texts)) return false;
        skipped = true;
        return true;
    }

    public boolean complete(String... texts) {
        if (!enabled || skipped) return true;
        int total = 0;
        for (String text : texts) {
            if (text != null) total = Math.max(total, text.codePointCount(0, text.length()));
        }
        long elapsed = Math.max(0L, System.currentTimeMillis() - startedAtMillis);
        return (elapsed * charactersPerSecond) / 1000L >= total;
    }

    public int typedIntervalIndex(int intervalCharacters) {
        int interval = Math.max(1, intervalCharacters);
        long elapsed = Math.max(0L, System.currentTimeMillis() - startedAtMillis);
        return (int) ((elapsed * charactersPerSecond) / 1000L / interval);
    }

    private static String firstCodePoints(String text, int count) {
        if (count <= 0) return "";
        int total = text.codePointCount(0, text.length());
        if (count >= total) return text;
        return text.substring(0, text.offsetByCodePoints(0, count));
    }
}
