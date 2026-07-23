package panetina.elarion.core.storage;

public final class MetricPersistenceFormatException extends IllegalArgumentException {
    public MetricPersistenceFormatException(String message) {
        super(message);
    }

    public MetricPersistenceFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
