# Offerings And Shrines

Admin guide for Shrine of Foundation projects and Offering verification.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

## Status

`Implemented`, `Admin-only`

Manual verification passed for item Offerings, banked currency Offerings, UI interaction, completion, Realm-citizen rewards, offline deferred rewards, restart persistence, reset/delete, and rejection paths.

Offerings owns Shrine blocks, project definitions, project instances, progress, donation records, and milestones.

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

## Verification

- Open the linked Shrine UI.
- Offer required items from inventory.
- Offer required currency from banked balance.
- Confirm progress updates live.
- Confirm recent donation history appears.
- Complete the project and verify milestone rewards.
- Restart the server and confirm progress, history, and completion state persist.

## Notes

- Item and currency offerings are server-authoritative.
- Event requirements such as `builder_help` are credited by future registered systems, not by direct player UI.
- Shrine UI uses the shared Core UI theme.
- The current model is a placeholder 2x2 footprint and 5-block height.

## Source-Backed Notes

- Addon docs: [../../docs/addons/offerings.md](../../docs/addons/offerings.md)
- Commands: [../../addons/offerings/src/main/java/panetina/elarion/addons/offerings/command/](../../addons/offerings/src/main/java/panetina/elarion/addons/offerings/command/)
- Client UI: [../../addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/](../../addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/)
