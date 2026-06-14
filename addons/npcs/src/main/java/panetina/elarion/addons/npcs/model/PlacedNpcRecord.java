package panetina.elarion.addons.npcs.model;

import java.util.UUID;

public record PlacedNpcRecord(
        UUID id,
        String handle,
        String definitionId,
        UUID entityId,
        String worldId,
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        String displayNameOverride,
        String skinOverride,
        String portraitOverride,
        String dialogueOverride,
        UUID createdBy,
        long createdAt
) {
    public String displayName(NpcDefinition definition) {
        return displayNameOverride == null || displayNameOverride.isBlank()
                ? definition.displayName()
                : displayNameOverride;
    }

    public String skin(NpcDefinition definition) {
        return skinOverride == null || skinOverride.isBlank() ? definition.skin() : skinOverride;
    }

    public String portrait(NpcDefinition definition) {
        return portraitOverride == null || portraitOverride.isBlank() ? definition.portrait() : portraitOverride;
    }

    public String dialogue(NpcDefinition definition) {
        return dialogueOverride == null || dialogueOverride.isBlank() ? definition.dialogue() : dialogueOverride;
    }

    public String commandId() {
        return handle == null || handle.isBlank() ? id.toString() : handle;
    }

    public PlacedNpcRecord withEntity(UUID entityId) {
        return new PlacedNpcRecord(
                id, handle, definitionId, entityId, worldId, x, y, z, yaw, pitch, displayNameOverride, skinOverride,
                portraitOverride, dialogueOverride, createdBy, createdAt);
    }

    public PlacedNpcRecord moved(String worldId, double x, double y, double z, float yaw, float pitch) {
        return new PlacedNpcRecord(
                id, handle, definitionId, entityId, worldId, x, y, z, yaw, pitch, displayNameOverride, skinOverride,
                portraitOverride, dialogueOverride, createdBy, createdAt);
    }

    public PlacedNpcRecord facing(float yaw) {
        return new PlacedNpcRecord(
                id, handle, definitionId, entityId, worldId, x, y, z, yaw, 0.0F, displayNameOverride, skinOverride,
                portraitOverride, dialogueOverride, createdBy, createdAt);
    }

    public PlacedNpcRecord named(String name) {
        return new PlacedNpcRecord(
                id, handle, definitionId, entityId, worldId, x, y, z, yaw, pitch, name, skinOverride,
                portraitOverride, dialogueOverride, createdBy, createdAt);
    }

    public PlacedNpcRecord withSkin(String skin) {
        return new PlacedNpcRecord(
                id, handle, definitionId, entityId, worldId, x, y, z, yaw, pitch, displayNameOverride, skin,
                portraitOverride, dialogueOverride, createdBy, createdAt);
    }

    public PlacedNpcRecord withPortrait(String portrait) {
        return new PlacedNpcRecord(
                id, handle, definitionId, entityId, worldId, x, y, z, yaw, pitch, displayNameOverride, skinOverride,
                portrait, dialogueOverride, createdBy, createdAt);
    }

    public PlacedNpcRecord withDialogue(String dialogue) {
        return new PlacedNpcRecord(
                id, handle, definitionId, entityId, worldId, x, y, z, yaw, pitch, displayNameOverride, skinOverride,
                portraitOverride, dialogue, createdBy, createdAt);
    }

    public PlacedNpcRecord withHandle(String handle) {
        return new PlacedNpcRecord(
                id, handle, definitionId, entityId, worldId, x, y, z, yaw, pitch, displayNameOverride, skinOverride,
                portraitOverride, dialogueOverride, createdBy, createdAt);
    }
}
