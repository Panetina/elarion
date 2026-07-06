# Government

Admin guide for the first Government backend foundation.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

## Status

`Implemented foundation`, `Admin-only`, `Manual verification needed`

Government currently owns config-defined forms, office metadata, action metadata, transition metadata, compact Realm government state, persisted founding votes, voted Realm identity/color overlays, assigned office holders, temporary office title overrides, Shrine/Foundation gate status, authority inactivity cleanup, two admin-placed Government blocks, focused Civic Forum UI snapshots/actions, citizen proposals, typed civic records, Seat of Rule proposal review/finalization/archive, and `/lc` authority eligibility.

Laws are one type of civic record in V1. Government does not yet implement taxes, treaties, treasury spending, automated enforcement, reform mechanics, or rich authority modules.

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
/e government proposals <realm>
/e government proposal inspect <realm> <proposal>
/e government laws <realm>
/e government law archive <realm> <law>
/e government law restore <realm> <law>
/e government set-form <realm> <form>
/e government identity set <realm> <tag> <display-name...>
/e government founding complete <realm>
/e government authority cleanup
/e government reset <realm>
/e test government reset [realm]
/e test government advance <realm>
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

Elected or assigned authority holders receive a temporary active Core title
while seated: Monarch, Heir, President, Councilor, Holy Priest, Synod Member,
Delegate, or Officer. Government unlocks the title before activating it. The
previous active title is restored when the office is removed, the delegate seat
becomes invalid, authority cleanup vacates the seat, or Government reset clears
that Realm. The temporary authority title unlock is also revoked when the rank
is gone, so it no longer appears in the player's Collection title list.

The `officer` office is reserved in every form for future law enforcement and
civic enforcement mechanics. In V1, officers can use `/lc` but do not approve,
reject, finalize, or direct-create civic records. Confederation delegates are
marked as group-representative offices.

Confederation delegate basics:

- candidates must be leaders of registered Groups addon groups
- every member of the candidate group must belong to the same Realm
- elected delegate groups cannot invite cross-Realm members while seated
- authority cleanup also removes invalid delegate seats if the represented
  group stops qualifying

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
- UI actions require a recent server-issued session from the correct block,
  player, Realm, world, and range. Reopen the block if an action is rejected as
  stale.
- Neutral players and citizens of another Realm cannot open that Realm's
  Civic Forum or Seat of Rule.
- Before Foundation I, the Realm Name screen is visible but locked.
- After Foundation I, active citizens can submit one Realm name/tag proposal
  during a 24h proposal window.
- After proposals close, citizens vote from the proposal grid during a separate
  24h voting window.
- After a name vote resolves, citizens vote for one vanilla Minecraft Realm
  color.
- After the color vote resolves, the Government Form screen appears.
- After Foundation II, active citizens can vote for Monarchy, Republic,
  Theocracy, or Confederation.
- If Theocracy wins, the Founding Faith screen appears. Citizens propose and
  vote for the religion name/mark before electing religious leaders.
- After the form vote resolves, or after the Theocracy faith vote resolves, the
  Founding Election screen appears.
- After Foundation III, active citizens can nominate themselves during a 24h
  nomination window. The Civic Forum shows `Nominate Yourself` even before the
  first candidate exists, then marks the citizen's row `You are nominated`.
- After nominations close, citizens vote from the candidate grid during a
  separate 24h founding election window.
- Republic founding is phased. Citizens elect one President first; after that
  resolves, a second founding election opens for at least one and up to three
  Councilors. The elected President cannot nominate for or hold a Councilor
  seat.
- Confederation delegate elections fill up to three seats. Each citizen may
  approve up to three different eligible group-leader candidates.
- Theocracy founding elects one Holy Priest title holder. The office id remains
  `high_priest`. The Holy Priest chooses Synod Members after founding; the
  Synod appointment UI remains a later authority module.
- Seat of Rule stays locked until Foundation III and founding completion. In
  V1, the active Monarch, President, Holy Priest, or any elected Confederation
  delegate can open the full Seat. Council and Synod members receive targeted
  Realm/Personal notification action cards when their vote is needed.
- If the final holder of the elected primary office resigns or is removed after
  founding, Civic Forum reopens that office's nomination and election flow.
  Support-office vacancies and partially occupied multi-seat offices do not
  reset founding. On server startup, the same repair is applied to completed
  Realms already missing their primary holder, including vacancies saved by an
  older build.
- Civic Forum keeps post-founding tabs visible but disabled until founding is
  complete. During Republic Councilor founding, stale Proposal/Law/Project
  clicks return to Current Votes and show a Civic notice explaining that at
  least one Councilor still has to be nominated and elected.
