package panetina.elarion.addons.offerings.model;

import java.util.Map;

public record OfferingMilestone(
        String id,
        String type,
        Map<String, String> parameters
) {
    public OfferingMilestone {
        id = id == null || id.isBlank() ? type : id;
        type = type == null ? "" : type;
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
