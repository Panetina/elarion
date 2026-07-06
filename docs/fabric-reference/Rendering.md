# Fabric Rendering

## Purpose

Render custom entities, GUI screens, HUD elements, block entity visuals, and item/tooltips.

## Core Minecraft Classes

- `net.minecraft.client.gui.DrawContext`
- `net.minecraft.client.gui.screen.Screen`
- `net.minecraft.client.render.entity.EntityRenderer`
- `net.minecraft.client.render.entity.EntityRendererFactory`
- `net.minecraft.client.render.VertexConsumerProvider`

## Core Fabric API Classes

- `net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry`
- `net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback`
- `net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry`

## Common Patterns

- Keep rendering client-only.
- Register renderers in client initializers.
- Use `DrawContext` for modern GUI drawing.
- Share UI primitives when screens use the same visual language.

## Anti-patterns

- Loading server state directly from a client renderer.
- Doing network calls or disk IO in render methods.
- Creating a new UI theme per screen.
- Rendering unbounded lists instead of virtualized rows.

## Example Source Locations

- Elarion Core UI: `platform/core/src/main/java/panetina/elarion/core/client/ui`
- Elarion NPC renderer: `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/ElarionNpcEntityRenderer.java`
- Elarion Shrine UI: `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`
- Fabric docs rendering section: `https://docs.fabricmc.net/1.21.1/develop/`

## Elarion Use Cases

- NPC body rendering.
- NPC dialogue UI.
- Shrine UI.
- Future HUD icons, Atlas, quest drawer, shop cards, and contribution/project displays.
