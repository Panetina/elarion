package panetina.elarion.core.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionConfigChangeValidatorTest {
    @Test
    void validatesAllowedChangeWithoutApplyingIt() {
        AtomicInteger current = new AtomicInteger(100);
        ElarionConfigRegistry registry = registry(entry(
                "font-scale",
                "ui_theme.yml.defaults.font-scale-percent",
                ElarionConfigCodec.INTEGER,
                100,
                current::get,
                ElarionConfigValidator.integerRange("font-scale", 100, 150),
                true,
                false,
                ElarionConfigPermission.OPERATOR));
        ElarionConfigChangeRequest request = request("font-scale", "125", "100");

        ElarionConfigChangeResult result = ElarionConfigChangeValidator.validate(
                registry, request, ElarionConfigPermission.OPERATOR);

        assertEquals(ElarionConfigChangeResult.Status.VALIDATED, result.status());
        assertTrue(result.success());
        assertEquals("100", result.oldDisplayValue());
        assertEquals("125", result.newDisplayValue());
        assertTrue(result.reloadRequired());
        assertFalse(result.restartRequired());
        assertEquals(100, current.get(), "validation must not apply the value");
    }

    @Test
    void rejectsUnknownDomainCategoryAndEntry() {
        ElarionConfigRegistry registry = registry(entry(
                "enabled",
                "domain.enabled",
                ElarionConfigCodec.BOOLEAN,
                true,
                () -> true,
                ElarionConfigValidator.pass(),
                true,
                false,
                ElarionConfigPermission.OPERATOR));

        assertError(ElarionConfigChangeValidator.validate(
                        registry, new ElarionConfigChangeRequest(
                                "missing", "general", "enabled", "true", "", UUID.randomUUID(), ""),
                        ElarionConfigPermission.OPERATOR),
                ElarionConfigChangeError.Code.UNKNOWN_DOMAIN);
        assertError(ElarionConfigChangeValidator.validate(
                        registry, new ElarionConfigChangeRequest(
                                "core", "missing", "enabled", "true", "", UUID.randomUUID(), ""),
                        ElarionConfigPermission.OPERATOR),
                ElarionConfigChangeError.Code.UNKNOWN_CATEGORY);
        assertError(ElarionConfigChangeValidator.validate(
                        registry, new ElarionConfigChangeRequest(
                                "core", "general", "missing", "true", "", UUID.randomUUID(), ""),
                        ElarionConfigPermission.OPERATOR),
                ElarionConfigChangeError.Code.UNKNOWN_ENTRY);
    }

    @Test
    void rejectsWhenActorPermissionIsTooLow() {
        ElarionConfigRegistry registry = registry(entry(
                "enabled",
                "domain.enabled",
                ElarionConfigCodec.BOOLEAN,
                true,
                () -> true,
                ElarionConfigValidator.pass(),
                true,
                false,
                ElarionConfigPermission.OPERATOR));

        ElarionConfigChangeResult result = ElarionConfigChangeValidator.validate(
                registry, request("enabled", "false", "true"), ElarionConfigPermission.PUBLIC);

        assertError(result, ElarionConfigChangeError.Code.PERMISSION_DENIED);
    }

    @Test
    void rejectsStaleExpectedCurrentValueBeforeParsing() {
        ElarionConfigRegistry registry = registry(entry(
                "enabled",
                "domain.enabled",
                ElarionConfigCodec.BOOLEAN,
                true,
                () -> true,
                ElarionConfigValidator.pass(),
                true,
                false,
                ElarionConfigPermission.OPERATOR));

        ElarionConfigChangeResult result = ElarionConfigChangeValidator.validate(
                registry, request("enabled", "not-a-boolean", "false"), ElarionConfigPermission.OPERATOR);

        assertError(result, ElarionConfigChangeError.Code.STALE_VALUE);
    }

    @Test
    void rejectsParseAndValidationFailures() {
        ElarionConfigRegistry registry = registry(entry(
                "font-scale",
                "ui_theme.yml.defaults.font-scale-percent",
                ElarionConfigCodec.INTEGER,
                100,
                () -> 100,
                ElarionConfigValidator.integerRange("font-scale", 100, 150),
                true,
                false,
                ElarionConfigPermission.OPERATOR));

        assertError(ElarionConfigChangeValidator.validate(
                        registry, request("font-scale", "large", "100"), ElarionConfigPermission.OPERATOR),
                ElarionConfigChangeError.Code.PARSE_FAILED);
        assertError(ElarionConfigChangeValidator.validate(
                        registry, request("font-scale", "151", "100"), ElarionConfigPermission.OPERATOR),
                ElarionConfigChangeError.Code.VALIDATION_FAILED);
    }

    @Test
    void nullRegistryOrRequestAreRejectedByContract() {
        ElarionConfigRegistry registry = registry(entry(
                "enabled",
                "domain.enabled",
                ElarionConfigCodec.BOOLEAN,
                true,
                () -> true,
                ElarionConfigValidator.pass(),
                true,
                false,
                ElarionConfigPermission.OPERATOR));

        assertThrows(NullPointerException.class, () -> ElarionConfigChangeValidator.validate(
                null, request("enabled", "true", "true"), ElarionConfigPermission.OPERATOR));
        assertThrows(NullPointerException.class, () -> ElarionConfigChangeValidator.validate(
                registry, null, ElarionConfigPermission.OPERATOR));
    }

    private static ElarionConfigRegistry registry(ElarionConfigEntry<?> entry) {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(new ElarionConfigDomain(
                "core",
                "platform:core",
                "Core",
                "Core config",
                List.of("config/elarion/core/test.yml"),
                "/e reload",
                List.of(new ElarionConfigCategory(
                        "general",
                        "General",
                        "General settings",
                        List.of(entry)))));
        return registry;
    }

    private static <T> ElarionConfigEntry<T> entry(
            String id,
            String path,
            ElarionConfigCodec<T> codec,
            T defaultValue,
            java.util.function.Supplier<T> currentValue,
            ElarionConfigValidator<T> validator,
            boolean runtimeReloadable,
            boolean restartRequired,
            ElarionConfigPermission writePermission
    ) {
        return new ElarionConfigEntry<>(
                id,
                id,
                "Test entry",
                path,
                codec,
                defaultValue,
                currentValue,
                validator,
                List.of(),
                "",
                "",
                runtimeReloadable,
                restartRequired,
                ElarionConfigPermission.OPERATOR,
                writePermission);
    }

    private static ElarionConfigChangeRequest request(String entryId, String value, String expected) {
        return new ElarionConfigChangeRequest(
                "core",
                "general",
                entryId,
                value,
                expected,
                UUID.randomUUID(),
                "test");
    }

    private static void assertError(ElarionConfigChangeResult result, ElarionConfigChangeError.Code code) {
        assertEquals(ElarionConfigChangeResult.Status.REJECTED, result.status());
        assertFalse(result.success());
        assertEquals(code, result.errors().getFirst().code());
    }
}
