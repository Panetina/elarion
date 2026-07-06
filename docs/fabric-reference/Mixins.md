# Fabric Mixins

## Purpose

Patch Minecraft behavior where Fabric API does not expose a stable event or extension point.

## Core Minecraft Classes

- Target classes depend on the hook.
- Yarn mappings define readable class/member names.

## Core Fabric API Classes

- Fabric Loader mixin integration.
- Access wideners when field/method access is the only blocker.

## Common Patterns

- Keep mixins narrow.
- Put behavior in services; mixins should delegate.
- Document why an event/API was insufficient.
- Add tests or manual verification notes for fragile hooks.

## Anti-patterns

- Owning gameplay state in a mixin.
- Broad injections into hot paths without performance proof.
- Redirecting large methods when a callback injection is enough.
- Mixing into client classes from server-only modules.

## Example Source Locations

- Names addon mixins: `addons/names/src/main/java/panetina/elarion/addons/names/mixin`
- Titles renderer mixin: `addons/titles/src/main/java/panetina/elarion/addons/titles/mixin`
- Realms explosion mixin: `addons/realms/src/main/java/panetina/elarion/addons/realms/mixin`
- Worlds border/spawn mixins: `addons/worlds/src/main/java/panetina/elarion/addons/worlds/mixin`

## Elarion Use Cases

- Identity/name rendering.
- Realm protection gaps.
- World border behavior.
- Client UI integration where Fabric API has no direct hook.
