package panetina.elarion.core.client.ui;

public final class ElarionFooterActionLayout {
    private ElarionFooterActionLayout() {
    }

    public static Action action(int x, int y, int width, int height) {
        return new Action(new ElarionSemanticRowLayout.Rect(
                x, y, Math.max(1, width), Math.max(1, height)
        ));
    }

    public record Action(ElarionSemanticRowLayout.Rect button) {
    }
}
