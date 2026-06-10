package panetina.elarion.core.model;

import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public record TitleUnlockRule(
        String id,
        String titleId,
        String trigger,
        String statKey,
        long threshold,
        long amount,
        Set<RegistryMatcher> entities,
        Set<RegistryMatcher> blocks,
        Set<RegistryMatcher> items,
        Set<RegistryMatcher> recipes,
        Set<String> worlds,
        Set<String> dimensions,
        Set<String> biomes,
        Set<String> regions,
        Map<String, String> metadata,
        Continuous continuous
) {
    public TitleUnlockRule {
        id = normalize(id);
        titleId = normalize(titleId);
        trigger = normalize(trigger);
        statKey = normalize(statKey);
        threshold = Math.max(0, threshold);
        amount = Math.max(1, amount);
        entities = copyMatchers(entities);
        blocks = copyMatchers(blocks);
        items = copyMatchers(items);
        recipes = copyMatchers(recipes);
        worlds = copyStrings(worlds);
        dimensions = copyStrings(dimensions);
        biomes = copyStrings(biomes);
        regions = copyStrings(regions);
        metadata = metadata == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(metadata));
    }

    public boolean matches(ProgressionEvent event) {
        if (!trigger.equals(event.type())) return false;
        if (!matchesLocation(worlds, event.worldId())) return false;
        if (!matchesLocation(dimensions, event.dimensionId())) return false;
        if (!matchesLocation(biomes, event.biomeId())) return false;
        if (!matchesRegions(regions, event.metadata().getOrDefault("regions", ""))) return false;
        if (!matchesTarget(entities, event.idFor("entity"), event.tagsFor("entity"))) return false;
        if (!matchesTarget(blocks, event.idFor("block"), event.tagsFor("block"))) return false;
        if (!matchesTarget(items, event.idFor("item"), event.tagsFor("item"))) return false;
        if (!matchesTarget(recipes, event.idFor("recipe"), Set.of())) return false;
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            if (!entry.getValue().equalsIgnoreCase(event.metadata().getOrDefault(entry.getKey(), ""))) return false;
        }
        return true;
    }

    public boolean isStatRule() {
        return "stat-threshold".equals(trigger) && !statKey.isBlank();
    }

    public boolean isContinuousRule() {
        return "continuous".equals(trigger) && continuous != null;
    }

    private static boolean matchesLocation(Set<String> allowed, String actual) {
        return allowed.isEmpty() || allowed.contains(normalize(actual));
    }

    private static boolean matchesRegions(Set<String> allowed, String actual) {
        if (allowed.isEmpty()) return true;
        for (String region : actual.split(",")) {
            if (allowed.contains(normalize(region))) return true;
        }
        return false;
    }

    private static boolean matchesTarget(Set<RegistryMatcher> matchers, Identifier id, Set<Identifier> tags) {
        if (matchers.isEmpty()) return true;
        for (RegistryMatcher matcher : matchers) {
            if (matcher.matches(id, tags)) return true;
        }
        return false;
    }

    private static Set<RegistryMatcher> copyMatchers(Set<RegistryMatcher> value) {
        return value == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(value));
    }

    private static Set<String> copyStrings(Collection<String> value) {
        if (value == null) return Set.of();
        Set<String> result = new LinkedHashSet<>();
        for (String item : value) {
            String normalized = normalize(item);
            if (!normalized.isBlank()) result.add(normalized);
        }
        return Set.copyOf(result);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public record RegistryMatcher(boolean tag, Identifier id) {
        public boolean matches(Identifier actual, Set<Identifier> tags) {
            return tag ? tags.contains(id) : id.equals(actual);
        }

        public static RegistryMatcher parse(String value) {
            String raw = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
            boolean tag = raw.startsWith("#");
            if (tag) raw = raw.substring(1);
            return new RegistryMatcher(tag, Identifier.of(raw));
        }
    }

    public record Continuous(
            long duration,
            String durationUnit,
            long sampleIntervalTicks,
            boolean resetOnFailure,
            Set<String> requiredStatusEffects,
            Set<String> allowedStatusEffects,
            Set<String> requiredMetadata
    ) {
        public Continuous {
            duration = Math.max(1, duration);
            durationUnit = normalize(durationUnit);
            sampleIntervalTicks = Math.max(20, sampleIntervalTicks);
            requiredStatusEffects = copyStrings(requiredStatusEffects);
            allowedStatusEffects = copyStrings(allowedStatusEffects);
            requiredMetadata = copyStrings(requiredMetadata);
        }

        public long requiredTicks() {
            return switch (durationUnit) {
                case "ticks" -> duration;
                case "real_minutes" -> duration * 60L * 20L;
                case "real_days" -> duration * 24L * 60L * 60L * 20L;
                case "minecraft_days" -> duration * 24_000L;
                default -> duration * 24_000L;
            };
        }
    }
}
