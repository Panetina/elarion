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
            case "government_monarch", "government_president", "government_high_cleric" -> ElarionCollectionRank.SOVEREIGN;
            case "government_heir", "government_delegate" -> ElarionCollectionRank.HEIR;
            case "government_councilor" -> ElarionCollectionRank.COUNCIL;
            case "government_synod_member" -> ElarionCollectionRank.SYNOD;
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
            case "government_monarch", "government_president", "government_high_cleric",
                 "government_heir", "government_delegate", "government_councilor",
                 "government_synod_member", "government_officer", "news_reporter", "diplomat",
                 "dragon_slayer", "maze_runner", "goblin_slayer", "aquatic" ->
                    rank(titleId, ownershipMode).color();
            default -> ownershipMode == TitleOwnershipMode.GLOBALLY_UNIQUE
                    ? ElarionCollectionRank.LEGENDARY.color()
                    : SIMPLE_TITLE_COLOR;
        };
    }
}
