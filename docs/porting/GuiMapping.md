# GUI Mapping

NeoForge feature: menus, screens, widgets, and client-only GUI events.

Fabric equivalent: `ScreenHandler`/`HandledScreen` for inventory UIs, custom `Screen` plus payload snapshots for non-inventory UIs.

Minecraft classes: `Screen`, `DrawContext`, `ScreenHandler`, `HandledScreen`, `Slot`.

Fabric API classes: screen handler API, client rendering APIs.

Porting difficulty: Medium.

Notes:

- Inventory transfer UIs should become `ScreenHandler` flows.
- Dialogue/dashboard/project screens can use Elarion UI primitives and custom payloads.
- Do not trust client UI state for item/currency movement.

Example source locations:

- NeoForge: client GUI packages under `external/neoforge/src/main/java/net/neoforged/neoforge/client`
- Fabric: `external/fabric-api/fabric-screen-handler-api-v1`
- Elarion: `NpcDialogueScreen`, `ShrineOfFoundationScreen`, `platform/core/.../client/ui`
