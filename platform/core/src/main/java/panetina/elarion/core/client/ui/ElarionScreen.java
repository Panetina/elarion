package panetina.elarion.core.client.ui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class ElarionScreen extends Screen {
    protected ElarionScreen(Text title) {
        super(title);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void blur() {
    }

    @Override
    protected void applyBlur(float delta) {
    }
}
