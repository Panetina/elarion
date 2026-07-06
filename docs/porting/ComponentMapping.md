# Component And Attachment Mapping

NeoForge feature: data attachments and item/entity/block data systems.

Fabric equivalent: Minecraft data components, Fabric Data Attachment API, or Elarion world storage.

Minecraft classes: `ItemStack`, `DataComponentTypes`, NBT classes.

Fabric API classes: `AttachmentRegistry`, `AttachmentType`.

Porting difficulty: Medium.

Notes:

- Use components for item-local facts.
- Use attachments for bounded object-local facts.
- Use Elarion storage for canonical gameplay state.

Example source locations:

- NeoForge: `external/neoforge/src/main/java/net/neoforged/neoforge/attachment`
- Fabric: `external/fabric-api/fabric-data-attachment-api-v1`
- Elarion: `platform/core/.../storage`, `addons/economy/.../storage`
