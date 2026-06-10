package panetina.elarion.core.model;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public record PlayerIdentity(
        Text displayName,
        Text chatName,
        Text tabName,
        Text titleText,
        Text leaderText,
        String prefix,
        String suffix,
        Formatting color,
        VisibilityScope visibilityScope
) {
}
