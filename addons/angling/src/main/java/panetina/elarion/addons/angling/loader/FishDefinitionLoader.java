package panetina.elarion.addons.angling.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.model.AnglingConditionId;
import panetina.elarion.addons.angling.model.AnglingRarity;
import panetina.elarion.addons.angling.model.FishDefinition;
import panetina.elarion.addons.angling.model.FishDefinitionIndex;
import panetina.elarion.addons.angling.model.FishDefinitionValidationException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class FishDefinitionLoader {
    public FishDefinitionIndex load(Map<String, String> documents) {
        Objects.requireNonNull(documents, "documents");

        Map<String, JsonElement> parsed = new LinkedHashMap<>();
        documents.forEach((documentId, json) -> {
            if (json == null) {
                throw parseFailure(documentId, "$", "document JSON is required");
            }
            try {
                parsed.put(documentId, JsonParser.parseString(json));
            } catch (JsonParseException exception) {
                throw parseFailure(documentId, "$", "invalid JSON");
            }
        });
        return loadElements(parsed);
    }

    public FishDefinitionIndex loadElements(Map<String, JsonElement> documents) {
        Objects.requireNonNull(documents, "documents");

        List<FishDefinition> definitions = new ArrayList<>(documents.size());
        documents.forEach((documentId, element) -> definitions.add(parseDefinition(documentId, element)));
        try {
            return new FishDefinitionIndex(definitions);
        } catch (FishDefinitionValidationException exception) {
            throw parseFailure("<index>", "$", exception.getMessage());
        }
    }

    private FishDefinition parseDefinition(String documentId, JsonElement element) {
        if (documentId == null || documentId.isBlank()) {
            throw parseFailure("<unknown>", "$", "document ID must not be blank");
        }
        if (element == null || !element.isJsonObject()) {
            throw parseFailure(documentId, "$", "root must be an object");
        }

        JsonObject object = element.getAsJsonObject();
        Identifier id = parseIdentifier(documentId, object, "id");
        String translationKey = requiredString(documentId, object, "translation_key");
        AnglingRarity rarity = parseRarity(documentId, object);
        int weight = parseWeight(documentId, object);
        List<AnglingConditionId> conditions = parseConditions(documentId, object);

        try {
            return new FishDefinition(id, translationKey, rarity, weight, conditions);
        } catch (FishDefinitionValidationException exception) {
            throw parseFailure(documentId, "$", exception.getMessage());
        }
    }

    private Identifier parseIdentifier(String documentId, JsonObject object, String field) {
        String raw = requiredString(documentId, object, field);
        Identifier id = Identifier.tryParse(raw);
        if (id == null) {
            throw parseFailure(documentId, field, "must be a valid identifier");
        }
        return id;
    }

    private AnglingRarity parseRarity(String documentId, JsonObject object) {
        String raw = requiredString(documentId, object, "rarity");
        try {
            return AnglingRarity.valueOf(raw);
        } catch (IllegalArgumentException exception) {
            throw parseFailure(documentId, "rarity", "unknown rarity " + raw);
        }
    }

    private int parseWeight(String documentId, JsonObject object) {
        JsonElement element = required(documentId, object, "weight");
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw parseFailure(documentId, "weight", "must be a number");
        }
        try {
            return element.getAsInt();
        } catch (NumberFormatException exception) {
            throw parseFailure(documentId, "weight", "must be an integer");
        }
    }

    private List<AnglingConditionId> parseConditions(String documentId, JsonObject object) {
        JsonElement element = object.get("conditions");
        if (element == null || element.isJsonNull()) {
            return List.of();
        }
        if (!element.isJsonArray()) {
            throw parseFailure(documentId, "conditions", "must be an array");
        }

        JsonArray array = element.getAsJsonArray();
        List<AnglingConditionId> conditions = new ArrayList<>(array.size());
        for (int index = 0; index < array.size(); index++) {
            JsonElement condition = array.get(index);
            String path = "conditions[" + index + "]";
            if (!condition.isJsonPrimitive() || !condition.getAsJsonPrimitive().isString()) {
                throw parseFailure(documentId, path, "must be a string");
            }
            try {
                conditions.add(AnglingConditionId.of(condition.getAsString()));
            } catch (FishDefinitionValidationException exception) {
                throw parseFailure(documentId, path, exception.getMessage());
            }
        }
        return List.copyOf(conditions);
    }

    private String requiredString(String documentId, JsonObject object, String field) {
        JsonElement element = required(documentId, object, field);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw parseFailure(documentId, field, "must be a string");
        }
        String value = element.getAsString();
        if (value.isBlank()) {
            throw parseFailure(documentId, field, "must not be blank");
        }
        return value;
    }

    private JsonElement required(String documentId, JsonObject object, String field) {
        JsonElement element = object.get(field);
        if (element == null || element.isJsonNull()) {
            throw parseFailure(documentId, field, "is required");
        }
        return element;
    }

    private FishDefinitionParseException parseFailure(String documentId, String fieldPath, String message) {
        return new FishDefinitionParseException(documentId, fieldPath, message);
    }
}
