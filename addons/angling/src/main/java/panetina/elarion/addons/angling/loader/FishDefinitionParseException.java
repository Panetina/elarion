package panetina.elarion.addons.angling.loader;

public final class FishDefinitionParseException extends IllegalArgumentException {
    private final String documentId;
    private final String fieldPath;

    public FishDefinitionParseException(String documentId, String fieldPath, String message) {
        super(format(documentId, fieldPath, message));
        this.documentId = documentId;
        this.fieldPath = fieldPath;
    }

    public String documentId() {
        return documentId;
    }

    public String fieldPath() {
        return fieldPath;
    }

    private static String format(String documentId, String fieldPath, String message) {
        return "Fish definition " + documentId + " field " + fieldPath + ": " + message;
    }
}
