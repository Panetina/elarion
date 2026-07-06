package panetina.elarion.core.config;

public record ElarionConfigChangeError(
        Code code,
        String path,
        String message
) {
    public ElarionConfigChangeError {
        code = code == null ? Code.INTERNAL_ERROR : code;
        path = path == null ? "" : path.trim();
        message = message == null || message.isBlank() ? code.defaultMessage() : message.trim();
    }

    public static ElarionConfigChangeError of(Code code, String path, String message) {
        return new ElarionConfigChangeError(code, path, message);
    }

    public enum Code {
        UNKNOWN_DOMAIN("Unknown config domain."),
        UNKNOWN_CATEGORY("Unknown config category."),
        UNKNOWN_ENTRY("Unknown config entry."),
        PERMISSION_DENIED("Permission denied."),
        STALE_VALUE("The config value changed before this request was applied."),
        PARSE_FAILED("The submitted value could not be parsed."),
        VALIDATION_FAILED("The submitted value failed validation."),
        UNSUPPORTED("This config entry does not support editing."),
        RELOAD_REQUIRED("A reload is required before this change can apply."),
        RESTART_REQUIRED("A restart is required before this change can apply."),
        APPLY_FAILED("The config change could not be applied."),
        INTERNAL_ERROR("Config change failed.");

        private final String defaultMessage;

        Code(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public String defaultMessage() {
            return defaultMessage;
        }
    }
}
