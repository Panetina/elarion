package panetina.elarion.addons.offerings.model;

public record OfferingPresentation(String levelText, String icon) {
    public OfferingPresentation {
        levelText = levelText == null || levelText.isBlank() ? "Foundation I" : levelText;
        icon = icon == null || icon.isBlank()
                ? "minecraft:textures/item/amethyst_shard.png"
                : icon;
    }

    public static OfferingPresentation defaults() {
        return new OfferingPresentation("Foundation I", "minecraft:textures/item/amethyst_shard.png");
    }
}
