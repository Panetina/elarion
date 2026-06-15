package panetina.elarion.core.client;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record ClientIdentity(
        UUID uuid,
        String username,
        String nickname,
        String prefix,
        String suffix,
        String title,
        String leaderLabel,
        Formatting color,
        String realmName,
        String realmId,
        boolean visible
) {
    private static final Identifier ICON_FONT = Identifier.of("elarion_core", "icons");
    private static final String CROWN_GLYPH = "\ue000";

    public Text displayName() {
        MutableText text = Text.literal(baseName()).formatted(color);
        if (!suffix.isBlank()) text.append(Text.literal(" " + suffix));
        return text;
    }

    public Text tabName() {
        MutableText text = Text.empty();
        if (!realmName.isBlank()) text.append(Text.literal(realmName + " ").formatted(Formatting.DARK_GRAY));
        if (!leaderLabel.isBlank()) text.append(crown()).append(Text.literal(" "));
        text.append(displayName());
        return text;
    }

    public Text hiddenTabName() {
        return Text.literal("Unknown Citizen").formatted(Formatting.DARK_GRAY);
    }

    public Text titleText() {
        return title.isBlank() ? Text.empty() : Text.literal(title);
    }

    public Text leaderText() {
        return leaderLabel.isBlank() ? Text.empty() : crown();
    }

    public String baseName() {
        return nickname.isBlank() ? username : nickname;
    }

    public boolean hasSimpleNickname() {
        return !nickname.isBlank() && nickname.chars().noneMatch(Character::isWhitespace);
    }

    private static Text crown() {
        return Text.literal(CROWN_GLYPH)
                .styled(style -> style.withFont(ICON_FONT).withColor(Formatting.GOLD));
    }
}
