package panetina.elarion.addons.worlds.model;

public record WorldBorderDefinition(
        double centerX,
        double centerZ,
        double size,
        double safeZone,
        double damagePerBlock,
        int warningBlocks,
        int warningTime
) {
}
