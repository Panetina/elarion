package panetina.elarion.core.placeholder;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlaceholderResolutionEngineTest {
    @Test
    void identityAliasesPreserveExistingSyntaxAndTransforms() {
        ElarionPlaceholderService service = new ElarionPlaceholderService(
                panetina.elarion.core.model.ServerIdentityConfig.defaults());

        PlaceholderResolution result = service.resolve("%server% %realm_term_upper% %missing%",
                PlaceholderResolutionContext.publicContext(PlaceholderRenderContext.UI, Map.of()));

        assertEquals("Elarion REALM %missing%", result.text());
        assertTrue(result.diagnostics().stream().anyMatch(value -> value.code().equals("unknown")));
    }

    @Test
    void requestMemoizationCallsResolverOnce() {
        ElarionPlaceholderRegistry registry = new ElarionPlaceholderRegistry();
        AtomicInteger calls = new AtomicInteger();
        registry.register(publicDescriptor("test.value"), ignored -> {
            calls.incrementAndGet();
            return "ok";
        });

        PlaceholderResolution result = new PlaceholderResolutionEngine(registry).resolve(
                "%test.value%/{test.value}", context(), PlaceholderResolutionLimits.DEFAULTS);

        assertEquals("ok/ok", result.text());
        assertEquals(1, calls.get());
    }

    @Test
    void cyclesAndBoundsPreserveTokensSafely() {
        ElarionPlaceholderRegistry registry = new ElarionPlaceholderRegistry();
        registry.register(publicDescriptor("test.cycle"), ignored -> "%test.cycle%");
        PlaceholderResolutionEngine engine = new PlaceholderResolutionEngine(registry);

        PlaceholderResolution cycle = engine.resolve("%test.cycle%", context(),
                PlaceholderResolutionLimits.DEFAULTS);
        PlaceholderResolution count = engine.resolve("%test.cycle% %test.cycle%", context(),
                new PlaceholderResolutionLimits(1, 64, 4, 4));

        assertEquals("%test.cycle%", cycle.text());
        assertTrue(cycle.diagnostics().stream().anyMatch(value -> value.code().equals("cycle")));
        assertTrue(count.diagnostics().stream().anyMatch(value -> value.code().equals("count")));
    }

    @Test
    void visibilityAndRequiredContextAreServerAuthoritative() {
        ElarionPlaceholderRegistry registry = new ElarionPlaceholderRegistry();
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("test.private", "test", "Private value",
                PlaceholderValueType.STRING, Set.of(PlaceholderRenderContext.UI), Set.of("secret"),
                PlaceholderVisibility.SELF, PlaceholderFailureBehavior.PRESERVE_TOKEN,
                PlaceholderFailureBehavior.EMPTY);
        registry.register(descriptor, value -> value.value("secret"));
        PlaceholderResolutionEngine engine = new PlaceholderResolutionEngine(registry);
        UUID subject = UUID.randomUUID();

        PlaceholderResolution denied = engine.resolve("x%test.private%y",
                new PlaceholderResolutionContext(PlaceholderRenderContext.UI, UUID.randomUUID(), subject,
                        "", "", false, Map.of("secret", "hidden")), PlaceholderResolutionLimits.DEFAULTS);
        PlaceholderResolution allowed = engine.resolve("%test.private%",
                new PlaceholderResolutionContext(PlaceholderRenderContext.UI, subject, subject,
                        "", "", false, Map.of("secret", "visible")), PlaceholderResolutionLimits.DEFAULTS);

        assertEquals("xy", denied.text());
        assertEquals("visible", allowed.text());
    }

    @Test
    void duplicateRegistrationAndUnknownAliasTargetAreRejected() {
        ElarionPlaceholderRegistry registry = new ElarionPlaceholderRegistry();
        registry.register(publicDescriptor("test.value"), ignored -> "ok");
        assertThrows(IllegalStateException.class,
                () -> registry.register(publicDescriptor("test.value"), ignored -> "again"));
        assertThrows(IllegalArgumentException.class,
                () -> registry.registerAlias(new PlaceholderAlias("old", "test.missing",
                        PlaceholderTransform.IDENTITY, true)));
    }

    private static PlaceholderDescriptor publicDescriptor(String id) {
        return PlaceholderDescriptor.publicString(id, "test", "Test", Set.of(PlaceholderRenderContext.UI));
    }

    private static PlaceholderResolutionContext context() {
        return PlaceholderResolutionContext.publicContext(PlaceholderRenderContext.UI, Map.of());
    }
}
