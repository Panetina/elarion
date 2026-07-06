# Elarion Government Addon

Technical contract for the Government backend foundation.

Last reviewed: 2026-07-05.

## Status

Implemented foundation. Form definitions, compact Realm state, OP office
assignment, Shrine/Foundation gate status, Government identity overlay state,
authority inactivity cleanup, two admin-placed Government blocks, focused Civic
Forum founding flow, persisted founding votes, citizen proposals, typed civic
records, Seat of Rule proposal review/finalization/archive, universal authority
chat, and temporary office title overrides exist.
Taxes, treaties, treasury spending, reforms, and rich authority modules are
future work.

## Ownership

Government owns:

- government form definitions
- office definition metadata
- role action metadata
- transition metadata
- Realm-scoped active government form state
- current office holders in Government runtime state
- previous active-title restore pointers for citizens whose active Core title
  is temporarily replaced by a Government office title
- persisted founding vote state for Realm name, Government form, and first
  authority election
- persisted citizen proposals and typed civic records
- Theocracy founding faith name/mark after Theocracy is selected
- authority eligibility for `/lc`
- Government gate status derived from Offering Realm flags
- Government identity overlay state before Core Realm display migration
- authority inactivity cleanup and basic vacancy/succession events
- `elarion:civic_forum` and `elarion:seat_of_rule` block/item registration
- the `elarion:government` Creative tab

Government does not own:

- Realm membership
- Core leader truth
- citizen identity
- abilities
- relationships
- rewards
- history storage
- group identity or group membership

Those remain Core-owned, except group identity and membership which are owned by
the Groups addon.

## Config

```text
config/elarion/addons/government/government.yml
config/elarion/addons/government/forms/<form-id>/form.yml
```

Definitions are loaded on startup/reload and cached as immutable records.

`GovernmentConfigDescriptors` registers the `government` read-only config
domain through `ElarionApi.system().configs()`. It exposes the current
`government.yml` authority cleanup settings and form summaries from
`GovernmentDefinitionService`: form IDs, display metadata, authority offices,
office counts/holder limits, action groups, and transitions. This is discovery
only; config writes, Admin Panel editing, reload semantics, file formats,
voting behavior, office behavior, packets, and persistence are unchanged.

## Runtime State

```text
world/elarion/addon-state/government/state.json
```

Runtime state stores only compact Realm government state. It does not copy full
form definitions into world state.

When a citizen is elected or assigned to an office, Government activates a
matching Core title while the office is held. Default office titles are:
Monarch, Heir, President, Councilor, Holy Priest, Synod Member, Delegate, and
Officer. Government unlocks the title before activating it, persists the
previous active title id in `state.json`, and restores it when the citizen
loses the office or the Realm's Government state is reset. Government also
revokes the temporary authority title unlock when the rank is gone, so the
title no longer appears in the Core Collection title list. Core remains the
source of truth for title definitions, unlocks, active title storage, and
identity rendering.

## Blocks

Government registers two OP-placed foundation blocks:

```text
elarion:civic_forum
elarion:seat_of_rule
```

Both appear in the `elarion:government` Creative tab. Their item placement is
OP level 4 only.

The current block interactions open server-authored GUI snapshots using the
shared Core Elarion UI foundation:

- Civic Forum shows exactly one focused staged page:
  - Realm Name, locked until `foundation_i`
  - Realm Color, shown after the name/tag vote resolves
  - Government Form, locked until `foundation_ii`
  - Founding Faith, shown only when Theocracy is selected
  - Founding Election, locked until `foundation_iii`
  - Citizen Civic Features after founding completes
- Seat of Rule is locked until Foundation III and founding completion. In V1 it
  is authority-facing: Monarch, President, Holy Priest, or any elected
  Confederation delegate can open it. Council and Synod members receive
  targeted notifications/action cards for decisions that require their vote.
  Seat modules are filtered by the active government form; monarchy-only
  succession rows such as `Appoint Heir` are shown only when the form actually
  defines an `heir` office.

