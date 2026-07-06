package panetina.elarion.core.config;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

public record ElarionConfigEntry<T>(
        String id,
        String label,
        String description,
        String path,
        ElarionConfigCodec<T> codec,
        T defaultValue,
        Supplier<T> currentValueSupplier,
        ElarionConfigValidator<T> validator,
        List<String> choices,
        String minimum,
        String maximum,
        boolean runtimeReloadable,
        boolean restartRequired,
        ElarionConfigPermission readPermission,
        ElarionConfigPermission writePermission
) {
    public ElarionConfigEntry {
        id = normalizeId(id, "Config entry id");
        label = label == null || label.isBlank() ? id : label.trim();
        description = description == null ? "" : description.trim();
        path = path == null || path.isBlank() ? id : path.trim();
        codec = Objects.requireNonNull(codec, "Config entry codec is required");
        currentValueSupplier = Objects.requireNonNull(currentValueSupplier, "Current value supplier is required");
        validator = validator == null ? ElarionConfigValidator.pass() : validator;
        choices = choices == null ? List.of() : List.copyOf(choices);
        minimum = minimum == null ? "" : minimum.trim();
        maximum = maximum == null ? "" : maximum.trim();
        readPermission = readPermission == null ? ElarionConfigPermission.OPERATOR : readPermission;
        writePermission = writePermission == null ? ElarionConfigPermission.OPERATOR : writePermission;
    }

    public T currentValue() {
        return currentValueSupplier.get();
    }

    public String currentDisplayValue() {
        return codec.format(currentValue());
    }

    public String defaultDisplayValue() {
        return codec.format(defaultValue);
    }

    public List<String> validateCurrent() {
        return validator.validate(currentValue());
    }

    static String normalizeId(String value, String label) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) throw new IllegalArgumentException(label + " is required");
        if (!normalized.matches("[a-z0-9_.:-]+")) {
            throw new IllegalArgumentException(label + " may contain lowercase letters, numbers, _, ., :, or - only: "
                    + value);
        }
        return normalized;
    }
}

