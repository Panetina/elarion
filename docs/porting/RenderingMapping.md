# Rendering Mapping

NeoForge feature: renderer registration, client render events, custom GUI/HUD hooks.

Fabric equivalent: Fabric client rendering registries, HUD callbacks, custom screens, and mixins for missing hooks.

Minecraft classes: `EntityRenderer`, `BlockEntityRenderer`, `DrawContext`, `Screen`.

Fabric API classes: `EntityRendererRegistry`, `HudRenderCallback`, `BlockEntityRendererRegistry`.

Porting difficulty: Medium.

Notes:

- Keep renderer state client-only.
- Drive renderers from synced snapshots/caches.
- Do not perform IO or networking in render loops.

Example source locations:

- NeoForge: `external/neoforge/src/main/java/net/neoforged/neoforge/client`
- Fabric: client rendering modules under `external/fabric-api`
- Elarion: `ElarionNpcEntityRenderer`, Core UI renderer.
