package panetina.elarion.core.client.ui;

public record ElarionUiCard(
        String id,
        String label,
        String icon,
        long count,
        long currencyAmount,
        boolean disabled
) {
    public ElarionUiCard {
        id = id == null ? "" : id;
        label = label == null ? "" : label;
        icon = icon == null ? "" : icon;
    }
}
