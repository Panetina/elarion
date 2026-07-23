package panetina.elarion.addons.angling.domainmap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;
import panetina.elarion.addons.angling.compile.AnglingTypedCompilerRegistry;
import panetina.elarion.addons.angling.compile.AnglingDefinitionCompilerSet;
import panetina.elarion.addons.angling.definition.AnglingTypedNode;
import panetina.elarion.addons.angling.modifier.AnglingCompiledModifier;
import panetina.elarion.addons.angling.modifier.AnglingEquipmentModifierCompilers;
import panetina.elarion.addons.angling.modifier.AnglingModifierValue;
import panetina.elarion.addons.angling.treasure.AnglingTreasureDefinition;
import panetina.elarion.addons.angling.minigame.AnglingSweetspotBehaviorType;
import panetina.elarion.addons.angling.minigame.AnglingSweetspotBehaviors;
import panetina.elarion.addons.angling.restriction.AnglingRestriction;
import panetina.elarion.addons.angling.restriction.AnglingRestrictionCompilers;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class AnglingDomainMapReloadListener implements SimpleSynchronousResourceReloadListener {
    private static final int MAX_VALUES = 256;
    private static final Identifier LISTENER_ID = id("domain_maps");
    private static final Identifier AQUARIUM = id("elarion_angling/aquarium/interactions.json");
    private static final Identifier TACKLE_SKINS = id("elarion_angling/equipment/tackle_skins.json");
    private static final Identifier ITEM_MODIFIERS = id("elarion_angling/equipment/item_modifiers.json");
    private static final Identifier EFFECT_MODIFIERS = id("elarion_angling/equipment/effect_modifiers.json");
    private static final Identifier TREASURES = id("elarion_angling/treasure/by_catch.json");
    private static final int MAX_MODIFIERS_PER_SELECTOR = 64;
    private static final AnglingTypedCompilerRegistry<AnglingModifierValue> MODIFIER_COMPILERS =
            AnglingEquipmentModifierCompilers.create();
    private static final AnglingDefinitionCompilerSet<AnglingRestriction, AnglingModifierValue,
            AnglingSweetspotBehaviorType> INLINE_DEFINITION_COMPILERS = new AnglingDefinitionCompilerSet<>(
            AnglingRestrictionCompilers.create(), MODIFIER_COMPILERS, AnglingSweetspotBehaviors.create());

    private final AnglingDomainMapRepository repository;

    AnglingDomainMapReloadListener(AnglingDomainMapRepository repository) {
        this.repository = repository;
    }

    @Override
    public Identifier getFabricId() {
        return LISTENER_ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<AnglingRegistrySelector, AnglingAquariumInteraction> aquarium = parse(
                manager, AQUARIUM, value -> AnglingAquariumInteraction.parse(requiredString(value, "interaction")));
        Map<AnglingRegistrySelector, Identifier> tackleSkins = parse(
                manager, TACKLE_SKINS, value -> {
                    Identifier identifier = Identifier.tryParse(requiredString(value, "tackle skin"));
                    if (identifier == null) throw new IllegalArgumentException("Invalid tackle skin identifier");
                    return identifier;
                });
        Map<AnglingRegistrySelector, List<AnglingCompiledModifier>> itemModifiers = parse(
                manager, ITEM_MODIFIERS, AnglingDomainMapReloadListener::compileModifierList);
        Map<AnglingRegistrySelector, List<AnglingCompiledModifier>> effectModifiers = parse(
                manager, EFFECT_MODIFIERS, AnglingDomainMapReloadListener::compileModifierList);
        Map<AnglingRegistrySelector, AnglingTreasureDefinition> treasures = parse(
                manager, TREASURES, value -> AnglingTreasureDefinition.CODEC.parse(JsonOps.INSTANCE, value).getOrThrow());
        repository.publish(aquarium, tackleSkins, itemModifiers, effectModifiers, treasures);
    }

    private static <T> Map<AnglingRegistrySelector, T> parse(
            ResourceManager manager,
            Identifier resourceId,
            java.util.function.Function<JsonElement, T> valueParser
    ) {
        Resource resource = manager.getResource(resourceId)
                .orElseThrow(() -> new IllegalStateException("Missing required Angling domain map " + resourceId));
        try (Reader reader = resource.getReader()) {
            return decode(resourceId, JsonParser.parseReader(reader), valueParser);
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Failed to compile Angling domain map " + resourceId + ": "
                    + exception.getMessage(), exception);
        }
    }

    static <T> Map<AnglingRegistrySelector, T> decode(
            Identifier resourceId,
            JsonElement parsed,
            java.util.function.Function<JsonElement, T> valueParser
    ) {
        try {
            if (!parsed.isJsonObject()) throw new IllegalArgumentException("root must be an object");
            JsonObject root = parsed.getAsJsonObject();
            if (!root.has("schema_version") || root.get("schema_version").getAsInt() != 1) {
                throw new IllegalArgumentException("unsupported schema_version");
            }
            JsonElement valuesElement = root.get("values");
            if (valuesElement == null || !valuesElement.isJsonObject()) {
                throw new IllegalArgumentException("values must be an object");
            }
            JsonObject values = valuesElement.getAsJsonObject();
            if (values.size() > MAX_VALUES) throw new IllegalArgumentException("values exceed " + MAX_VALUES);
            Map<AnglingRegistrySelector, T> result = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
                AnglingRegistrySelector selector = AnglingRegistrySelector.parse(entry.getKey());
                if (result.putIfAbsent(selector, valueParser.apply(entry.getValue())) != null) {
                    throw new IllegalArgumentException("duplicate selector " + selector);
                }
            }
            return result;
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Failed to compile Angling domain map " + resourceId + ": "
                    + exception.getMessage(), exception);
        }
    }

    private static String requiredString(JsonElement value, String label) {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(label + " must be a string");
        }
        return value.getAsString();
    }

    static List<AnglingCompiledModifier> compileModifierList(JsonElement value) {
        if (value == null || !value.isJsonArray()) {
            throw new IllegalArgumentException("modifier value must be an array");
        }
        if (value.getAsJsonArray().size() > MAX_MODIFIERS_PER_SELECTOR) {
            throw new IllegalArgumentException("modifier list exceeds " + MAX_MODIFIERS_PER_SELECTOR);
        }
        java.util.ArrayList<AnglingCompiledModifier> compiled = new java.util.ArrayList<>();
        for (JsonElement element : value.getAsJsonArray()) {
            AnglingTypedNode node = AnglingTypedNode.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
            AnglingModifierValue modifier = MODIFIER_COMPILERS.compile(node.type(), node);
            if (modifier instanceof AnglingModifierValue.AddToPool addToPool) {
                addToPool.definition().ifPresent(definition -> INLINE_DEFINITION_COMPILERS.compile(
                        node.type(), definition));
            } else if (modifier instanceof AnglingModifierValue.OverrideCatch overrideCatch) {
                INLINE_DEFINITION_COMPILERS.compile(node.type(), overrideCatch.definition());
            }
            compiled.add(new AnglingCompiledModifier(node.type(), modifier));
        }
        return List.copyOf(compiled);
    }

    private static Identifier id(String path) {
        return Identifier.of(ElarionAnglingAddon.MOD_ID, path);
    }
}
