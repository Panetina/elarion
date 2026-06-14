package panetina.elarion.addons.portals.config;

import panetina.elarion.addons.economy.EconomyItems;
import org.yaml.snakeyaml.Yaml;
import panetina.elarion.addons.portals.PortalContent;
import panetina.elarion.addons.portals.model.PortalRouteDefinition;
import panetina.elarion.addons.portals.model.PortalRouteMode;
import panetina.elarion.addons.portals.model.PortalScheduleDefinition;
import panetina.elarion.addons.portals.model.PortalUiConfig;
import panetina.elarion.addons.portals.model.PortalVisualDefinition;
import panetina.elarion.core.api.AddonConfigFiles;
import panetina.elarion.core.api.ElarionApi;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PortalConfigLoader {
    private PortalConfigLoader() {
    }

    public static Loaded load(ElarionApi api) {
        Path routesFile = AddonConfigFiles.writeDefault("portals", "routes.yml", PortalConfigDefaults.ROUTES);
        Path uiFile = AddonConfigFiles.writeDefault("portals", "ui.yml", PortalConfigDefaults.UI);
        return new Loaded(loadRoutes(api, routesFile), loadUi(api, uiFile));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, PortalRouteDefinition> loadRoutes(ElarionApi api, Path file) {
        Map<String, PortalRouteDefinition> result = new LinkedHashMap<>();
        Map<String, Object> root = read(file);
        Object routesValue = root.get("routes");
        if (!(routesValue instanceof Map<?, ?> routes)) {
            throw new PortalConfigException(file + ": routes must be a mapping.");
        }
        for (Map.Entry<?, ?> entry : routes.entrySet()) {
            String id = String.valueOf(entry.getKey());
            if (!(entry.getValue() instanceof Map<?, ?> raw)) {
                throw new PortalConfigException(file + ": route " + id + " must be a mapping.");
            }
            Map<String, Object> route = (Map<String, Object>) raw;
            Map<String, Object> ticket = child(route, "ticket");
            Map<String, Object> passage = child(route, "passage");
            Map<String, Object> schedule = child(route, "schedule");
            Map<String, Object> visual = child(route, "visual");
            PortalRouteMode mode;
            try {
                mode = PortalRouteMode.parse(string(route, "mode", "scheduled_ticketed"));
            } catch (IllegalArgumentException exception) {
                throw new PortalConfigException(file + " route " + id + ": " + exception.getMessage());
            }
            PortalRouteDefinition definition = new PortalRouteDefinition(
                    id,
                    identity(api, string(route, "display-name", id)),
                    identity(api, string(route, "description", "")),
                    string(route, "source-dimension", "elarion:lobby"),
                    string(route, "destination-dimension", ""),
                    bool(route, "enabled", true),
                    mode,
                    string(ticket, "id", id),
                    identity(api, string(ticket, "display-name", id + " Ticket")),
                    identity(api, string(ticket, "lore", "")),
                    string(ticket, "price-key", mode.requiresTicket() ? "portal_ticket." + id : ""),
                    string(passage, "price-key", mode.chargesPassage() ? "ancient_gate.passage" : ""),
                    bool(passage, "first-round-trip-free", false),
                    mode.usesSchedule() ? new PortalScheduleDefinition(
                            zone(schedule, "timezone"),
                            instant(schedule, "anchor"),
                            duration(schedule, "interval"),
                            duration(schedule, "duration"),
                            durationList(schedule.get("warnings")))
                            : PortalScheduleDefinition.alwaysOpenSchedule(),
                    new PortalVisualDefinition(
                            color(visual, "color"),
                            floatValue(visual, "brightness", 1.0F),
                            floatValue(visual, "opacity", 0.82F),
                            integer(visual, "frame-time", 2),
                            string(visual, "texture", "minecraft:block/nether_portal"),
                            string(visual, "icon-item", ""),
                            optionalColor(visual, "prompt-accent-color")));
            validate(file, definition);
            if (result.put(id, definition) != null) {
                throw new PortalConfigException(file + ": duplicate route " + id);
            }
        }
        if (result.isEmpty()) throw new PortalConfigException(file + ": at least one route is required.");
        return Map.copyOf(result);
    }

    private static PortalUiConfig loadUi(ElarionApi api, Path file) {
        Map<String, Object> map = read(file);
        PortalUiConfig defaults = PortalUiConfig.defaults();
        PortalUiConfig config = new PortalUiConfig(
                string(map, "theme-variant", defaults.themeVariant()),
                integer(map, "logical-width", defaults.logicalWidth()),
                integer(map, "logical-height", defaults.logicalHeight()),
                integer(map, "minimum-scale-percent", defaults.minimumScalePercent()),
                integer(map, "confirm-button-width", defaults.confirmButtonWidth()),
                integer(map, "close-button-width", defaults.closeButtonWidth()));
        List<String> errors = new ArrayList<>();
        if (!api.uiThemes().current().variants().containsKey(config.themeVariant())) errors.add("unknown theme variant");
        if (config.logicalWidth() < 320 || config.logicalWidth() > 960) errors.add("logical-width out of range");
        if (config.logicalHeight() < 160 || config.logicalHeight() > 720) errors.add("logical-height out of range");
        if (config.minimumScalePercent() < 25 || config.minimumScalePercent() > 100) {
            errors.add("minimum-scale-percent out of range");
        }
        if (!errors.isEmpty()) throw new PortalConfigException(file + ": " + String.join("; ", errors));
        return config;
    }

    private static void validate(Path file, PortalRouteDefinition route) {
        List<String> errors = new ArrayList<>();
        if (!route.id().matches("[a-z0-9_\\-]+")) errors.add("invalid route id " + route.id());
        boolean unrestrictedDestination = "*".equals(route.destinationDimension());
        if (!unrestrictedDestination
                && net.minecraft.util.Identifier.tryParse(route.destinationDimension()) == null) {
            errors.add("invalid destination-dimension");
        }
        if (net.minecraft.util.Identifier.tryParse(route.sourceDimension()) == null) {
            errors.add("invalid source-dimension");
        }
        if (!unrestrictedDestination && route.sourceDimension().equals(route.destinationDimension())) {
            errors.add("source-dimension and destination-dimension must differ");
        }
        if (route.mode().requiresTicket()) {
            if (!route.ticketId().matches("[a-z0-9_.\\-]+")) errors.add("invalid ticket id");
            if (!route.ticketPriceKey().matches("[a-z0-9_.\\-]+")) errors.add("invalid ticket price-key");
        } else if (!route.ticketPriceKey().isBlank()) {
            errors.add("always_open routes must not define a ticket price-key");
        }
        if (route.mode().chargesPassage()) {
            if (!route.passagePriceKey().matches("[a-z0-9_.\\-]+")) errors.add("invalid passage price-key");
        } else if (!route.passagePriceKey().isBlank()) {
            errors.add("only fee_passage routes may define passage.price-key");
        }
        if (route.mode().usesSchedule()) {
            if (route.schedule().interval().isZero() || route.schedule().interval().isNegative()) {
                errors.add("schedule interval must be positive");
            }
            if (route.schedule().duration().isZero() || route.schedule().duration().isNegative()
                    || route.schedule().duration().compareTo(route.schedule().interval()) >= 0) {
                errors.add("schedule duration must be positive and shorter than interval");
            }
        }
        if (route.visual().brightness() < 0.0F || route.visual().brightness() > 2.0F) {
            errors.add("brightness must be between 0 and 2");
        }
        if (route.visual().opacity() < 0.0F || route.visual().opacity() > 1.0F) {
            errors.add("opacity must be between 0 and 1");
        }
        if (net.minecraft.util.Identifier.tryParse(route.visual().texture()) == null) {
            errors.add("invalid visual texture");
        }
        if (!route.visual().iconItem().isBlank()) {
            net.minecraft.util.Identifier icon =
                    net.minecraft.util.Identifier.tryParse(route.visual().iconItem());
            if (icon == null || !net.minecraft.registry.Registries.ITEM.containsId(icon)
                    && !PortalContent.TICKET_ID.equals(icon)
                    && !EconomyItems.CURRENCY_ID.equals(icon)) {
                errors.add("invalid visual icon-item");
            }
        }
        if (!errors.isEmpty()) throw new PortalConfigException(file + " route " + route.id()
                + ": " + String.join("; ", errors));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> read(Path file) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml().load(reader);
            if (!(loaded instanceof Map<?, ?> raw)) throw new PortalConfigException(file + " must be a mapping.");
            return (Map<String, Object>) raw;
        } catch (IOException exception) {
            throw new PortalConfigException("Failed to read " + file, exception);
        } catch (RuntimeException exception) {
            if (exception instanceof PortalConfigException config) throw config;
            throw new PortalConfigException("Invalid portal config " + file + ": " + exception.getMessage(), exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> child(Map<String, Object> parent, String key) {
        Object value = parent.get(key);
        return value instanceof Map<?, ?> raw ? (Map<String, Object>) raw : Map.of();
    }

    private static String identity(ElarionApi api, String value) {
        return api.serverIdentity().replace(value);
    }

    private static String string(Map<?, ?> map, String key, String fallback) {
        Object value = map.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private static boolean bool(Map<?, ?> map, String key, boolean fallback) {
        Object value = map.get(key);
        return value instanceof Boolean result ? result
                : value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
    }

    private static int integer(Map<?, ?> map, String key, int fallback) {
        Object value = map.get(key);
        return value instanceof Number number ? number.intValue()
                : value == null ? fallback : Integer.parseInt(String.valueOf(value));
    }

    private static long longValue(Map<?, ?> map, String key, long fallback) {
        Object value = map.get(key);
        return value instanceof Number number ? number.longValue()
                : value == null ? fallback : Long.parseLong(String.valueOf(value));
    }

    private static float floatValue(Map<?, ?> map, String key, float fallback) {
        Object value = map.get(key);
        return value instanceof Number number ? number.floatValue()
                : value == null ? fallback : Float.parseFloat(String.valueOf(value));
    }

    private static int optionalColor(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value == null || String.valueOf(value).isBlank() ? 0 : 0xFF000000 | color(map, key);
    }

    private static ZoneId zone(Map<?, ?> map, String key) {
        try {
            return ZoneId.of(string(map, key, "UTC"));
        } catch (RuntimeException exception) {
            throw new PortalConfigException("Invalid timezone " + map.get(key));
        }
    }

    private static Instant instant(Map<?, ?> map, String key) {
        try {
            return Instant.parse(string(map, key, ""));
        } catch (RuntimeException exception) {
            throw new PortalConfigException("Invalid schedule anchor " + map.get(key));
        }
    }

    private static Duration duration(Map<?, ?> map, String key) {
        return parseDuration(string(map, key, ""));
    }

    private static List<Duration> durationList(Object value) {
        if (!(value instanceof List<?> list)) return List.of();
        return list.stream().map(entry -> parseDuration(String.valueOf(entry))).toList();
    }

    static Duration parseDuration(String raw) {
        if (raw == null || raw.length() < 2) throw new PortalConfigException("Invalid duration " + raw);
        long amount;
        try {
            amount = Long.parseLong(raw.substring(0, raw.length() - 1));
        } catch (NumberFormatException exception) {
            throw new PortalConfigException("Invalid duration " + raw);
        }
        return switch (raw.charAt(raw.length() - 1)) {
            case 's' -> Duration.ofSeconds(amount);
            case 'm' -> Duration.ofMinutes(amount);
            case 'h' -> Duration.ofHours(amount);
            case 'd' -> Duration.ofDays(amount);
            default -> throw new PortalConfigException("Duration must end in s, m, h, or d: " + raw);
        };
    }

    private static int color(Map<?, ?> map, String key) {
        String raw = string(map, key, "#FFFFFF");
        if (!raw.matches("#[0-9A-Fa-f]{6}")) throw new PortalConfigException("Invalid RGB color " + raw);
        return Integer.parseInt(raw.substring(1), 16);
    }

    public record Loaded(Map<String, PortalRouteDefinition> routes, PortalUiConfig ui) {
    }
}
