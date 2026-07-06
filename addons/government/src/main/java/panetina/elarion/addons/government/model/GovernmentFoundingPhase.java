package panetina.elarion.addons.government.model;

public record GovernmentFoundingPhase(
        String officeId,
        String title,
        String phaseLabel,
        boolean nominationsOpen,
        boolean votingOpen,
        boolean canNominate,
        String nominationReason
) {
    public GovernmentFoundingPhase {
        officeId = clean(officeId);
        title = clean(title);
        phaseLabel = clean(phaseLabel);
        nominationReason = clean(nominationReason);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
