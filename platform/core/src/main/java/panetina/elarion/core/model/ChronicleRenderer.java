package panetina.elarion.core.model;

public interface ChronicleRenderer {
    boolean supports(PublicHistoryEntry entry);

    ChronicleProjection render(PublicHistoryEntry entry, ChronicleRenderContext context);
}
