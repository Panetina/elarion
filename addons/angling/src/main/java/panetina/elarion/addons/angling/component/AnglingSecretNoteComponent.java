package panetina.elarion.addons.angling.component;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

/** Frozen secret-note identities from the authorized reference snapshot. */
public enum AnglingSecretNoteComponent implements StringIdentifiable {
    SAMPLE_NOTE("sample_note", "message_overworld"),
    AMETHYST_HOOK("amethyst_hook", "message_overworld"),
    ARNWULF_1("lava_proof_bottle_1", "message_overworld"),
    ARNWULF_2("lava_proof_bottle_2", "message_overworld"),
    HOPEFUL_NOTE("hopeful_note", "message_overworld"),
    HOPELESS_NOTE("hopeless_note", "message_overworld"),
    WITHER("wither_note", "message_nether"),
    TRUE_BLUE("true_blue", "message_overworld");

    public static final Codec<AnglingSecretNoteComponent> CODEC = StringIdentifiable.createCodec(
            AnglingSecretNoteComponent::values);

    private final String serializedName;
    private final String texture;

    AnglingSecretNoteComponent(String serializedName, String texture) {
        this.serializedName = serializedName;
        this.texture = texture;
    }

    @Override
    public String asString() {
        return serializedName;
    }

    public String texture() {
        return texture;
    }
}
