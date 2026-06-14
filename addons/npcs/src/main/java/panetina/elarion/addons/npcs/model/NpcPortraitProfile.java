package panetina.elarion.addons.npcs.model;

public record NpcPortraitProfile(
        String id,
        String displayName,
        String type,
        String texture,
        String playerName,
        String fallbackType,
        String fallbackTexture
) {
}
