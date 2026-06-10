package panetina.elarion.core.config;

import net.minecraft.util.Identifier;
import panetina.elarion.core.model.CitizenStatus;
import panetina.elarion.core.service.RewardActionService;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

final class CoreConfigReferenceValidator {
    private static final Set<String> CORE_TITLE_EFFECT_TYPES = Set.of("status_effect");

    private CoreConfigReferenceValidator() {
    }

    static void validate(Function<String, Map<String, Object>> loader, List<String> errors) {
        Map<String, Object> titles = loader.apply("titles.yml");
        Map<String, Object> rewards = loader.apply("rewards.yml");
        Map<String, Object> abilities = loader.apply("abilities.yml");
        Map<String, Object> defaults = loader.apply("citizens-defaults.yml");

        Set<String> titleIds = ids(map(titles.get("titles")));
        Set<String> abilityIds = ids(map(abilities.get("abilities")));
        collectTitleAbilities(titles, abilityIds);

        validateDefaultTitle(defaults, titleIds, errors);
        validateTitleReferences(titles, abilityIds, errors);
        validateRewardReferences(rewards, titleIds, abilityIds, errors);
    }

    private static void validateDefaultTitle(
            Map<String, Object> defaults,
            Set<String> titleIds,
            List<String> errors
    ) {
        String title = string(map(defaults.get("defaults")).get("title"));
        if (!title.isBlank() && !titleIds.contains(normalize(title))) {
            errors.add("citizens-defaults.yml.defaults.title: unknown title '" + title + "'");
        }
    }

    private static void validateTitleReferences(
            Map<String, Object> root,
            Set<String> abilityIds,
            List<String> errors
    ) {
        map(root.get("titles")).forEach((id, raw) -> {
            String path = "titles.yml.titles." + id;
            Map<String, Object> title = map(raw);
            for (String ability : stringSet(title.get("abilities"))) {
                if (!isAbilityId(ability)) {
                    errors.add(path + ".abilities: invalid ability id '" + ability + "'");
                }
            }
            validateTitleEffects(path + ".active-effects", title.get("active-effects"), errors);
        });
    }

    private static void validateTitleEffects(String path, Object raw, List<String> errors) {
        if (!(raw instanceof Collection<?> collection)) return;
        int index = 0;
        for (Object item : collection) {
            Map<String, Object> effect = map(item);
            String type = normalizeUnderscore(string(effect.get("type")));
            String effectPath = path + "[" + index + "]";
            if (!CORE_TITLE_EFFECT_TYPES.contains(type)) {
                errors.add(effectPath + ".type: unknown Core title effect '" + string(effect.get("type")) + "'");
            }
            if ("status_effect".equals(type)) {
                validateIdentifier(effectPath + ".id", string(effect.get("id")), errors);
                validateOptionalInteger(effectPath + ".amplifier", effect.get("amplifier"), errors);
            }
            index++;
        }
    }

    private static void validateRewardReferences(
            Map<String, Object> root,
            Set<String> titleIds,
            Set<String> abilityIds,
            List<String> errors
    ) {
        map(root.get("rewards")).forEach((id, raw) -> {
            Object actions = map(raw).get("actions");
            if (!(actions instanceof Collection<?> collection)) return;
            int index = 0;
            for (Object item : collection) {
                Map<String, Object> action = map(item);
                String type = normalizeDash(string(action.get("type")));
                String path = "rewards.yml.rewards." + id + ".actions[" + index + "]";
                if (!RewardActionService.builtInActionTypes().contains(type)) {
                    errors.add(path + ".type: unknown Core reward action '" + string(action.get("type")) + "'");
                }
                validateRewardActionParameters(path, type, action, titleIds, abilityIds, errors);
                index++;
            }
        });
    }

    private static void validateRewardActionParameters(
            String path,
            String type,
            Map<String, Object> action,
            Set<String> titleIds,
            Set<String> abilityIds,
            List<String> errors
    ) {
        switch (type) {
            case "item" -> {
                validateIdentifier(path + ".id", string(action.get("id")), errors);
                validateOptionalInteger(path + ".count", action.get("count"), errors);
            }
            case "status-change" -> validateEnum(path + ".status", string(action.get("status")),
                    CitizenStatus.class, errors);
            case "title-grant" -> {
                String title = string(action.get("title"));
                if (!titleIds.contains(normalize(title))) {
                    errors.add(path + ".title: unknown title '" + title + "'");
                }
            }
            case "ability-grant" -> {
                String ability = string(action.get("ability"));
                if (!isAbilityId(ability)) {
                    errors.add(path + ".ability: invalid ability id '" + ability + "'");
                }
            }
            default -> {
            }
        }
    }

    private static void collectTitleAbilities(Map<String, Object> titles, Set<String> abilityIds) {
        map(titles.get("titles")).values().forEach(raw ->
                abilityIds.addAll(stringSet(map(raw).get("abilities"))));
    }

    private static Set<String> ids(Map<String, Object> values) {
        Set<String> result = new LinkedHashSet<>();
        values.keySet().forEach(id -> result.add(normalize(id)));
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> result ? (Map<String, Object>) result : Map.of();
    }

    private static Set<String> stringSet(Object value) {
        if (!(value instanceof Collection<?> collection)) return Set.of();
        Set<String> result = new LinkedHashSet<>();
        collection.forEach(item -> result.add(string(item)));
        return result;
    }

    private static String string(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    private static String normalizeDash(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private static String normalizeUnderscore(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }

    private static boolean isAbilityId(String value) {
        return value.matches("[a-z0-9_.-]+(\\.[a-z0-9_.-]+)+");
    }

    private static void validateIdentifier(String path, String value, List<String> errors) {
        if (value.isBlank() || Identifier.tryParse(value) == null) {
            errors.add(path + ": invalid registry ID '" + value + "'");
        }
    }

    private static void validateOptionalInteger(String path, Object value, List<String> errors) {
        if (value == null) return;
        if (value instanceof Number) return;
        try {
            Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException exception) {
            errors.add(path + ": expected an integer");
        }
    }

    private static <T extends Enum<T>> void validateEnum(
            String path,
            String value,
            Class<T> enumType,
            List<String> errors
    ) {
        try {
            Enum.valueOf(enumType, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            errors.add(path + ": unknown value '" + value + "'");
        }
    }
}
