package panetina.elarion.core.service;

import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.ChronicleRenderContext;
import panetina.elarion.core.model.ChronicleRenderer;
import panetina.elarion.core.model.PublicHistoryEntry;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ChronicleRendererRegistry {
    public static final String VARIANT_METADATA_KEY = "chronicle.variant";

    private final List<ChronicleRenderer> renderers = new CopyOnWriteArrayList<>();

    public void register(ChronicleRenderer renderer) {
        if (renderer == null) throw new IllegalArgumentException("Chronicle renderer is required.");
        renderers.add(renderer);
    }

    public ChronicleProjection project(PublicHistoryEntry entry, ChronicleRenderContext context) {
        ChronicleRenderContext safeContext = context == null ? ChronicleRenderContext.EMPTY : context;
        if (entry != null) {
            for (ChronicleRenderer renderer : renderers) {
                if (!renderer.supports(entry)) continue;
                ChronicleProjection projection = renderer.render(entry, safeContext);
                if (projection != null) return projection;
            }
        }
        return fallback(entry);
    }

    public static String selectedVariantId(PublicHistoryEntry entry, String family) {
        String persisted = entry == null ? "" : entry.metadata().getOrDefault(VARIANT_METADATA_KEY, "").trim();
        if (!persisted.isBlank()) return persisted;
        String cleanFamily = family == null || family.isBlank() ? "core.default" : family.trim();
        return cleanFamily + ".default";
    }

    private static ChronicleProjection fallback(PublicHistoryEntry entry) {
        if (entry == null) {
            return new ChronicleProjection("Chronicle Event", "", "Chronicle", "Chronicle record", "core.default");
        }
        return new ChronicleProjection(
                titleCase(entry.type()),
                entry.text(),
                titleCase(entry.category()),
                "Chronicle record",
                selectedVariantId(entry, entry.category() + "." + entry.type()));
    }

    private static String titleCase(String value) {
        if (value == null || value.isBlank()) return "Chronicle";
        String[] parts = value.replace('_', '-').split("-");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (!builder.isEmpty()) builder.append(' ');
            String lower = part.toLowerCase(Locale.ROOT);
            builder.append(Character.toUpperCase(lower.charAt(0))).append(lower.substring(1));
        }
        return builder.isEmpty() ? "Chronicle" : builder.toString();
    }
}
