package panetina.elarion.addons.angling.model;

import net.minecraft.util.Identifier;

public enum AnglingRarity {
    PLACEHOLDER_COMMON("placeholder_common"),
    PLACEHOLDER_UNCOMMON("placeholder_uncommon"),
    PLACEHOLDER_RARE("placeholder_rare"),
    PLACEHOLDER_EPIC("placeholder_epic");

    private final Identifier id;

    AnglingRarity(String path) {
        this.id = Identifier.of("elarion_angling", path);
    }

    public Identifier id() {
        return id;
    }
}