The blocks infer their Realm from Core's Realm-owned world mapping. They do not
own votes, elections, laws, treasury state, or Core Realm truth.

Network/UI:

- `GovernmentUiOpenPayload` sends a server-authored snapshot, including the
  current primary authority label resolved through Core nicknames.
- `GovernmentUiActionPayload` sends proposal, vote, nomination, and module
  open requests back to the server.
- `GovernmentUiFeedbackPayload` updates the open Civic Forum notice band for
  rejected or stale actions without relying on chat only.
- `GovernmentStatusScreen` renders the snapshot client-side.
- Client clicks are presentation only; all Government mutations validate in
  `GovernmentStateService`.
- Government UI actions also require a recent server-issued block session for
  the same player, Realm, world, block type, and interaction range. Seat module
  screens validate against the Seat of Rule; Civic screens validate against the
  Civic Forum.
- Neutral players and citizens of another Realm cannot open or mutate a
  Realm's Civic Forum or Seat of Rule. Same-Realm citizens can open Civic
  Forum, but Seat of Rule is restricted to the active ruler or an elected
  Confederation delegate in V1.
- Foundation screens are routed through focused client classes under
  `client/foundation/`; the Seat uses `client/seat/`. Shared drawing remains in
  one screen shell so form-specific screens do not duplicate UI infrastructure.
- Locked Government block screens and rejected server actions report the
  specific blocking Shrine level, such as Foundation I for Realm naming,
  Foundation II for Government-form voting, and Foundation III for founding
  elections or Seat of Rule access.
- While founding is incomplete, Civic Forum keeps `Current Votes` available
  and visually disables `Proposals`, `Laws`, `Projects`, `Offices`, and
  `History`. Stale already-open module screens are bounced back to Current
  Votes with the exact remaining founding requirement.

## Commands

All commands are OP level 4:

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

`set-form` is a development command for testing stored state. It is not a vote
implementation.

`/e test government advance <realm>` is a temporary development command. During
proposal/nomination windows it ends the submission window first; after at least
one ballot exists it expires and resolves the current vote.

`/e test government reset` clears Government founding/runtime state for every
Realm: voted names/tags/colors, faith identity, active form, offices, votes,
proposals, and civic records. `/e test government reset <realm>` limits that to
one Realm. Resetting Government also releases Confederation delegate group
locks. It does not touch Portal routes, NPC placements, Shrine blocks, or
Offering progress/flags. It restores active titles that Government temporarily
replaced for removed office holders.

`block remove` safely removes the Government block the administrator is looking
at. Government blocks do not own separate placement runtime records, so no
orphaned placement state remains.

Player command:

```text
/lc <message>
```

`/lc` sends to same-Realm Government authority holders only. `/ac` remains
alliance chat.

## Default Forms

The generated foundation forms are:

- `monarchy`: monarch, heir, officer
- `republic`: president, council_member, officer
- `theocracy`: high_priest, synod_member, officer
- `confederation`: delegate, officer

Every form reserves the `officer` office for future law enforcement and civic
enforcement systems. In V1, officers can use `/lc` but do not approve,
reject, finalize, or direct-create civic records. Confederation delegates are
group-representative offices and consume the Groups addon API.

Confederation delegate rules:

- delegate candidates must be leaders of an eligible Groups addon group
- all group members must belong to the same Realm at nomination time
- elected delegate groups are locked against cross-Realm invites
- delegate group IDs are stored in Government runtime state beside delegate
  office holders
- `/e government authority cleanup` removes invalid delegate seats and releases
  their group locks

## Foundation Gates

Government reads Offering-owned Realm flags:

- `foundation_i`
- `foundation_ii`
- `foundation_iii`

The gate status API reports:

- name vote visible/unlocked
- color vote visible/unlocked after the Realm name vote resolves
- Government choices visible
- Government vote unlocked
- founding election unlocked
- Seat of Rule unlocked

