package panetina.elarion.core.registry;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RegistryExecutionContextTest {
    @Test
    void contextNormalizesNullableStringsAndMetadata() {
        RegistryExecutionContext context = new RegistryExecutionContext(
                newApiPlaceholder(), null, null, null,
                null, null, null, null, null, null);

        assertEquals("", context.actorRealmId());
        assertEquals("", context.targetRealmId());
        assertEquals("", context.worldId());
        assertEquals("elarion_core", context.sourceAddon());
        assertTrue(context.metadata().isEmpty());
    }

    @Test
    void resultCopiesServerTasks() {
        RegistryExecutionResult result = new RegistryExecutionResult(true, null, List.of())
                .withServerTask(() -> {});

        assertTrue(result.success());
        assertEquals("", result.message());
        assertEquals(1, result.serverTasks().size());
        assertFalse(RegistryExecutionResult.failure("no").success());
    }

    @Test
    void actionContextCopiesParameters() {
        RegistryExecutionContext execution = RegistryExecutionContext.server(
                newApiPlaceholder(), null, "test");
        ActionContext context = new ActionContext(execution, "run_reward", Map.of("reward", "welcome"));

        assertEquals("run_reward", context.actionId());
        assertEquals("welcome", context.parameters().get("reward"));
    }

    @Test
    void builtInCloseActionExecutes() {
        ElarionRegistries registries = new ElarionRegistries();
        RegistryExecutionResult result = registries.execute(new ActionContext(
                RegistryExecutionContext.server(newApiPlaceholder(), null, "test"),
                "close",
                Map.of()));

        assertTrue(result.success());
    }

    @Test
    void unknownActionHandlerFailsWithoutThrowing() {
        ElarionRegistries registries = new ElarionRegistries();
        RegistryExecutionResult result = registries.execute(new ActionContext(
                RegistryExecutionContext.server(newApiPlaceholder(), null, "test"),
                "missing",
                Map.of()));

        assertFalse(result.success());
    }

    private static panetina.elarion.core.api.ElarionApi newApiPlaceholder() {
        return null;
    }
}
