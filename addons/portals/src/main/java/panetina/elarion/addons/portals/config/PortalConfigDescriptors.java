package panetina.elarion.addons.portals.config;

import panetina.elarion.addons.portals.model.PortalRouteDefinition;
import panetina.elarion.addons.portals.model.PortalRouteMode;
import panetina.elarion.addons.portals.model.PortalScheduleDefinition;
import panetina.elarion.addons.portals.model.PortalUiConfig;
import panetina.elarion.addons.portals.model.PortalVisualDefinition;
import panetina.elarion.core.config.ElarionConfigCategory;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.config.ElarionConfigValidator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class PortalConfigDescriptors {
    private PortalConfigDescriptors() {
    }

    public static void register(
            ElarionConfigRegistry registry,
            Supplier<Collection<PortalRouteDefinition>> routes,
            Supplier<PortalUiConfig> ui
    ) {
        registry.registerDomain(domain(routes, ui));
    }

    public static ElarionConfigDomain domain(
            Supplier<Collection<PortalRouteDefinition>> routes,
            Supplier<PortalUiConfig> ui
    ) {
        List<PortalRouteDefinition> snapshot = sortedRoutes(routes);
        PortalUiConfig uiSnapshot = safeUi(ui);
        return new ElarionConfigDomain(
                "portals",
                "addons:portals",
                "Portals",
                "Portal route definitions, schedules, pricing keys, visuals, and travel prompt UI.",
                List.of(
                        "config/elarion/addons/portals/routes.yml",
                        "config/elarion/addons/portals/ui.yml"),
                "/e portal reload",
                List.of(
                        new ElarionConfigCategory(
                                "general",
                                "General",
                                "Portal route discovery and travel prompt UI settings.",
                                generalEntries(routes, ui, snapshot, uiSnapshot)),
                        new ElarionConfigCategory(
                                "routes",
                                "Routes",
                                "Current loaded route definition summaries.",
                                routeEntries(routes, snapshot))));
    }

    private static List<ElarionConfigEntry<?>> generalEntries(
            Supplier<Collection<PortalRouteDefinition>> routes,
            Supplier<PortalUiConfig> ui,
            List<PortalRouteDefinition> snapshot,
            PortalUiConfig uiSnapshot
    ) {
        return List.of(
                intEntry("routes.count", "Route Count",
                        "Number of currently loaded portal route definitions.",
                        "routes.yml.routes",
                        snapshot.size(),
                        () -> sortedRoutes(routes).size(),
                        1,
                        Integer.MAX_VALUE),
                stringEntry("routes.ids", "Route IDs",
                        "Comma-separated portal route IDs currently known to Portals.",
                        "routes.yml.routes",
                        routeIds(snapshot),
                        () -> routeIds(sortedRoutes(routes)),
                        false),
                stringEntry("ui.theme-variant", "UI Theme Variant",
                        "Shared Elarion UI theme variant used by Portal prompts.",
                        "ui.yml.theme-variant",
                        uiSnapshot.themeVariant(),
                        () -> safeUi(ui).themeVariant()),
                intEntry("ui.logical-width", "Logical Width",
                        "Portal prompt logical width.",
                        "ui.yml.logical-width",
                        uiSnapshot.logicalWidth(),
                        () -> safeUi(ui).logicalWidth(),
                        320,
                        960),
                intEntry("ui.logical-height", "Logical Height",
                        "Portal prompt logical height.",
                        "ui.yml.logical-height",
                        uiSnapshot.logicalHeight(),
                        () -> safeUi(ui).logicalHeight(),
                        160,
                        720),
                intEntry("ui.minimum-scale-percent", "Minimum Scale Percent",
                        "Minimum prompt scale percentage.",
                        "ui.yml.minimum-scale-percent",
                        uiSnapshot.minimumScalePercent(),
                        () -> safeUi(ui).minimumScalePercent(),
                        25,
                        100),
                intEntry("ui.confirm-button-width", "Confirm Button Width",
                        "Prompt confirm-button width.",
                        "ui.yml.confirm-button-width",
                        uiSnapshot.confirmButtonWidth(),
                        () -> safeUi(ui).confirmButtonWidth(),
                        1,
                        Integer.MAX_VALUE),
                intEntry("ui.close-button-width", "Close Button Width",
                        "Prompt close-button width.",
                        "ui.yml.close-button-width",
                        uiSnapshot.closeButtonWidth(),
                        () -> safeUi(ui).closeButtonWidth(),
                        1,
                        Integer.MAX_VALUE));
    }

    private static List<ElarionConfigEntry<?>> routeEntries(
            Supplier<Collection<PortalRouteDefinition>> routes,
            List<PortalRouteDefinition> snapshot
    ) {
        List<ElarionConfigEntry<?>> entries = new ArrayList<>();
        for (PortalRouteDefinition route : snapshot) {
            entries.add(routeStringEntry(route, "display-name", "Display Name",
                    "Route display name.",
                    routes, PortalRouteDefinition::displayName));
            entries.add(routeStringEntry(route, "description", "Description",
                    "Route description used in prompts and notices.",
                    routes, PortalRouteDefinition::description, false));
            entries.add(routeStringEntry(route, "source-dimension", "Source Dimension",
                    "Dimension containing the outbound gate.",
                    routes, PortalRouteDefinition::sourceDimension));
            entries.add(routeStringEntry(route, "destination-dimension", "Destination Dimension",
                    "Destination dimension, or `*` for unrestricted always-open routes.",
                    routes, PortalRouteDefinition::destinationDimension));
            entries.add(routeBoolEntry(route, "enabled", "Enabled",
                    "Whether this route definition is enabled.",
                    routes, PortalRouteDefinition::enabled));
            entries.add(routeStringEntry(route, "mode", "Mode",
                    "Route travel mode.",
                    routes, value -> value.mode().configId(), routeModeChoices()));
            entries.add(routeStringEntry(route, "ticket.id", "Ticket ID",
                    "Physical ticket ID for ticketed routes.",
                    routes, PortalRouteDefinition::ticketId, false));
            entries.add(routeStringEntry(route, "ticket.price-key", "Ticket Price Key",
                    "Economy service price key for ticketed outbound travel.",
                    routes, PortalRouteDefinition::ticketPriceKey, false));
            entries.add(routeStringEntry(route, "passage.price-key", "Passage Price Key",
                    "Economy service price key for fee-passage outbound travel.",
                    routes, PortalRouteDefinition::passagePriceKey, false));
            entries.add(routeBoolEntry(route, "passage.first-round-trip-free", "First Trip Free",
                    "Whether the first outbound-and-return cycle can be free.",
                    routes, PortalRouteDefinition::firstRoundTripFree));
            entries.add(routeStringEntry(route, "schedule.timezone", "Schedule Timezone",
                    "Configured schedule timezone.",
                    routes, value -> value.schedule().zone().toString()));
            entries.add(routeStringEntry(route, "schedule.anchor", "Schedule Anchor",
                    "Configured recurring schedule anchor.",
                    routes, value -> value.schedule().anchor().toString()));
            entries.add(routeStringEntry(route, "schedule.interval", "Schedule Interval",
                    "Configured time between windows.",
                    routes, value -> duration(value.schedule().interval())));
            entries.add(routeStringEntry(route, "schedule.duration", "Schedule Duration",
                    "Configured open-window duration.",
                    routes, value -> duration(value.schedule().duration())));
            entries.add(routeStringEntry(route, "schedule.warnings", "Schedule Warnings",
                    "Configured warning offsets.",
                    routes, value -> warnings(value.schedule()), false));
            entries.add(routeStringEntry(route, "visual.color", "Visual Color",
                    "Route visual RGB color.",
                    routes, value -> color(value.visual().rgb())));
            entries.add(routeStringEntry(route, "visual.brightness", "Brightness",
                    "Route visual brightness multiplier.",
                    routes, value -> Float.toString(value.visual().brightness())));
            entries.add(routeStringEntry(route, "visual.opacity", "Opacity",
                    "Route visual opacity.",
                    routes, value -> Float.toString(value.visual().opacity())));
            entries.add(routeIntEntry(route, "visual.frame-time", "Frame Time",
                    "Portal visual animation frame time.",
                    routes, value -> value.visual().frameTime(), 1, Integer.MAX_VALUE));
            entries.add(routeStringEntry(route, "visual.texture", "Texture",
                    "Route portal texture identifier.",
                    routes, value -> value.visual().texture()));
            entries.add(routeStringEntry(route, "visual.icon-item", "Prompt Icon Item",
                    "Prompt icon item identifier.",
                    routes, value -> value.visual().iconItem(), false));
            entries.add(routeStringEntry(route, "visual.status-icon-item", "Status Icon Item",
                    "HUD route status icon item identifier.",
                    routes, value -> value.visual().statusIconItem(), false));
            entries.add(routeStringEntry(route, "visual.prompt-accent-color", "Prompt Accent Color",
                    "Optional prompt accent ARGB color.",
                    routes, value -> optionalArgb(value.visual()), false));
        }
        return entries;
    }

    private static ElarionConfigEntry<Boolean> routeBoolEntry(
            PortalRouteDefinition route,
            String field,
            String label,
            String description,
            Supplier<Collection<PortalRouteDefinition>> routes,
            Function<PortalRouteDefinition, Boolean> value
    ) {
        return boolEntry(routeId(route, field), routeLabel(route, label), description,
                routePath(route, field), value.apply(route),
                () -> value.apply(currentRoute(routes, route)));
    }

    private static ElarionConfigEntry<Integer> routeIntEntry(
            PortalRouteDefinition route,
            String field,
            String label,
            String description,
            Supplier<Collection<PortalRouteDefinition>> routes,
            Function<PortalRouteDefinition, Integer> value,
            int minimum,
            int maximum
    ) {
        return intEntry(routeId(route, field), routeLabel(route, label), description,
                routePath(route, field), value.apply(route),
                () -> value.apply(currentRoute(routes, route)), minimum, maximum);
    }

    private static ElarionConfigEntry<String> routeStringEntry(
            PortalRouteDefinition route,
            String field,
            String label,
            String description,
            Supplier<Collection<PortalRouteDefinition>> routes,
            Function<PortalRouteDefinition, String> value
    ) {
        return routeStringEntry(route, field, label, description, routes, value, List.of(), true);
    }

    private static ElarionConfigEntry<String> routeStringEntry(
            PortalRouteDefinition route,
            String field,
            String label,
            String description,
            Supplier<Collection<PortalRouteDefinition>> routes,
            Function<PortalRouteDefinition, String> value,
            boolean nonBlank
    ) {
        return routeStringEntry(route, field, label, description, routes, value, List.of(), nonBlank);
    }

    private static ElarionConfigEntry<String> routeStringEntry(
            PortalRouteDefinition route,
            String field,
            String label,
            String description,
            Supplier<Collection<PortalRouteDefinition>> routes,
            Function<PortalRouteDefinition, String> value,
            List<String> choices
    ) {
        return routeStringEntry(route, field, label, description, routes, value, choices, true);
    }

    private static ElarionConfigEntry<String> routeStringEntry(
            PortalRouteDefinition route,
            String field,
            String label,
            String description,
            Supplier<Collection<PortalRouteDefinition>> routes,
            Function<PortalRouteDefinition, String> value,
            List<String> choices,
            boolean nonBlank
    ) {
        return stringEntry(
                routeId(route, field),
                routeLabel(route, label),
                description,
                routePath(route, field),
                value.apply(route),
                () -> value.apply(currentRoute(routes, route)),
                choices,
                nonBlank);
    }

    private static ElarionConfigEntry<Boolean> boolEntry(
            String id,
            String label,
            String description,
            String path,
            boolean defaultValue,
            Supplier<Boolean> currentValue
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.BOOLEAN, defaultValue, currentValue,
                ElarionConfigValidator.pass(), List.of("true", "false"), "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<Integer> intEntry(
            String id,
            String label,
            String description,
            String path,
            int defaultValue,
            Supplier<Integer> currentValue,
            int minimum,
            int maximum
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.INTEGER, defaultValue, currentValue,
                ElarionConfigValidator.integerRange(path, minimum, maximum), List.of(),
                Integer.toString(minimum), maximum == Integer.MAX_VALUE ? "" : Integer.toString(maximum),
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue
    ) {
        return stringEntry(id, label, description, path, defaultValue, currentValue, List.of(), true);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue,
            boolean nonBlank
    ) {
        return stringEntry(id, label, description, path, defaultValue, currentValue, List.of(), nonBlank);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue,
            List<String> choices,
            boolean nonBlank
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.STRING, defaultValue, currentValue,
                nonBlank ? ElarionConfigValidator.nonBlank(path) : ElarionConfigValidator.pass(),
                choices, "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static PortalRouteDefinition currentRoute(
            Supplier<Collection<PortalRouteDefinition>> routes,
            PortalRouteDefinition fallback
    ) {
        for (PortalRouteDefinition route : sortedRoutes(routes)) {
            if (route.id().equals(fallback.id())) return route;
        }
        return fallback;
    }

    private static List<PortalRouteDefinition> sortedRoutes(Supplier<Collection<PortalRouteDefinition>> routes) {
        Collection<PortalRouteDefinition> value = routes == null ? null : routes.get();
        if (value == null) return List.of();
        return value.stream()
                .sorted(Comparator.comparing(PortalRouteDefinition::id))
                .toList();
    }

    private static PortalUiConfig safeUi(Supplier<PortalUiConfig> ui) {
        PortalUiConfig value = ui == null ? null : ui.get();
        return value == null ? PortalUiConfig.defaults() : value;
    }

    private static String routeId(PortalRouteDefinition route, String field) {
        return "routes." + route.id() + "." + field;
    }

    private static String routePath(PortalRouteDefinition route, String field) {
        return "routes.yml.routes." + route.id() + "." + field;
    }

    private static String routeLabel(PortalRouteDefinition route, String fieldLabel) {
        return route.id() + " " + fieldLabel;
    }

    private static List<String> routeModeChoices() {
        List<String> choices = new ArrayList<>();
        for (PortalRouteMode mode : PortalRouteMode.values()) {
            choices.add(mode.configId());
        }
        return choices;
    }

    private static String routeIds(List<PortalRouteDefinition> routes) {
        return routes.stream().map(PortalRouteDefinition::id).reduce((left, right) -> left + ", " + right).orElse("");
    }

    private static String duration(Duration duration) {
        if (duration.isZero()) return "0s";
        long seconds = duration.getSeconds();
        if (seconds % 86_400L == 0L) return seconds / 86_400L + "d";
        if (seconds % 3_600L == 0L) return seconds / 3_600L + "h";
        if (seconds % 60L == 0L) return seconds / 60L + "m";
        return seconds + "s";
    }

    private static String warnings(PortalScheduleDefinition schedule) {
        return schedule.warnings().stream()
                .map(PortalConfigDescriptors::duration)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static String color(int rgb) {
        return String.format(java.util.Locale.ROOT, "#%06X", rgb & 0xFFFFFF);
    }

    private static String optionalArgb(PortalVisualDefinition visual) {
        return visual.promptAccentColor() == 0 ? "" : String.format(
                java.util.Locale.ROOT, "#%08X", visual.promptAccentColor());
    }
}
