package panetina.elarion.addons.optimization;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.service.ElarionTaskConfig;
import panetina.elarion.core.service.ElarionTaskService;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PerformanceConfigDescriptorsTest {
    @Test
    void registersCoreOwnedRestartRequiredPerformanceSettings() {
        ElarionTaskService tasks = new ElarionTaskService(
                LoggerFactory.getLogger("test"), ElarionTaskConfig.Settings.defaults(false));
        try {
            ElarionConfigRegistry registry = new ElarionConfigRegistry();
            PerformanceConfigDescriptors.register(registry, tasks::snapshot);

            ElarionConfigDomain domain = registry.domain("optimization").orElseThrow();
            assertEquals("platform:core", domain.ownerModule());
            assertEquals("", domain.reloadCommand());
            assertEquals(Set.of("host", "task-budgets", "monitoring"), domain.categories().stream()
                    .map(category -> category.id()).collect(Collectors.toSet()));
            assertEquals(16, domain.categories().stream().mapToInt(category -> category.entries().size()).sum());
            assertEquals("unknown_online_host",
                    domain.entry("host", "hardware-profile").orElseThrow().currentValue());
            assertEquals(1, domain.entry("task-budgets", "io-workers").orElseThrow().currentValue());
            assertEquals("2.0",
                    domain.entry("task-budgets", "max-server-apply-millis").orElseThrow().currentValue());
            assertEquals("45.0",
                    domain.entry("monitoring", "tick-warning-millis").orElseThrow().currentValue());

            for (var category : domain.categories()) {
                for (ElarionConfigEntry<?> entry : category.entries()) {
                    assertFalse(entry.runtimeReloadable());
                    assertTrue(entry.restartRequired());
                    assertTrue(entry.validateCurrent().isEmpty(), entry.path());
                }
            }
        } finally {
            tasks.shutdown();
        }
    }

    @Test
    void decimalThresholdsRemainBoundedReadOnlyStrings() {
        ElarionTaskService tasks = new ElarionTaskService(
                LoggerFactory.getLogger("test"), ElarionTaskConfig.Settings.defaults(false));
        try {
            ElarionConfigDomain domain = PerformanceConfigDescriptors.domain(tasks::snapshot);
            ElarionConfigEntry<?> entry = domain.entry(
                    "monitoring", "headroom-overloaded-mspt").orElseThrow();

            assertEquals("string", entry.codec().id());
            assertEquals("0.0", entry.minimum());
            assertEquals("50.0", entry.currentValue());
        } finally {
            tasks.shutdown();
        }
    }
}
