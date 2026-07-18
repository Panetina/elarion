# Offerings And Shrines

Admin guide for Shrine of Foundation projects and Offering verification.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

## Status

`Implemented`, `Admin-only`

Manual verification passed for item Offerings, banked currency Offerings, UI interaction, completion, Realm-Ember rewards, offline deferred rewards, restart persistence, reset/delete, and rejection paths.

Offerings owns Shrine blocks, project definitions, project instances, progress, donation records, milestones, and optional per-instance display-name overrides.

Definitions:

```text
config/elarion/addons/offerings/projects/
config/elarion/addons/offerings/instances.yml
config/elarion/addons/offerings/ui.yml
```

Runtime state:

```text
world/elarion/addon-state/offerings/
```

## Basic Admin Flow

```text
/e offerings reload
/e offerings projects
/e offerings inspect council_hall
/e offerings start realm realm1 council_hall
/e offerings instances
```

Place a Shrine of Foundation, look at it, then link it:

```text
/e offerings shrine link <instance>
/e offerings shrine inspect
```

Use unlink if you want to preserve the instance:

```text
/e offerings shrine unlink
```

Use remove if you want to delete the linked Shrine and its instance:

```text
/e offerings shrine remove
```

Reset Shrine progression for testing without deleting Shrine blocks or links:

```text
/e test shrine reset
/e test shrine reset realm1
/e test realm global realm1 on
/e test realm global realm1 off
```

This clears Offering progress, donation history, display-name overrides, and
Offering-owned Realm flags such as Foundation gates. Reset instances return to
their first configured project level, and the Shrine title falls back to that
level presentation such as `Foundation I`. It also reverses project-owned
milestone side effects such as Shrine-unlocked Ancient Gate routes. It
preserves Shrine blocks, Shrine links, NPC placements, Quest state, and
Government state.

Use `/e test realm global <realm> on|off` when you need to manually toggle the
Offering-owned global-access flag without resetting Shrine progress. This is a
development/admin override for testing world notifications, tablist visibility,
and global-stage portal/title behavior.

## Verification

- Open the linked Shrine UI.
- Offer required items from inventory.
- Offer required currency from banked balance.
- Confirm progress updates live.
- Confirm recent donation history appears.
- Confirm history centers the contributor nickname and offering amount, uses
  the contributor's Realm color, uses the Sigil accent for currency, and gray
  for block/item Offerings.
- Complete the project and verify milestone rewards.
- Restart the server and confirm progress, history, and completion state persist.

## Notes

- Item and currency offerings are server-authoritative.
- Event requirements such as `builder_help` are credited by future registered systems, not by direct player UI.
- Shrine UI uses the shared civic brown/gold shell with selected Contribute and
  History tabs, compact requirement rows, bounded reward slots, native reward
  tooltips, and a server-authoritative amount prompt.
- The current model is a placeholder 2x2 footprint and 5-block height.

## Source-Backed Notes

- Addon docs: [../../docs/addons/offerings.md](../../docs/addons/offerings.md)
- Commands: [../../addons/offerings/src/main/java/panetina/elarion/addons/offerings/command/](../../addons/offerings/src/main/java/panetina/elarion/addons/offerings/command/)
- Client UI: [../../addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/](../../addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/)
