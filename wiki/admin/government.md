# Government

Admin guide for the first Government backend foundation.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

## Status

`Implemented foundation`, `Admin-only`

Government currently owns config-defined forms, office metadata, action metadata, transition metadata, compact Realm government state, assigned office holders, Shrine/Foundation gate status, authority inactivity cleanup, two admin-placed Government blocks, read-only Government UI snapshots, and `/lc` authority eligibility.

It does not yet implement real votes, laws, taxes, treaties, treasury spending, mutating GUI actions, or election flows.

Definitions:

```text
config/elarion/addons/government/government.yml
config/elarion/addons/government/forms/<form-id>/form.yml
```

Runtime state:

```text
world/elarion/addon-state/government/state.json
```

## Admin Commands

```text
/e government reload
/e government forms
/e government inspect <form>
/e government state <realm>
/e government gates <realm>
/e government set-form <realm> <form>
/e government identity set <realm> <tag> <display-name...>
/e government founding complete <realm>
/e government authority cleanup
/e government office assign <realm> <office> <player>
/e government office remove <realm> <office> <player>
```

`set-form` is a development/testing command. It changes Government addon state only. Core still owns Realm membership, Core leader, identity, relationships, abilities, rewards, and history truth.

Authority chat:

```text
/lc <message>
```

`/lc` is Government authority chat. `/ac` remains alliance chat.

## Default Forms

- `monarchy`: monarch, heir, officer
- `republic`: president, council_member, officer
- `theocracy`: high_priest, synod_member, officer
- `confederation`: delegate, officer

The `officer` office is reserved in every form for future law enforcement and
civic enforcement mechanics. Confederation delegates are marked as
group-representative offices, but the delegate election flow is future work.

## Foundation Gates

Gate status is derived from Offering Realm flags:

- `foundation_i`
- `foundation_ii`
- `foundation_iii`

Use:

```text
/e government gates realm1
```

The OP-only `identity set` and `founding complete` commands are development
stand-ins for future Civic Forum votes.

## Blocks

Government blocks:

```text
elarion:civic_forum
elarion:seat_of_rule
```

Both are in the `elarion:government` Creative tab and can only be placed by OP
level 4 players.

Current behavior:

- Civic Forum opens a read-only staged UI for the Realm-owned world it is
  placed in.
- Civic Forum shows Foundation-gated name voting, Government form visibility,
  Government voting, founding election, and Seat unlock state.
- Seat of Rule stays locked until Foundation III and founding completion, then
  shows the official government summary, office holders, and future authority
  modules.

These blocks are interaction anchors only. They do not yet run votes or mutate
laws/elections through a GUI.

## Authority Inactivity

Default authority inactivity is 7 days, checked every 600 seconds.

```text
/e government authority cleanup
```

This command runs the same bounded cleanup immediately. Monarchy can promote an
active heir; other vacant offices wait for future election/replacement systems.

## Current Verification

- `forms` should list Monarchy, Republic, Theocracy, and Confederation.
- `inspect republic` should show offices and role action metadata.
- `state <realm>` should show the current active form or `-`.
- `gates realm1` should show Foundation I/II/III locks and Seat of Rule state.
- Civic Forum and Seat of Rule should appear in the Government Creative tab.
- Right-clicking Civic Forum should open the Government UI, not chat spam.
- Right-clicking Seat of Rule should open a locked or authority-summary UI.
- `set-form <realm> republic` should persist to Government runtime state and emit a government history event.
- `office assign <realm> president <player>` should grant `/lc` access if the active form has that office.

## Source-Backed Notes

- Commands: [../../addons/government/src/main/java/panetina/elarion/addons/government/command/GovernmentCommands.java](../../addons/government/src/main/java/panetina/elarion/addons/government/command/GovernmentCommands.java)
- Blocks: [../../addons/government/src/main/java/panetina/elarion/addons/government/GovernmentBlocks.java](../../addons/government/src/main/java/panetina/elarion/addons/government/GovernmentBlocks.java)
- Block interactions: [../../addons/government/src/main/java/panetina/elarion/addons/government/GovernmentBlockInteractions.java](../../addons/government/src/main/java/panetina/elarion/addons/government/GovernmentBlockInteractions.java)
- UI payload: [../../addons/government/src/main/java/panetina/elarion/addons/government/network/GovernmentUiOpenPayload.java](../../addons/government/src/main/java/panetina/elarion/addons/government/network/GovernmentUiOpenPayload.java)
- UI screen: [../../addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentStatusScreen.java](../../addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentStatusScreen.java)
- Definitions: [../../addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentDefinitionService.java](../../addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentDefinitionService.java)
- Runtime state: [../../addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentStateService.java](../../addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentStateService.java)
