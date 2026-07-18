package panetina.elarion.core.client.ui;

public final class ElarionModalLayout {
    private ElarionModalLayout() {
    }

    public static TwoButtonModal twoButtonModal(int parentWidth, int parentHeight, Spec spec) {
        Spec safe = spec.sanitized();
        int x = (Math.max(1, parentWidth) - safe.width()) / 2;
        int y = (Math.max(1, parentHeight) - safe.height()) / 2;
        int bodyX = x + safe.bodyInset();
        int bodyY = y + safe.bodyTopOffset();
        int bodyWidth = Math.max(1, safe.width() - safe.bodyInset() * 2);
        int inputX = bodyX;
        int inputY = bodyY + safe.bodyHeight() + safe.inputGap();
        int inputWidth = bodyWidth;
        int buttonY = y + safe.height() - safe.buttonBottomInset();
        int totalButtonWidth = safe.buttonWidth() * 2 + safe.buttonGap();
        int cancelX = x + (safe.width() - totalButtonWidth) / 2;
        int submitX = cancelX + safe.buttonWidth() + safe.buttonGap();
        return new TwoButtonModal(
                x,
                y,
                safe.width(),
                safe.height(),
                bodyX,
                bodyY,
                bodyWidth,
                safe.bodyHeight(),
                inputX,
                inputY,
                inputWidth,
                cancelX,
                submitX,
                buttonY,
                safe.buttonWidth(),
                safe.buttonHeight()
        );
    }

    public record Spec(
            int width,
            int height,
            int bodyInset,
            int bodyTopOffset,
            int bodyHeight,
            int inputGap,
            int buttonBottomInset,
            int buttonWidth,
            int buttonHeight,
            int buttonGap
    ) {
        private Spec sanitized() {
            return new Spec(
                    Math.max(1, width),
                    Math.max(1, height),
                    Math.max(0, bodyInset),
                    Math.max(0, bodyTopOffset),
                    Math.max(1, bodyHeight),
                    Math.max(0, inputGap),
                    Math.max(1, buttonBottomInset),
                    Math.max(1, buttonWidth),
                    Math.max(1, buttonHeight),
                    Math.max(0, buttonGap)
            );
        }
    }

    public record TwoButtonModal(
            int x,
            int y,
            int width,
            int height,
            int bodyX,
            int bodyY,
            int bodyWidth,
            int bodyHeight,
            int inputX,
            int inputY,
            int inputWidth,
            int cancelX,
            int submitX,
            int buttonY,
            int buttonWidth,
            int buttonHeight
    ) {
        public int bottom() {
            return y + height;
        }

        public int buttonBottom() {
            return buttonY + buttonHeight;
        }
    }
}