- Seat of Rule root layout is an authority dashboard with Review, Laws,
  Projects, Offices, and Archive modules.
- Locked Government block screens and rejected voting actions now name the
  blocking Shrine level, for example Foundation I for Realm naming, Foundation
  II for Government form voting, and Foundation III for founding elections or
  Seat of Rule access.

These blocks are interaction anchors and UI entry points. Taxes, treaties,
automated law enforcement, reform mechanics, and full office management remain
future modules.

Citizen proposals:

- after founding, active citizens open Civic Forum -> Proposals
- choose a category with Left/Right in the proposal overlay
- enter a title and bounded body
- submitted Republic law proposals open citizen ratification first, then move
  to authority review after enough citizen support

Seat review:

- authority holders open Seat of Rule -> Proposals
- click `Approve` or `Reject` on a pending proposal
- Monarchy uses Monarch approval
- Theocracy law flow is citizen proposal -> Holy Priest approval -> Holy Priest
  final doctrine -> active law.
- Republic law flow is citizen petition vote -> President review -> President
  final wording -> active law.
- Confederation law proposals are decided only by the three delegates and need
  at least two `Approve` votes to pass. The delegate whose vote reaches the
  two-approval threshold becomes the Sponsor Delegate and writes the final
  wording. Two `Reject` votes fail the proposal.
- Officers are excluded from proposal decisions in V1.
- approved proposals move to `Finalize`; they do not become official records
  until authority writes the final official title/body
- finalized citizen proposals become typed civic records and move out of the
  active Proposals list into Laws or Projects
- notices are authority-created Realm notifications plus Government history,
  not citizen proposal categories; the Realm notification title identifies the
  authority holder who sent it
- Monarchy authority can directly add laws or projects and send notices
- Holy Priest authority can directly add laws or projects and send notices
- Republic records use proposal approval plus finalization
- Confederation uses delegate approval plus finalization
- Seat of Rule -> Laws/Notices/Rules/Projects can archive active records
- Seat of Rule -> Archive can restore archived records

Realm names are limited to 3-24 characters and two words. Names cannot contain
government or settlement labels such as Kingdom, Empire, City, Republic,
Confederation, or Holy Land; those labels are generated later from the chosen
government form.

Development timing:

```text
/e test government advance realm1
```

Run it once to end an active proposal or nomination window. After at least one
ballot is cast, run it again to expire and resolve the current vote. It follows
the Realm's current founding screen.

If a vote window expires with no valid ballots, Government reopens the same
stage with a fresh 24h window. It does not choose a default result.

Reset one Realm's Government runtime state:

```text
/e government reset realm1
/e test government reset
/e test government reset realm1
```

`/e government reset <realm>` is the normal admin path for resetting a single
Realm. `/e test government reset` remains the development-only full reset.

Both reset forms clear voted Realm identity/color, faith identity, active form,
offices, votes, proposals, and civic records for their scope. They also release
Confederation delegate group locks. They do not remove Civic Forum or Seat of
Rule blocks and do not touch Shrine progress, Portal routes, or NPC placements.
They restore active titles that Government temporarily replaced for office
holders.

Notifications:

- Realm notifications are published for proposal/vote windows, Realm name and
  color results, government-form results, founding election completion, and
  office changes.
- Personal notifications confirm proposals, votes, nominations, election
  wins, office removal/assignment, proposal approval/rejection, and proposal
  finalization.
- Government also emits Core domain events for meaningful civic lifecycle
  changes so future systems can consume them without polling Government state.

Theocracy notes:

- Theocracy first chooses a faith identity after Theocracy wins the government
  form vote.
- The founding election then chooses one Holy Priest title holder.
- Synod Members are chosen by the Holy Priest after founding, not elected by
  citizens.
- The Holy Priest can submit law doctrine directly; Synod approves or rejects the
  wording.
- Citizens can submit appeal requests as proposals for the Holy Priest to
  consider.
- Rituals, holy sites, and succession-crisis UI are future modules.

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
active heir. If the final elected primary office becomes vacant, Civic Forum
reopens nominations and publishes a Realm notice. For Confederation, cleanup
also vacates delegate seats whose represented group is no longer valid and
releases the group lock; the election reopens only when no delegate remains.

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
- Voting for a proposal should refresh the UI with live vote counts and a
  green selected-vote frame.
- After vote expiry, the next stage should appear.
- Right-clicking Seat of Rule should open a locked or authority-summary UI.
- Seat module buttons are placeholders, but they still require a valid Seat
  session and Realm citizen context.
- Proposal approval should show `Finalize` instead of immediately creating a
  law.
- Finalizing a proposal should create the matching typed record and preserve
  the source proposal link.
- Record rows expand/collapse on click; archive/restore require their dedicated
  buttons.
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
