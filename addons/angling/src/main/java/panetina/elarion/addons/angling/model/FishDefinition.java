package panetina.elarion.addons.angling.model;

import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;

public record FishDefinition(
        Identifier id,
        String translationKey,
        AnglingRarity rarity,
        int weight,
        List<AnglingConditionId> conditions
) {
    public static final String NAMESPACE = "elarion_angling";
    public static final String PLACEHOLDER_PREFIX = "placeholder_";
    public static final String TRANSLATION_KEY_PREFIX = "fish.elarion_angling.";
    public static final int MAX_CONDITIONS = 32;

    public FishDefinition {
        if (id == null) {
            throw new FishDefinitionValidationException("Fish ID is required.");
        }
        if (!NAMESPACE.equals(id.getNamespace())) {
            throw new FishDefinitionValidationException("Fish ID namespace must be " + NAMESPACE + ".");
        }
        if (id.getPath().isBlank() || !id.getPath().startsWith(PLACEHOLDER_PREFIX)) {
            throw new FishDefinitionValidationException(
                    "Fish ID path must start with " + PLACEHOLDER_PREFIX + ".");
        }
        if (translationKey == null || translationKey.isBlank()) {
            throw new FishDefinitionValidationException("Translation key must not be blank.");
        }
        if (!translationKey.startsWith(TRANSLATION_KEY_PREFIX)) {
            throw new FishDefinitionValidationException(
                    "Translation key must start with " + TRANSLATION_KEY_PREFIX + ".");
        }
        if (rarity == null) {
            throw new FishDefinitionValidationException("Rarity is required.");
        }
        if (weight <= 0) {
            throw new FishDefinitionValidationException("Weight must be positive.");
        }
        if (conditions == null) {
            conditions = List.of();
        } else {
            conditions = List.copyOf(conditions);
            if (conditions.size() > MAX_CONDITIONS) {
                throw new FishDefinitionValidationException(
                        "Fish definitions may contain at most " + MAX_CONDITIONS + " conditions.");
            }
            Set<AnglingConditionId> uniqueConditions = new HashSet<>();
            for (AnglingConditionId condition : conditions) {
                Objects.requireNonNull(condition, "condition");
                if (!uniqueConditions.add(condition)) {
                    throw new FishDefinitionValidationException(
                            "Duplicate condition ID: " + condition.value());
                }
            }
        }
    }
}
