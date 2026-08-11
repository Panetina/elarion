# Backpacks

## Ownership

`addons/backpacks` is a thin integration and acquisition-policy addon.
`vendor/yyzsbackpack` preserves the official MIT-licensed `1.21.1-fabric`
source at commit `6f44d3372a25638a90c48e76751c7638e42a4d87`, including its bundled Trinkets
compatibility JAR. It owns the four backpack items, container components, UI,
sorting, movement helpers, models, and network protocol. Trinkets owns
accessory-slot definitions and entity component state. Elarion does not copy
either owner's storage.

The vendor patch is deliberately bounded: item-color registration moved from
the common initializer to the client initializer because the official 1.21.1
JAR references a client-only Fabric class while starting a dedicated server.
The Elarion vendor integration makes an equipped Trinkets slot authoritative:
a backpack carried in normal inventory is inactive and invisible on the player.
The same equipped stack drives opening, storage access, synchronization, and
the player-model renderer. Unequipping it immediately removes that access and
appearance; no parallel Elarion backpack state is stored.

The canonical item IDs are:

- `yyzsbackpack:iron_backpack`
- `yyzsbackpack:gold_backpack`
- `yyzsbackpack:diamond_backpack`
- `yyzsbackpack:netherite_backpack`

All four remain visible in Yyz's Creative tab so builders can place them in
future NPC-owned trade catalogs.

## Acquisition Policy

Backpacks cannot be crafted, upgraded through smithing, or recolored through
the vanilla armor-dye recipe. The vendor source removes all six upstream recipe
files, three recipe advancements, and the backpack entries in the vanilla
`dyeable` tag; the build also excludes those paths defensively. On server start
and successful data-pack reload, the addon removes every recipe owned by the
`yyzsbackpack` namespace, including recipes reintroduced by a data pack. A
focused vanilla recipe mixin rejects backpack stacks from the generic armor-dye
recipe even if another data pack restores the tag.

Creative/admin issuance and future server-authoritative NPC purchases are the
only intended acquisition paths. The future NPC trade catalog must reference
the item IDs above; it must not add replacement recipes or duplicate backpack
contents.

## Trinkets And Future Content

Trinkets 3.10.0 is pinned on both client and server. Yyz's bundled compatibility
module exposes backpacks through `chest/back`. Future Elarion accessories must
use data-driven Trinkets slots/tags and keep their own effect state in their
owning addon. Backpack visual skins belong to resource/model definitions or a
future explicit cosmetic registry; Trinkets is the equipment-slot framework,
not a skin database.

Backpack item components and Trinkets entity components must survive normal
save/restart and any Core/Underworld inventory transition. New reset/death code
must preserve these external components unless an explicit destructive reset
contract says otherwise.

## Events And Notifications

This integration emits no lifecycle events and publishes no notifications.
Routine equipping, opening, moving, sorting, and recipe rejection remain silent
to avoid Chronicle and HUD spam. A future NPC purchase event belongs to NPCs
and Economy settlement, not this addon.

## Verification

- `./gradlew :addons:backpacks:test :addons:backpacks:build`
- `./gradlew verifyDistributionManifest`
- Dedicated startup/reload: the vendored mod and Trinkets compatibility must
  load without environment, recipe, or mixin errors.
- Live client: all four items appear in Creative, but activate and render only
  while equipped in the Trinkets back slot; no crafting/smithing/dyeing result
  is available.
