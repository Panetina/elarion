package panetina.elarion.core.model;

import java.util.List;

public record ElarionNotificationRewardPreview(
        String label,
        String icon,
        int count,
        List<String> tooltipLines
) {
    public ElarionNotificationRewardPreview(String label, String icon, int count) {
        this(label, icon, count, List.of());
    }

    public ElarionNotificationRewardPreview {
        label = label == null ? "" : label;
        icon = icon == null ? "" : icon;
        count = Math.max(0, count);
        tooltipLines = tooltipLines == null ? List.of() : tooltipLines.stream()
                .filter(line -> line != null && !line.isBlank())
                .map(String::trim)
                .limit(8)
                .toList();
    }
}
