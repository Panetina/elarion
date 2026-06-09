package panetina.elarion.core.client;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

public record ClientIdentity(
        UUID uuid,
        String username,
        String nickname,
        String prefix,
        String suffix,
        String title,
        Formatting color,
        String realmId,
        boolean visible
) {
    public Text displayName() {
        MutableText text = Text.literal(baseName()).formatted(color);
        if (!suffix.isBlank()) text.append(Text.literal(" " + suffix));
        return text;
    }

    public Text tabName() {
        return displayName();
    }

    public Text titleText() {
        return title.isBlank() ? Text.empty() : Text.literal(title);
    }

    public String baseName() {
        return nickname.isBlank() ? username : nickname;
    }

    public boolean hasSimpleNickname() {
        return !nickname.isBlank() && nickname.chars().noneMatch(Character::isWhitespace);
    }
}
