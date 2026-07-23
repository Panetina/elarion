package panetina.elarion.addons.angling.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Persisted player-letter value with the reference screen's hard limits enforced at decode time. */
public record AnglingLetterMessageComponent(
        UUID sender,
        String senderDisplayName,
        Identifier dimension,
        List<String> text,
        boolean locked
) {
    public static final int MAX_SENDER_NAME_LENGTH = 17;
    public static final int MAX_LINES = 15;
    public static final int MAX_LINE_LENGTH = 40;

    private static final Codec<String> SENDER_NAME_CODEC = Codec.STRING.validate(value ->
            value.length() <= MAX_SENDER_NAME_LENGTH
                    ? DataResult.success(value)
                    : DataResult.error(() -> "sender_display_name exceeds " + MAX_SENDER_NAME_LENGTH + " characters"));
    private static final Codec<String> LINE_CODEC = Codec.STRING.validate(value ->
            value.length() <= MAX_LINE_LENGTH
                    ? DataResult.success(value)
                    : DataResult.error(() -> "message line exceeds " + MAX_LINE_LENGTH + " characters"));
    private static final Codec<List<String>> TEXT_CODEC = LINE_CODEC.listOf().validate(value ->
            value.size() <= MAX_LINES
                    ? DataResult.success(value)
                    : DataResult.error(() -> "message exceeds " + MAX_LINES + " lines"));

    public static final Codec<AnglingLetterMessageComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("sender").forGetter(AnglingLetterMessageComponent::sender),
            SENDER_NAME_CODEC.fieldOf("sender_display_name").forGetter(AnglingLetterMessageComponent::senderDisplayName),
            Identifier.CODEC.fieldOf("dimension").forGetter(AnglingLetterMessageComponent::dimension),
            TEXT_CODEC.fieldOf("text").forGetter(AnglingLetterMessageComponent::text),
            Codec.BOOL.fieldOf("locked").forGetter(AnglingLetterMessageComponent::locked)
    ).apply(instance, AnglingLetterMessageComponent::new));

    public AnglingLetterMessageComponent {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(senderDisplayName, "senderDisplayName");
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(text, "text");
        if (senderDisplayName.length() > MAX_SENDER_NAME_LENGTH || text.size() > MAX_LINES
                || text.stream().anyMatch(line -> line == null || line.length() > MAX_LINE_LENGTH)) {
            throw new IllegalArgumentException("Letter message exceeds bounded display limits");
        }
        text = List.copyOf(text);
    }

    public AnglingLetterMessageComponent lock() {
        return locked ? this : new AnglingLetterMessageComponent(sender, senderDisplayName, dimension, text, true);
    }
}
