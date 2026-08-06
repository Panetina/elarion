package panetina.elarion.core.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class CoreConfigHistorySupportTest {
    @Test
    void emptyChronicleCategoryConfigurationUsesEveryCurrentPromotedFallbackCategory() {
        CoreConfigHistorySupport.Settings settings = CoreConfigHistorySupport.load(
                Map.of("archive", Map.of("chronicle-categories", List.of())),
                CoreConfigHistorySupport.DEFAULT_CHRONICLE_CATEGORIES);

        assertTrue(settings.chroniclePolicy().allows("underworld", "true-death"));
        assertTrue(settings.chroniclePolicy().allows("portal", "route-unlocked"));
        assertTrue(settings.chroniclePolicy().allows("offering", "project-completed"));
    }
}
