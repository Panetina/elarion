package panetina.elarion.addons.economy.config;

import org.yaml.snakeyaml.Yaml;
import panetina.elarion.addons.economy.model.EconomyServicePrice;
import panetina.elarion.core.api.AddonConfigFiles;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EconomyServicePriceConfig {
    private static final String DEFAULT_CONFIG = """
            # Economy-owned bounded service prices. Gameplay addons reference these
            # stable IDs but do not own or cache the resulting price.
            prices:
              portal_ticket.nether:
                base: 25
                minimum: 15
                maximum: 60
              portal_ticket.end:
                base: 150
                minimum: 100
                maximum: 400
              ancient_gate.passage:
                base: 5
                minimum: 1
                maximum: 15
            """;

    private EconomyServicePriceConfig() {
    }

    public static Map<String, EconomyServicePrice> load() {
        Path path = AddonConfigFiles.writeDefault("economy", "service_prices.yml", DEFAULT_CONFIG);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml().load(reader);
            Map<?, ?> root = loaded instanceof Map<?, ?> map ? map : Map.of();
            if (!(root.get("prices") instanceof Map<?, ?> rawPrices)) {
                throw new IllegalStateException(path + ": prices must be a mapping");
            }
            Map<String, EconomyServicePrice> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : rawPrices.entrySet()) {
                String id = String.valueOf(entry.getKey());
                if (!(entry.getValue() instanceof Map<?, ?> values)) {
                    throw new IllegalStateException(path + ": price " + id + " must be a mapping");
                }
                long base = number(values.get("base"), -1);
                long minimum = number(values.get("minimum"), base);
                long maximum = number(values.get("maximum"), base);
                EconomyServicePrice definition = new EconomyServicePrice(id, base, minimum, maximum);
                if (result.put(id, definition) != null) {
                    throw new IllegalStateException(path + ": duplicate price " + id);
                }
            }
            return Map.copyOf(result);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load Economy service prices " + path, exception);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Invalid Economy service prices " + path + ": "
                    + exception.getMessage(), exception);
        }
    }

    private static long number(Object value, long fallback) {
        return value instanceof Number number ? number.longValue() : fallback;
    }
}
