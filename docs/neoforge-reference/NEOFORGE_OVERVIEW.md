# NeoForge 1.21.1 Reference Overview

NeoForge is a reference source only. Elarion remains Fabric-first. Use `external/neoforge` to understand mods that need porting, then translate concepts into Fabric and Elarion's Core/addon architecture.

## Event System

- NeoForge uses event buses and event classes under packages such as `net.neoforged.neoforge.event`.
- Many hooks are registered with mod/event bus subscribers.
- Fabric equivalents are usually Fabric API event callbacks, lifecycle events, networking callbacks, or focused mixins when no event exists.
- Do not create broad event buses in addons unless there is a real cross-addon extension need. Prefer Core registries and explicit addon APIs.

## Registries

- NeoForge commonly uses deferred registers and registry events.
- Fabric uses direct registry calls during initialization.
- Porting rule: preserve registry IDs and behavior, but rewrite registration to Fabric's `Registry.register(...)` and Elarion module conventions.

## Menus

- NeoForge menus map to Minecraft menu/screen concepts.
- Fabric uses `ScreenHandler`, `ScreenHandlerType`, and `HandledScreen` for inventory-backed UI.
- Elarion also uses custom server-authoritative screens for non-inventory systems such as NPC dialogue and Shrine project views.

## Networking

- NeoForge networking often uses payload registration and channel/distributor helpers.
- Fabric uses typed `CustomPayload` codecs plus `ServerPlayNetworking` and `ClientPlayNetworking`.
- Server validation remains mandatory after porting. Never trust a client packet because the original NeoForge mod did.

## Data Attachments

- NeoForge Data Attachments provide object-attached persisted data.
- Fabric has a Data Attachment API, but Elarion should prefer Core-owned persistent storage for canonical systems.
- Attachments are suitable for small local state, not citizens, Realms, Economy balances, history, rewards, or public-memory indexes.

## Datagen

- NeoForge datagen and Fabric datagen solve similar asset/data-generation problems with different APIs.
- Port generated content into Fabric datagen or static assets.
- Keep Elarion gameplay definitions data-driven in config, not compiled into datagen output.

## Local Reference

- NeoForge checkout: `external/neoforge`
- Requested branch: `1.21.1`

Use this as porting research, not as an architecture template for Elarion.
