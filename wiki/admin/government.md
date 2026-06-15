# Government

Admin guide for the first Government backend foundation.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

## Status

`Implemented foundation`, `Admin-only`, `Manual verification needed`

Government currently owns config-defined forms, office metadata, action metadata, transition metadata, compact Realm government state, persisted founding votes, assigned office holders, Shrine/Foundation gate status, authority inactivity cleanup, two admin-placed Government blocks, focused Civic Forum UI snapshots/actions, Seat of Rule module shells, and `/lc` authority eligibility.

It does not yet implement laws, taxes, treaties, treasury spending, reform mechanics, or rich authority modules.

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
/e government test advance <realm>
/e government block remove
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

The OP-only `identity set` and `founding complete` commands remain development
shortcuts. The Civic Forum now contains the normal founding vote flow.

## Blocks

Government blocks:

```text
elarion:civic_forum
elarion:seat_of_rule
```

Both are in the `elarion:government` Creative tab and can only be placed by OP
level 4 players.

Current behavior:

- Civic Forum opens one focused page at a time for the Realm-owned world it is
  placed in.
- Neutral players and citizens of another Realm cannot open that Realm's
  Civic Forum or Seat of Rule.
- Before Foundation I, the Realm Name screen is visible but locked.
- After Foundation I, active citizens can submit one Realm name/tag proposal
  during a 24h proposal window.
- After proposals close, citizens vote from the proposal grid during a separate
  24h voting window.
- After a name vote resolves, the Government Form screen appears.
- After Foundation II, active citizens can vote for Monarchy, Republic,
  Theocracy, or Confederation.
- After the form vote resolves, the Founding Election screen appears.
- After Foundation III, active citizens can nominate themselves and vote in the
  founding election.
- Seat of Rule stays locked until Foundation III and founding completion, then
  shows the official government summary, office holders, and future authority
  modules.

These blocks are interaction anchors and UI entry points. Laws, taxes,
treaties, proposals, and full office management are still future modules.

Realm names are limited to 3-24 characters and two words. Names cannot contain
government or settlement labels such as Kingdom, Empire, City, Republic,
Confederation, or Holy Land; those labels are generated later from the chosen
government form.

Development timing:

```text
/e government test advance realm1
```

Run it once to end an active name-proposal window. After at least one ballot is
cast, run it again to expire and resolve the current vote. It follows the
Realm's current founding screen.

Safe block removal:

```text
/e government block remove
```

Look directly at a Civic Forum or Seat of Rule before running it.

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
- Right-clicking Civic Forum should open the current focused founding page.
- Civic Forum and Seat actions are server-bound to the specific block session
  opened by right-clicking. If an action is rejected after moving away, changing
  world, removing the block, or waiting too long, reopen the block.
- The session layer is intentionally small and tested separately; gameplay
  checks still happen server-side before state is mutated.
- Before Foundation I, name proposal controls should be locked.
- After Foundation I, a citizen should be able to propose a name and tag.
- Voting for a proposal should refresh the UI with `Your vote`.
- After vote expiry, the next stage should appear.
- Right-clicking Seat of Rule should open a locked or authority-summary UI.
- Seat module buttons are placeholders, but they still require a valid Seat
  session and Realm citizen context.
- `set-form <realm> republic` should persist to Government runtime state and emit a government history event.
- `office assign <realm> president <player>` should grant `/lc` access if the active form has that office.

## Source-Backed Notes

- Commands: [../../addons/government/src/main/java/panetina/elarion/addons/government/command/GovernmentCommands.java](../../addons/government/src/main/java/panetina/elarion/addons/government/command/GovernmentCommands.java)
- Blocks: [../../addons/government/src/main/java/panetina/elarion/addons/government/GovernmentBlocks.java](../../addons/government/src/main/java/panetina/elarion/addons/government/GovernmentBlocks.java)
- Block interactions: [../../addons/government/src/main/java/panetina/elarion/addons/government/GovernmentBlockInteractions.java](../../addons/government/src/main/java/panetina/elarion/addons/government/GovernmentBlockInteractions.java)
- UI sessions: [../../addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentUiSessionService.java](../../addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentUiSessionService.java)
- UI payload: [../../addons/government/src/main/java/panetina/elarion/addons/government/network/GovernmentUiOpenPayload.java](../../addons/government/src/main/java/panetina/elarion/addons/government/network/GovernmentUiOpenPayload.java)
- UI screen: [../../addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentStatusScreen.java](../../addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentStatusScreen.java)
- Definitions: [../../addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentDefinitionService.java](../../addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentDefinitionService.java)
- Runtime state: [../../addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentStateService.java](../../addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentStateService.java)
