package panetina.elarion.addons.offerings.model;

public record OfferingRequirement(
        String type,
        String id,
        long count
) {
    public OfferingRequirement {
        type = type == null ? "" : type;
        id = id == null ? "" : id;
        if (count < 1) throw new IllegalArgumentException("requirement count must be positive");
    }

    public String key() {
        return switch (type) {
            case "items" -> "item:" + id;
            case "currency" -> "currency";
            case "events" -> "event:" + id;
            default -> type + ":" + id;
        };
    }
}
