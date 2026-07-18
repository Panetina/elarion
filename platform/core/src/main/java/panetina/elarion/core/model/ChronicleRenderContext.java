package panetina.elarion.core.model;

public record ChronicleRenderContext(String actorName) {
    public static final ChronicleRenderContext EMPTY = new ChronicleRenderContext("");

    public ChronicleRenderContext {
        actorName = actorName == null ? "" : actorName.trim();
    }
}