The block UI and server-side voting checks surface the exact blocking level in
their lock messages instead of a generic locked message.

The current `identity set` and `founding complete` commands remain OP-only
development controls. The Civic Forum now owns the normal player-facing
founding flow.

## Founding Votes

Government persists vote state inside `state.json`.

Implemented vote types:

- `REALM_NAME`: active citizens propose one display name and 2-6 character
  tag during a 24h proposal window, then vote for one proposal during a
  separate 24h voting window.
- `REALM_COLOR`: active citizens choose one vanilla Minecraft text color after
  the Realm name/tag resolves and before the government-form vote opens.
- `GOVERNMENT_FORM`: active citizens vote for Monarchy, Republic, Theocracy, or
  Confederation.
- `THEOCRACY_FAITH`: if Theocracy wins, active citizens propose one founding
  faith name and 2-6 character faith mark, then vote for one proposal.
- `FOUNDING_ELECTION`: active citizens nominate themselves during a 24h
  nomination window, then approve eligible candidates during a separate 24h
  voting window. The empty candidate view retains the `Nominate Yourself`
  action, and a citizen who has nominated sees `You are nominated` until
  voting opens. Multi-seat offices let each voter approve up to the office
  holder limit.

Rules:

- active same-Realm citizens only
- one name proposal per citizen
- Realm names are 3-24 characters, one or two Unicode-letter words, normalized
  to title case with one space, and cannot contain government/settlement titles
  such as Kingdom, Empire, City, Republic, or Holy Land
- first valid ballot starts a 24h window
- voter identities remain private; live option counts are shown in UI snapshots
- final vote totals are persisted for future result notifications
- plurality wins
- ties create an automatic 12h runoff containing only the tied leading options
- expired vote windows with no valid ballots reopen the same stage with a fresh
  24h window instead of advancing silently
- Republic founding is phased: citizens elect one President first, then elect
  at least one and up to three Councilors in a second founding election phase.
  The elected President cannot nominate for or hold a Councilor seat. A
  President-only dev/runtime state stays in the Councilor phase; startup repair
  only marks founding complete if both President and at least one Councilor are
  already present.
- Confederation delegate nomination requires an eligible Groups addon leader;
  each voter may approve up to three different delegate candidates.
- Theocracy founding elections are blocked until the founding faith is selected

## Theocracy

If citizens choose Theocracy, the Civic Forum inserts a `Founding Faith` stage
before Foundation III elections. This gives voters a concrete religion before
they choose religious leaders.

The stage records:

- faith display name
- public 2-6 character faith mark
- completion timestamp

After the faith vote resolves, the founding election chooses:

- one `high_priest`

The Holy Priest title is the primary spiritual authority and public voice of
the Realm faith. Synod Members are chosen by that office after founding and
serve as the religious council that reviews Holy Priest law doctrine in V1.
Rituals, holy sites, synod appointment UI, and succession-crisis UI remain
future work.

## Proposals And Civic Records

After founding is complete, the Civic Forum exposes citizen modules:

- `Proposals`: active citizens create structured proposals.
- `Laws`: citizens read active law records.
- `Projects`: citizens read approved project records.
- `Offices`: citizens read authority holders, tenure, and basic office stats.
- `History`: citizens read bounded Government chronicle entries and recent
  civic outcomes.

Before founding completes these tabs remain visible but disabled so citizens
understand they are locked behind the remaining founding step. Rejected
proposal creation during this phase shows a Civic notice instead of silently
failing.

Proposal V1 fields are category, title, and bounded body. Citizen proposals are
suggestions, not final legal wording. Supported categories are `law`,
`realm_project`, and `civic_rule`. Public notices are authority notifications
plus Government history entries, not citizen proposal categories.

The Seat of Rule exposes authority review modules:

- `Proposals`: authority holders approve or reject pending citizen proposals.
- `Laws`: authority holders can direct-add/archive active law records where
  their government form allows it.
- `Projects`: authority holders can direct-add/archive project records where
  their government form allows it.
