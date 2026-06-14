package panetina.elarion.addons.offerings.model;

public record OfferingUiConfig(
        String themeVariant,
        int logicalWidth,
        int logicalHeight,
        int minimumScalePercent,
        int summaryWidth,
        int tabHeight,
        int rowHeight,
        int iconSize,
        int closeButtonWidth,
        String rewardsPlaceholder,
        String historyPlaceholder,
        String contributionPlaceholder,
        String eventTitle,
        String eventBody,
        String eventLockedBody
) {
    public static OfferingUiConfig defaults() {
        return new OfferingUiConfig(
                "shrine", 520, 360, 60, 150, 20, 24, 48, 92,
                "Rewards will appear here when milestones are configured.",
                "No recent offerings recorded.",
                "Select an incomplete item or currency requirement to make an offering.",
                "Upcoming Event", "No shrine event active.", "Events system not unlocked yet.");
    }
}
