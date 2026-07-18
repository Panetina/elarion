package panetina.elarion.core.client;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
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
        int titleColorArgb,
        String leaderLabel,
        Formatting color,
        String realmName,
        String realmId,
        boolean tabVisible,
        boolean visible
) {
    private static final Identifier ICON_FONT = Identifier.of("elarion_core", "icons");
    private static final String CROWN_GLYPH = "\ue000";

    public ClientIdentity {
        username = clean(username);
        nickname = clean(nickname);
        prefix = clean(prefix);
        suffix = clean(suffix);
        title = clean(title);
        titleColorArgb = 0xFF000000 | (titleColorArgb & 0x00FFFFFF);
        leaderLabel = clean(leaderLabel);
        realmName = clean(realmName);
        realmId = clean(realmId);
    }

    public Text displayName() {
        MutableText text = Text.literal(baseName()).formatted(color);
        if (!suffix.isBlank()) text.append(Text.literal(" " + suffix));
        return text;
    }

    public Text tabName() {
        MutableText text = Text.empty();
        if (!leaderLabel.isBlank()) text.append(crown()).append(Text.literal(" "));
        text.append(displayName());
        text.append(ClientIdentityDecorations.tabSuffix(uuid));
        return text;
    }

    public Text hiddenTabName() {
        return Text.literal("Unknown Ember").formatted(Formatting.DARK_GRAY);
    }

    public Text titleText() {
        return title.isBlank() ? Text.empty()
                : Text.literal(title).styled(style -> style.withColor(TextColor.fromRgb(titleColorArgb & 0x00FFFFFF)));
    }

    public Text leaderText() {
        return leaderLabel.isBlank() ? Text.empty() : crown();
    }

    public String baseName() {
        return nickname.isBlank() ? username : nickname;
    }

    public int nameColorArgb() {
        Integer rgb = color.getColorValue();
        return 0xFF000000 | ((rgb == null ? 0xFFFFFF : rgb) & 0x00FFFFFF);
    }

    public boolean hasSimpleNickname() {
        return !nickname.isBlank() && nickname.chars().noneMatch(Character::isWhitespace);
    }

    private static String clean(String value) {
        if (value == null || value.isBlank()) return "";
        StringBuilder builder = new StringBuilder(value.length());
        value.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .filter(codePoint -> Character.getType(codePoint) != Character.FORMAT)
                .forEach(builder::appendCodePoint);
        return builder.toString().trim();
    }

    private static Text crown() {
        return Text.literal(CROWN_GLYPH)
                .styled(style -> style.withFont(ICON_FONT).withColor(Formatting.GOLD));
    }
}
