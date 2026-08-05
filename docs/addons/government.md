# Elarion Government Addon

Technical contract for the Government backend and civic UI.

Last reviewed: 2026-07-18.

## Status

Implemented foundation. Government now supports two founding forms only:
Monarchy and Republic. Confederation and Theocracy have been removed from
active config, runtime state, founding flow, UI, tests, titles, and docs.

Government owns form definitions, office metadata, Realm-scoped Government
state, founding votes, voted Realm identity/color overlays, office holders,
temporary office title overrides, Shrine/Foundation gate checks, Civic Forum,
Seat of Rule, audience requests, typed civic records, authority chat, authority
inactivity cleanup, Government notifications, and Government Chronicle wording.

Taxes, treaties, treasury spending, reforms, automated law effects, rich office
management, and enforcement mechanics remain future work.

## Ownership

Government owns:

- government form definitions
- Government offices and authority checks
- Realm name/tag/color founding votes
- Government form and founding election votes
- current office holders in Government runtime state
- audience requests and typed civic records
- Civic Forum and Seat of Rule blocks/UI
- `/lc` authority chat eligibility
- Government notifications and Government domain events

Government does not own:

- Realm membership
- Core Realm canonical IDs
- character identity
- Economy balances or treasuries
- rewards
- public history storage
- group identity or membership

## Config

```text
config/elarion/addons/government/government.yml
config/elarion/addons/government/forms/<form-id>/form.yml
```

Only these generated form IDs are active:

- `monarchy`
- `republic`

`GovernmentConfigLoader` ignores any stale inactive form directories in dev
data, but development config should not keep obsolete form files.

## Runtime State

```text
world/elarion/addon-state/government/state.json
```

Runtime state stores compact Realm government state. It keeps internal Realm
IDs stable and stores only presentation overlays such as voted display name,
tag, color, active form, offices, votes, audience/vote records, and civic
records.

Default temporary authority titles:

- `government_monarch`
- `government_heir`
- `government_president`
- `government_officer`

Government temporarily activates the matching Core title while an office is
held and restores the previous active title when the office is removed or
Government state resets.

## Blocks

Government registers:

```text
elarion:civic_forum
elarion:seat_of_rule
```

Both blocks infer their Realm from Core's Realm-owned world mapping.

Civic Forum flow:

- Realm Name, visible but locked until `foundation_i`
- Realm Color, after the name/tag vote resolves
- Government Form, locked until `foundation_ii`
- Founding Election, locked until `foundation_iii`
- Citizen Civic Features after founding completes

Seat of Rule is locked until Foundation III and founding completion. In V1, the
full Seat is opened by the active ruler:

- Monarchy: `monarch`
- Republic: `president`

Officers keep `/lc` access only and do not approve or finalize civic records in
V1.

The Seat also hosts the Realm-heraldry editor. Government owns the revisioned
32x32 palette bytes in its schema-v2 runtime state; the save request is valid
only for a live Seat session and current Realm authority, and is rate-limited.
Core supplies the reusable client pixel surface but never stores Government
truth. Economy owns the category policy, quote, revision, and treasury
settlement contract. Each
Seat tax edit carries the displayed policy revision and Economy rejects stale
writes before changing its atomic policy snapshot. The Seat presents the
bounded 0–25% category control as a slider; it never calculates tax or chooses
the treasury destination locally. The implemented UI receives a typed, bounded
snapshot of category IDs, labels, basis-point rates, revision, and treasury
destination. Controls use 0.25% steps and remain in the Taxes module when a
mutation fails; formatted presentation rows are never parsed as authority.

## Commands

OP level 4:

```text
/e government reload
/e government forms
/e government inspect <form>
/e government state <realm>
/e government gates <realm>
/e government audience <realm>
/e government audience inspect <realm> <record>
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

Player authority chat:

```text
/lc <message>
```

`/ac` remains alliance chat.

## Foundation Votes

Implemented vote types:

- `REALM_NAME`: active citizens propose one display name/tag, then vote.
- `REALM_COLOR`: active citizens vote from vanilla Minecraft text colors.
- `GOVERNMENT_FORM`: active citizens vote for Monarchy or Republic.
- `FOUNDING_ELECTION`: active citizens nominate and elect the first authority.

Rules:

- active same-Realm citizens only
- inactive citizens cannot propose, vote, nominate, hold office, or count
  toward election logic
- one name proposal per citizen
- name proposals have a 24h proposal window before voting opens
- founding nominations have a 24h nomination window before voting opens
- first valid ballot starts a 24h voting window
- plurality wins
- ties create a runoff
- voter identities remain private while the vote is active
- final vote totals persist for future result notifications

Republic founding elects one President.

## Audience, Laws, And Civic Records

Citizen law/project/rule intake is not active in V1.

Monarchy:

- Citizens may request an audience with the Monarch.
- The Monarch may accept/reject audience requests.
- The Monarch may directly add, archive, and restore laws and project records.

Republic:

- Citizens do not submit law drafts.
- The President writes a law title/body and opens a Yes/No citizen vote.
- If citizen approval reaches threshold, the law becomes active.
- If rejection reaches threshold, the law fails.

Seat modules:

- `Audience` for Monarchy only
- `Laws`
- `Projects`
- `Offices`
- `Archive`

Archive and restore are dedicated actions, not row-click side effects.

## Notifications And Events

Government publishes Realm notifications for vote windows, Realm name and color
results, Government form results, founding completion, Republic law votes,
office changes, civic record creation, archive, and restore.

Personal notifications confirm audience requests, votes, nominations, election
wins, office assignments/removals, and audience approval/rejection.

Government emits Core domain events for meaningful lifecycle changes. Events do
not automatically create notifications; notifications are explicit projections.

## Source

```text
addons/government/src/main/java/panetina/elarion/addons/government/
```

Primary services:

- `GovernmentDefinitionService`
- `GovernmentStateService`
- `GovernmentRealmRecordIndex`
- `GovernmentVoteDeadlineIndex`
- `GovernmentProposalDecisionPolicy`
- `GovernmentCommands`
- `GovernmentBlockInteractions`
- `GovernmentUiOpenPayload`
- `GovernmentStatusScreen`
