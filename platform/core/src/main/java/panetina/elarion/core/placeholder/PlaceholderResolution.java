package panetina.elarion.core.placeholder;

import java.util.List;

public record PlaceholderResolution(String text, int resolvedCount, boolean truncated,
                                    List<PlaceholderDiagnostic> diagnostics) {
    public PlaceholderResolution {
        text = text == null ? "" : text;
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
    }
}
