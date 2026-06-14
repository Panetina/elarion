package panetina.elarion.addons.offerings.model;

public record OfferingContributionResult(
        boolean success,
        String message,
        long acceptedAmount,
        OfferingInstance instance
) {
    public static OfferingContributionResult failure(String message) {
        return new OfferingContributionResult(false, message, 0L, null);
    }

    public static OfferingContributionResult success(
            String message,
            long acceptedAmount,
            OfferingInstance instance
    ) {
        return new OfferingContributionResult(true, message, acceptedAmount, instance);
    }
}
