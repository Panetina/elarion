package panetina.elarion.core.storage;

public final class CatchTelemetryFormatException extends IllegalArgumentException {
    public CatchTelemetryFormatException(String message) {
        super(message);
    }

    public CatchTelemetryFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
