package panetina.elarion.core.client.ui;

import java.util.ArrayList;
import java.util.List;

public final class ElarionPresetButtonRowLayout {
    private ElarionPresetButtonRowLayout() {
    }

    public static Row row(int x, int y, int buttonWidth, int buttonHeight, int gap, int count) {
        int safeCount = Math.max(0, count);
        int safeWidth = Math.max(1, buttonWidth);
        int safeHeight = Math.max(1, buttonHeight);
        int safeGap = Math.max(0, gap);
        List<ElarionSemanticRowLayout.Rect> buttons = new ArrayList<>(safeCount);
        for (int index = 0; index < safeCount; index++) {
            buttons.add(new ElarionSemanticRowLayout.Rect(
                    x + index * (safeWidth + safeGap), y, safeWidth, safeHeight
            ));
        }
        int boundsWidth = safeCount == 0 ? 0 : safeCount * safeWidth + (safeCount - 1) * safeGap;
        return new Row(List.copyOf(buttons), new ElarionSemanticRowLayout.Rect(x, y, boundsWidth, safeHeight));
    }

    public static PresetConfirmRow presetConfirmRow(
            int x,
            int y,
            int buttonWidth,
            int buttonHeight,
            int gap,
            int count,
            int confirmGap,
            int confirmWidth
    ) {
        Row presets = row(x, y, buttonWidth, buttonHeight, gap, count);
        int safeConfirmGap = Math.max(0, confirmGap);
        int safeConfirmWidth = Math.max(1, confirmWidth);
        int confirmX = presets.bounds().x() + presets.bounds().width() + safeConfirmGap;
        ElarionSemanticRowLayout.Rect confirm =
                new ElarionSemanticRowLayout.Rect(confirmX, y, safeConfirmWidth, Math.max(1, buttonHeight));
        int boundsWidth = presets.bounds().width() + safeConfirmGap + safeConfirmWidth;
        return new PresetConfirmRow(presets, confirm,
                new ElarionSemanticRowLayout.Rect(x, y, boundsWidth, Math.max(1, buttonHeight)));
    }

    public record Row(List<ElarionSemanticRowLayout.Rect> buttons, ElarionSemanticRowLayout.Rect bounds) {
        public ElarionSemanticRowLayout.Rect button(int index) {
            return index < 0 || index >= buttons.size()
                    ? new ElarionSemanticRowLayout.Rect(bounds.x(), bounds.y(), 1, bounds.height())
                    : buttons.get(index);
        }

        public int hitIndex(double x, double y) {
            for (int index = 0; index < buttons.size(); index++) {
                if (buttons.get(index).contains(x, y)) return index;
            }
            return -1;
        }
    }

    public record PresetConfirmRow(
            Row presets,
            ElarionSemanticRowLayout.Rect confirm,
            ElarionSemanticRowLayout.Rect bounds
    ) {
    }
}
