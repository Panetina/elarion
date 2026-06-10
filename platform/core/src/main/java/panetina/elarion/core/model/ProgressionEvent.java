package panetina.elarion.core.model;

import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public record ProgressionEvent(
        String type,
        UUID actorId,
        String worldId,
        String dimensionId,
        String biomeId,
        Identifier entityId,
        Set<Identifier> entityTags,
        Identifier blockId,
        Set<Identifier> blockTags,
        Identifier itemId,
        Set<Identifier> itemTags,
        Identifier recipeId,
        long amount,
        Map<String, String> metadata
) {
    public ProgressionEvent {
        type = normalize(type);
        worldId = safe(worldId);
        dimensionId = safe(dimensionId);
        biomeId = safe(biomeId);
        entityTags = copyIds(entityTags);
        blockTags = copyIds(blockTags);
        itemTags = copyIds(itemTags);
        amount = Math.max(1, amount);
        metadata = metadata == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(metadata));
    }

    public Set<Identifier> tagsFor(String target) {
        return switch (normalize(target)) {
            case "entity" -> entityTags;
            case "block" -> blockTags;
            case "item" -> itemTags;
            default -> Set.of();
        };
    }

    public Identifier idFor(String target) {
        return switch (normalize(target)) {
            case "entity" -> entityId;
            case "block" -> blockId;
            case "item" -> itemId;
            case "recipe" -> recipeId;
            default -> null;
        };
    }

    public static Builder builder(String type, UUID actorId) {
        return new Builder(type, actorId);
    }

    public static Set<Identifier> tagIds(Iterable<? extends TagKey<?>> tags) {
        Set<Identifier> ids = new LinkedHashSet<>();
        if (tags == null) return ids;
        for (TagKey<?> tag : tags) {
            ids.add(tag.id());
        }
        return ids;
    }

    public static Set<Identifier> tagIds(Stream<? extends TagKey<?>> tags) {
        Set<Identifier> ids = new LinkedHashSet<>();
        if (tags == null) return ids;
        tags.forEach(tag -> ids.add(tag.id()));
        return ids;
    }

    private static Set<Identifier> copyIds(Set<Identifier> value) {
        return value == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(value));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public static final class Builder {
        private final String type;
        private final UUID actorId;
        private String worldId = "";
        private String dimensionId = "";
        private String biomeId = "";
        private Identifier entityId;
        private Set<Identifier> entityTags = Set.of();
        private Identifier blockId;
        private Set<Identifier> blockTags = Set.of();
        private Identifier itemId;
        private Set<Identifier> itemTags = Set.of();
        private Identifier recipeId;
        private long amount = 1;
        private Map<String, String> metadata = new LinkedHashMap<>();

        private Builder(String type, UUID actorId) {
            this.type = type;
            this.actorId = actorId;
        }

        public Builder world(String value) { this.worldId = value; return this; }
        public Builder dimension(String value) { this.dimensionId = value; return this; }
        public Builder biome(String value) { this.biomeId = value; return this; }
        public Builder entity(Identifier id, Set<Identifier> tags) { this.entityId = id; this.entityTags = tags; return this; }
        public Builder block(Identifier id, Set<Identifier> tags) { this.blockId = id; this.blockTags = tags; return this; }
        public Builder item(Identifier id, Set<Identifier> tags) { this.itemId = id; this.itemTags = tags; return this; }
        public Builder recipe(Identifier id) { this.recipeId = id; return this; }
        public Builder amount(long value) { this.amount = value; return this; }
        public Builder metadata(String key, String value) { this.metadata.put(key, value); return this; }

        public ProgressionEvent build() {
            return new ProgressionEvent(type, actorId, worldId, dimensionId, biomeId, entityId, entityTags,
                    blockId, blockTags, itemId, itemTags, recipeId, amount, metadata);
        }
    }
}
