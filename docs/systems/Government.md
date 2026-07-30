# Government System

Status: Implemented foundation, active development.

## Purpose

Government owns Realm civic progression after Offering/Foundation gates:
Realm naming, Realm color selection, Monarchy/Republic selection, founding
elections, authority offices, authority chat eligibility, audience requests,
typed civic records, Government blocks, Government runtime state, notifications,
and temporary office-driven active title overrides.

Government does not own Core Realm membership, Core citizens, Economy balances,
Offering progress, Groups membership, taxes, treaties, automated enforcement,
or treasury spending.

## Main Classes

- `addons/government/.../ElarionGovernmentAddon.java`
- `addons/government/.../GovernmentBlockInteractions.java`
- `addons/government/.../service/GovernmentStateService.java`
- `addons/government/.../service/GovernmentAuthorityTitlePolicy.java`
- `addons/government/.../service/GovernmentProposalDecisionPolicy.java`
- `addons/government/.../service/GovernmentNotificationPolicy.java`
- `addons/government/.../service/GovernmentDefinitionService.java`
- `addons/government/.../service/GovernmentUiSessionService.java`
- `addons/government/.../command/GovernmentCommands.java`
- `addons/government/.../network/GovernmentUiOpenPayload.java`
- `addons/government/.../network/GovernmentUiActionPayload.java`

## Entry Points

- Fabric mod entrypoint: `ElarionGovernmentAddon`
- Client entrypoint: `ElarionGovernmentClient`
- Blocks: Civic Forum and Seat of Rule
- Commands: `/e government ...`
- Authority chat: `/lc`

## Active Forms

- `monarchy`: `monarch`, `heir`, `officer`
- `republic`: `president`, `officer`

Theocracy and Confederation are removed from active Government. Players can
still roleplay religions, churches, factions, alliances, or city unions through
Realm culture, laws, groups, and future relationship systems without those
being separate founding Government forms.

## GUI / Screens

The Government UI uses shared Core UI primitives and two screens:

- `CivicForumScreen`: citizen-facing Realm founding and civic participation.
- `SeatOfRuleScreen`: authority-facing audience review, official records,
  office summaries, and archive actions.

Civic Forum flow:

- `civic_name`
- `civic_color`
- `civic_form`
- `civic_election`
- `civic_features`
- `civic_module_*`

Seat tabs:

- `Review`
- `Laws`
- `Projects`
- `Offices`
- `Archive`

Every mutating UI request carries a server-issued session id and is validated
against player, Realm, screen/block type, world, block position, and interaction
range before mutation.

## Storage / Persistence

Editable definitions:

```text
config/elarion/addons/government/government.yml
config/elarion/addons/government/forms/<form-id>/form.yml
```

Runtime state:

```text
world/elarion/addon-state/government/state.json
```

Runtime state stores compact Realm government state, persisted votes, office
holders, selected identity, selected Realm color, selected form, audience
requests, Republic law votes, typed civic records, archived records, founding-completion
timestamps, active/removed office term statistics, and previous active title
restore pointers.

## Vote Lifecycle

Implemented vote types:

- `REALM_NAME`
- `REALM_COLOR`
- `GOVERNMENT_FORM`
- `FOUNDING_ELECTION`

Republic founding elects one President.

## Audience And Law Lifecycle

Monarchy:

- Citizens may request an audience with the Monarch.
- The Monarch may accept/reject audience requests.
- The Monarch may directly create, archive, and restore laws and project
  records.

Republic:

- Citizens do not submit law drafts.
- The President writes a law title/body and opens a Yes/No citizen vote.
- Approved citizen votes create active laws; rejected votes fail.

## Dependencies

- Core: citizens, Realms, identity presentation, history, authority markers,
  command registration, networking, notifications, and domain events.
- Offerings: Foundation flags `foundation_i`, `foundation_ii`,
  `foundation_iii`.
- Groups: normal group presentation only; Government no longer consumes Groups
  for founding offices.

## Extension Points

- form definitions
- office definitions
- future civic-record effects
- future Government UI sub-screens
- authority checks for future enforcement systems
- Core notification projections
- Core domain events

## Risks

- `GovernmentStateService` still owns vote lifecycle, audience review,
  civic-record finalization, authority cleanup, identity presentation, and
  office assignment in one service. Split vote/civic-record services before
  adding reforms, enforcement, or many record effects.
- Vote expiration is currently checked from the existing server tick path.
  Replace with interval/deadline scheduling before many Realms have concurrent
  elections.
- Command/GameTest coverage for full in-game Civic Forum and Seat actions is
  still needed.

## Do Not Duplicate This System By Creating

- a second Realm identity owner
- a second office/authority owner
- a second voting runtime store
- a second Government UI packet stack
- laws/taxes/treaties inside Core or Groups
- a separate civic-record manager outside Government
