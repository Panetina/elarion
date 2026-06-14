package panetina.elarion.addons.npcs.model;

public record NpcSkinProfile(
        String id,
        String displayName,
        String type,
        String texture,
        String playerName,
        String fallbackType,
        String fallbackTexture,
        String adapter
) {
}
