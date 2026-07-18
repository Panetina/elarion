package panetina.elarion.core.placeholder;

public record PlaceholderResolutionLimits(int maxPlaceholders, int maxOutputLength, int maxDepth,
                                          int maxDiagnostics) {
    public static final PlaceholderResolutionLimits DEFAULTS = new PlaceholderResolutionLimits(64, 8192, 4, 16);

    public PlaceholderResolutionLimits {
        maxPlaceholders = bounded(maxPlaceholders, 1, 1024);
        maxOutputLength = bounded(maxOutputLength, 64, 65536);
        maxDepth = bounded(maxDepth, 1, 16);
        maxDiagnostics = bounded(maxDiagnostics, 0, 128);
    }

    private static int bounded(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