- `Offices`: authority holders inspect and manage support offices where their
  government form allows it.
- `Archive`: authority holders can restore archived civic records.

Approval and finalization rules are form-specific, but the player-facing flow is
intentionally simple: citizen proposal -> authority approval -> final official
text -> active civic record. Once finalized, the record appears under Laws,
Projects, or History; enacted/rejected proposals no longer stay in the active
Proposals list.

- Monarchy: the Monarch approves or rejects.
- Theocracy: the Holy Priest approves or rejects citizen law proposals and
  writes final doctrine.
- Republic: the President approves or rejects citizen law proposals first. If
  approved, the President writes final official wording and the law becomes an
  active record.
- Confederation: non-law proposals use majority of filled delegate seats. Law
  proposals require at least two delegate approvals to pass and at least two
  delegate rejections to fail. The delegate whose vote reaches the two-approval
  threshold becomes the Sponsor Delegate and writes the final official wording.

The `officer` placeholder office is excluded from proposal decisions in V1.
Officers keep authority chat access only until law enforcement mechanics exist.

Approved proposals become `APPROVED_PENDING_FINALIZATION`. Authority then writes
the official title/body. Finalization creates the active civic record directly
and preserves the source proposal ID for notifications and history.

Direct authority creation is form-specific:

- Monarchy: the Monarch can directly add laws and project records, and can
  send Realm notices.
- Theocracy: the Holy Priest can directly add laws and project records, and can
  send Realm notices.
- Republic: law proposals first require citizen petition support, then
  President approval plus finalization.
- Confederation: uses delegate approval plus finalization. Delegate law approval
  requires two delegates.

Typed civic records are presentation/authority records in V1; they do not
enforce taxes, punishments, permissions, jail actions, or economy effects yet.

## Notifications

Government publishes Realm/Government notifications to a snapshotted audience
for proposal windows, vote windows, runoffs, completed results, Realm color
selection, authority appointments, removals, authority-authored notices, and
inactivity vacancies. Personal notifications confirm proposal, vote,
nomination, election, and office outcomes. Seat of Rule notices are titled as
notices from the current authority holder, using their Core nickname/display
name plus office label.
Active vote cards can open the Civic Forum directly. Realm relationship
decisions use Core Government cards with Approve/Reject actions and publish a
final result card after resolution or expiry. Ballot totals remain private while
voting is active.

Citizen proposal creation publishes an authority review notification. Approved,
rejected, and finalized proposals notify the author. Finalized civic records
notify the source proposer and the Realm. Archived/restored records notify the
Realm.

Government emits Core domain events for meaningful lifecycle changes such as
Realm name/color selection, government-form selection, vote casts, nominations,
founding election completion, office assignment/removal, and inactivity
vacancies. Proposal/law lifecycle events include
`government-proposal-created`, `government-proposal-review-vote`,
`government-proposal-resolved`, `government-proposal-finalized`,
`government-civic-record-created`, `government-civic-record-archived`, and
`government-civic-record-restored`. Notifications remain explicit projections;
events do not automatically create player-facing cards.

## Authority Inactivity

`government.yml` controls:

```text
authority.inactivity-days: 7
authority.inactivity-check-interval-seconds: 600
```

The cleanup is bounded to stored office holders. It does not scan all players
or all worlds. Online office holders are considered active. Monarchy can promote
an active heir when the monarch is removed; other offices are vacated and emit
history for future election/replacement systems.

For Confederation, the cleanup also checks stored delegate seats against the
represented group. If the delegate is no longer the group leader or the group is
no longer Realm-eligible, the delegate seat is vacated and the group lock is
released.

## Source

```text
addons/government/src/main/java/panetina/elarion/addons/government/
```

Primary services:

- `GovernmentDefinitionService`
- `GovernmentStateService`
- `GovernmentProposalDecisionPolicy`
- `GovernmentCommands`
- `GovernmentBlockInteractions`
- `GovernmentUiOpenPayload`
- `GovernmentStatusScreen`
