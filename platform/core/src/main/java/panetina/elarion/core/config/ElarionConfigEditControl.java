package panetina.elarion.core.config;

import java.util.List;

public record ElarionConfigEditControl(
        ElarionConfigEditTarget target,
        String label,
        String description,
        String path,
        ElarionConfigCodec.ValueType valueType,
        String currentDisplayValue,
        String defaultDisplayValue,
        List<String> choices,
        String minimum,
        String maximum,
        boolean runtimeReloadable,
        boolean restartRequired,
        ElarionConfigPermission readPermission,
        ElarionConfigPermission writePermission,
        boolean inputEditable,
        boolean applyAvailable,
        String disabledReason,
        String applyDisabledReason
) {
    public ElarionConfigEditControl {
        if (target == null) throw new IllegalArgumentException("Config edit target is required");
        label = clean(label);
        description = clean(description);
        path = clean(path);
        valueType = valueType == null ? ElarionConfigCodec.ValueType.STRING : valueType;
        currentDisplayValue = clean(currentDisplayValue);
        defaultDisplayValue = clean(defaultDisplayValue);
        choices = choices == null ? List.of() : List.copyOf(choices);
        minimum = clean(minimum);
        maximum = clean(maximum);
        readPermission = readPermission == null ? ElarionConfigPermission.OPERATOR : readPermission;
        writePermission = writePermission == null ? ElarionConfigPermission.OPERATOR : writePermission;
        disabledReason = clean(disabledReason);
        applyDisabledReason = clean(applyDisabledReason);
    }

    public ElarionConfigEditControl(
            ElarionConfigEditTarget target,
            String label,
            String description,
            String path,
            ElarionConfigCodec.ValueType valueType,
            String currentDisplayValue,
            String defaultDisplayValue,
            List<String> choices,
            String minimum,
            String maximum,
            boolean runtimeReloadable,
            boolean restartRequired,
            ElarionConfigPermission readPermission,
            ElarionConfigPermission writePermission,
            boolean editable,
            String disabledReason
    ) {
        this(target, label, description, path, valueType, currentDisplayValue, defaultDisplayValue,
                choices, minimum, maximum, runtimeReloadable, restartRequired, readPermission,
                writePermission, editable, false, disabledReason, disabledReason);
    }

    public static ElarionConfigEditControl fromEntry(
            ElarionConfigEditTarget target,
            ElarionConfigEntry<?> entry,
            boolean editable,
            String disabledReason
    ) {
        if (entry == null) throw new IllegalArgumentException("Config entry is required");
        return new ElarionConfigEditControl(
                target,
                entry.label(),
                entry.description(),
                entry.path(),
                entry.codec().valueType(),
                entry.currentDisplayValue(),
                entry.defaultDisplayValue(),
                entry.choices(),
                entry.minimum(),
                entry.maximum(),
                entry.runtimeReloadable(),
                entry.restartRequired(),
                entry.readPermission(),
                entry.writePermission(),
                editable,
                false,
                disabledReason,
                disabledReason);
    }

    public boolean editable() {
        return inputEditable;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
