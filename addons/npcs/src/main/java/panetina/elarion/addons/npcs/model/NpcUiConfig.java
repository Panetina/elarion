package panetina.elarion.addons.npcs.model;

public record NpcUiConfig(
        int panelWidth,
        int minPanelHeight,
        int maxPanelHeight,
        int minimumUiScalePercent,
        int optionRowHeight,
        int visibleOptionRows,
        int scrollbarWidth,
        int padding,
        int buttonHeight,
        int compactButtonHeight,
        int buttonGap,
        int contentGap,
        int npcRowHeight,
        int playerRowHeight,
        int optionColumnsWide,
        int portraitSize,
        int playerPortraitSize,
        boolean showPortraitReference,
        boolean showRelationBar,
        boolean showActionFeedbackInGui,
        boolean alsoSendActionFeedbackToChat,
        double defaultInteractionRangeBlocks,
        boolean typingEnabled,
        int typingCharactersPerSecond,
        boolean typingClickCompletes,
        boolean typingSoundEnabled,
        int typingSoundIntervalCharacters
) {
    public static NpcUiConfig defaults() {
        return new NpcUiConfig(
                420, 250, 340, 60, 18, 6, 6,
                16, 20, 16, 4, 8, 76, 56, 2, 64, 36,
                true, true, true, false, 6.0D,
                true, 45, true, false, 4);
    }
}
