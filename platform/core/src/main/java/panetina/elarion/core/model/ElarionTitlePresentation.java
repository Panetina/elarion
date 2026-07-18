package panetina.elarion.core.model;

public final class ElarionTitlePresentation {
    public static final int SIMPLE_TITLE_COLOR = 0xFFFFFFFF;

    private ElarionTitlePresentation() {
    }

    public static ElarionCollectionRank rank(TitleDefinition title) {
        return rank(title.id(), title.ownershipMode());
    }

    public static ElarionCollectionRank rank(String titleId, TitleOwnershipMode ownershipMode) {
        return switch (titleId == null ? "" : titleId) {
            case "government_monarch", "government_president" -> ElarionCollectionRank.SOVEREIGN;
            case "government_heir" -> ElarionCollectionRank.HEIR;
            case "government_officer" -> ElarionCollectionRank.OFFICER;
            case "news_reporter", "diplomat" -> ElarionCollectionRank.TRUSTED;
            case "dragon_slayer", "maze_runner" -> ElarionCollectionRank.LEGENDARY;
            case "goblin_slayer", "aquatic" -> ElarionCollectionRank.RARE;
            default -> ownershipMode == TitleOwnershipMode.GLOBALLY_UNIQUE
                    ? ElarionCollectionRank.LEGENDARY
                    : ElarionCollectionRank.COMMON;
        };
    }

    public static int fallbackColor(String titleId, TitleOwnershipMode ownershipMode) {
        return switch (titleId == null ? "" : titleId) {
            case "government_monarch", "government_president",
                 "government_heir",
                 "government_officer", "news_reporter", "diplomat",
                 "dragon_slayer", "maze_runner", "goblin_slayer", "aquatic" ->
                    rank(titleId, ownershipMode).color();
            default -> ownershipMode == TitleOwnershipMode.GLOBALLY_UNIQUE
                    ? ElarionCollectionRank.LEGENDARY.color()
                    : SIMPLE_TITLE_COLOR;
        };
    }
}
