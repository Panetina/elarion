package panetina.elarion.core.placeholder;

import panetina.elarion.core.model.ServerIdentityConfig;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;

public final class ElarionPlaceholderService {
    private static final Set<PlaceholderRenderContext> ALL = Set.of(PlaceholderRenderContext.values());
    private final ElarionPlaceholderRegistry registry = new ElarionPlaceholderRegistry();
    private final PlaceholderResolutionEngine engine = new PlaceholderResolutionEngine(registry);

    public ElarionPlaceholderService(ServerIdentityConfig identity) {
        registerIdentity(identity);
        registerContextValue("core.player.name", "player", "Current player display text");
        registerContextValue("core.message", "message", "Current message text");
        registerContextValue("core.realm.id", "realm", "Current Realm id or official text");
        registerContextValue("core.realm.short", "realm_short", "Current Realm short name");
        registerContextValue("core.realm.display", "realm_display", "Current Realm display name");
        registerContextValue("core.realm.official", "realm_official", "Current Realm official name");
        registerContextValue("core.realm.tag", "realm_tag", "Current Realm tag or short name");
    }

    public ElarionPlaceholderRegistry registry() {
        return registry;
    }

    public void register(PlaceholderDescriptor descriptor, PlaceholderResolver resolver) {
        registry.register(descriptor, resolver);
    }

    public void registerAlias(PlaceholderAlias alias) {
        registry.registerAlias(alias);
    }

    public PlaceholderResolution resolve(String template, PlaceholderResolutionContext context) {
        return engine.resolve(template, context, PlaceholderResolutionLimits.DEFAULTS);
    }

    public PlaceholderResolution resolve(String template, PlaceholderResolutionContext context,
                                         PlaceholderResolutionLimits limits) {
        return engine.resolve(template, context, limits);
    }

    public String resolvePublic(String template, PlaceholderRenderContext context, Map<String, String> values) {
        return resolve(template, PlaceholderResolutionContext.publicContext(context, values)).text();
    }

    /** Resolves server identity tokens while loading addon-authored configuration. */
    public String replaceIdentity(String template) {
        return resolvePublic(template, PlaceholderRenderContext.CONFIG, Map.of());
    }

    public static PlaceholderResolution resolveSchema(String template, String owner,
                                                      PlaceholderRenderContext renderContext,
                                                      Map<String, String> values) {
        ElarionPlaceholderRegistry schema = new ElarionPlaceholderRegistry();
        Map<String, String> bounded = new LinkedHashMap<>();
        if (values != null) {
            values.entrySet().stream().limit(64).forEach(entry -> {
                String key = safeSchemaKey(entry.getKey());
                if (!key.isBlank()) bounded.putIfAbsent(key, entry.getValue() == null ? "" : entry.getValue());
            });
        }
        bounded.forEach((key, value) -> {
            String id = "schema." + key;
            schema.register(PlaceholderDescriptor.publicString(id, owner, "Schema field " + key,
                    Set.of(renderContext)), ignored -> value);
            schema.registerAlias(new PlaceholderAlias(key, id, PlaceholderTransform.IDENTITY, false));
        });
        return new PlaceholderResolutionEngine(schema).resolve(template,
                PlaceholderResolutionContext.publicContext(renderContext, Map.of()),
                PlaceholderResolutionLimits.DEFAULTS);
    }

    private static String safeSchemaKey(String value) {
        if (value == null) return "";
        String clean = value.trim().toLowerCase(java.util.Locale.ROOT);
        return clean.matches("[a-z][a-z0-9_.:-]*") ? clean : "";
    }

    private void registerIdentity(ServerIdentityConfig identity) {
        identity.placeholders().forEach((legacy, value) -> {
            String canonical = "core.identity." + legacy.replace('_', '.');
            register(PlaceholderDescriptor.publicString(canonical, "platform:core",
                    "Server identity value " + legacy, ALL), ignored -> value);
            registerAlias(new PlaceholderAlias(legacy, canonical, PlaceholderTransform.IDENTITY, false));
            registerAlias(new PlaceholderAlias(legacy + "_upper", canonical, PlaceholderTransform.UPPER, false));
            registerAlias(new PlaceholderAlias(legacy + "_lower", canonical, PlaceholderTransform.LOWER, false));
            registerAlias(new PlaceholderAlias(legacy + "_title", canonical, PlaceholderTransform.TITLE, false));
        });
    }

    private void registerContextValue(String id, String contextKey, String description) {
        register(new PlaceholderDescriptor(id, "platform:core", description, PlaceholderValueType.STRING, ALL,
                Set.of(contextKey), PlaceholderVisibility.PUBLIC, PlaceholderFailureBehavior.PRESERVE_TOKEN,
                PlaceholderFailureBehavior.EMPTY), context -> context.value(contextKey));
        if (!registry.alias(contextKey).isPresent()) {
            registerAlias(new PlaceholderAlias(contextKey, id, PlaceholderTransform.IDENTITY, false));
        }
    }
}
