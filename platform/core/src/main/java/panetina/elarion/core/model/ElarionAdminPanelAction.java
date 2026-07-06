package panetina.elarion.core.model;

public record ElarionAdminPanelAction(
        String providerId,
        String id,
        String label,
        String style,
        boolean enabled,
        boolean requiresConfirmation,
        String parameterKey,
        String parameterLabel,
        String parameterPlaceholder,
        java.util.List<String> parameterSuggestions,
        String confirmationTitle,
        String confirmationBody
) {
    public ElarionAdminPanelAction {
        providerId = clean(providerId);
        id = clean(id);
        label = clean(label);
        style = clean(style).isBlank() ? "normal" : clean(style);
        parameterKey = clean(parameterKey);
        parameterLabel = clean(parameterLabel);
        parameterPlaceholder = clean(parameterPlaceholder);
        parameterSuggestions = parameterSuggestions == null ? java.util.List.of()
                : parameterSuggestions.stream()
                .map(ElarionAdminPanelAction::clean)
                .filter(value -> !value.isBlank())
                .distinct()
                .limit(128)
                .toList();
        confirmationTitle = clean(confirmationTitle);
        confirmationBody = clean(confirmationBody);
    }

    public static ElarionAdminPanelAction normal(String providerId, String id, String label) {
        return new ElarionAdminPanelAction(providerId, id, label, "normal", true, false,
                "", "", "", java.util.List.of(), "", "");
    }

    public static ElarionAdminPanelAction input(
            String providerId, String id, String label, String parameterKey,
            String parameterLabel, String parameterPlaceholder
    ) {
        return new ElarionAdminPanelAction(providerId, id, label, "normal", true, false,
                parameterKey, parameterLabel, parameterPlaceholder, java.util.List.of(), "", "");
    }

    public static ElarionAdminPanelAction input(
            String providerId, String id, String label, String parameterKey,
            String parameterLabel, String parameterPlaceholder, java.util.List<String> parameterSuggestions
    ) {
        return new ElarionAdminPanelAction(providerId, id, label, "normal", true, false,
                parameterKey, parameterLabel, parameterPlaceholder, parameterSuggestions, "", "");
    }

    public static ElarionAdminPanelAction danger(
            String providerId, String id, String label, String title, String body
    ) {
        return new ElarionAdminPanelAction(providerId, id, label, "danger", true, true,
                "", "", "", java.util.List.of(), title, body);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
