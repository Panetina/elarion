# Fabric ScreenHandlers

## Purpose

Build server-backed inventory UIs where players move item stacks, confirm trades, or interact with slot state.

## Core Minecraft Classes

- `net.minecraft.screen.ScreenHandler`
- `net.minecraft.screen.ScreenHandlerType`
- `net.minecraft.screen.slot.Slot`
- `net.minecraft.client.gui.screen.ingame.HandledScreen`

## Core Fabric API Classes

- `net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory`
- `net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry`

## Common Patterns

- Use `ScreenHandler` when real item slots exist.
- Validate every click and transfer on the server.
- Keep client widgets presentation-only.
- Use custom payloads for non-inventory screens.

## Anti-patterns

- Implementing real barter/trade with only client GUI packets.
- Trusting client-sent item stacks.
- Using a `ScreenHandler` for a read-only dashboard when a snapshot screen is simpler.

## Example Source Locations

- Fabric API: `external/fabric-api/fabric-screen-handler-api-v1`
- Elarion UI primitives: `platform/core/src/main/java/panetina/elarion/core/client/ui`
- Elarion NPC screen: `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`

## Elarion Use Cases

- Future market/barter UI with two inventories and escrow slots.
- Future shop purchase screen if item movement needs slot safety.
- Shrine and NPC dialogue currently use custom server-authoritative screens instead.
