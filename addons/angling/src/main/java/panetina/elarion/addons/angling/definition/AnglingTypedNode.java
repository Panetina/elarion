package panetina.elarion.addons.angling.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Identifier;

import java.util.Objects;

/**
 * Immutable reload DTO for a polymorphic restriction or modifier.
 * Registered handlers compile this bounded JSON once during resource reload;
 * gameplay paths never parse it.
 */
public record AnglingTypedNode(Identifier type, String sourceJson) {
    public static final int MAX_JSON_CHARACTERS = 16_384;

    public static final Codec<AnglingTypedNode> CODEC = Codec.PASSTHROUGH.comapFlatMap(
            AnglingTypedNode::decode,
            AnglingTypedNode::encode
    );

    public AnglingTypedNode {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(sourceJson, "sourceJson");
        if (sourceJson.length() > MAX_JSON_CHARACTERS) {
            throw new IllegalArgumentException("Typed Angling node exceeds bounded JSON size");
        }
        JsonElement parsed = JsonParser.parseString(sourceJson);
        if (!parsed.isJsonObject()) {
            throw new IllegalArgumentException("Typed Angling node must be a JSON object");
        }
        String encodedType = parsed.getAsJsonObject().has("type")
                ? parsed.getAsJsonObject().get("type").getAsString()
                : "";
        if (!type.toString().equals(encodedType)) {
            throw new IllegalArgumentException("Typed Angling node type must match its JSON type field");
        }
        sourceJson = parsed.toString();
    }

    public JsonObject copySource() {
        return JsonParser.parseString(sourceJson).getAsJsonObject();
    }

    private static DataResult<AnglingTypedNode> decode(Dynamic<?> dynamic) {
        try {
            JsonElement element = dynamic.convert(JsonOps.INSTANCE).getValue();
            if (!element.isJsonObject()) {
                return DataResult.error(() -> "Typed Angling node must be a JSON object");
            }
            JsonObject object = element.getAsJsonObject();
            if (!object.has("type") || !object.get("type").isJsonPrimitive()) {
                return DataResult.error(() -> "Typed Angling node is missing a string type");
            }
            Identifier type = Identifier.tryParse(object.get("type").getAsString());
            if (type == null) {
                return DataResult.error(() -> "Typed Angling node has an invalid type identifier");
            }
            if (object.toString().length() > MAX_JSON_CHARACTERS) {
                return DataResult.error(() -> "Typed Angling node exceeds bounded JSON size");
            }
            return DataResult.success(new AnglingTypedNode(type, object.toString()));
        } catch (RuntimeException exception) {
            return DataResult.error(() -> "Invalid typed Angling node: " + exception.getMessage());
        }
    }

    private static Dynamic<?> encode(AnglingTypedNode node) {
        return new Dynamic<>(JsonOps.INSTANCE, JsonParser.parseString(node.sourceJson));
    }
}
