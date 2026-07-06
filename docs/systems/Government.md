# Government System

Status: Implemented foundation, active development.

## Purpose

Government owns Realm civic progression after Offering/Foundation gates:
Realm naming, Realm color selection, government-form selection, founding
faith identity for Theocracy, founding elections, authority offices, authority
chat eligibility, citizen proposals, typed civic records, Government blocks,
Government runtime state, and temporary office-driven active title overrides.

Government does not own Core Realm membership, Core citizens, Economy balances,
Offering progress, Groups membership, taxes, treaties, automated enforcement,
or treasury spending.

## Main Classes

- `addons/government/.../ElarionGovernmentAddon.java`
- `addons/government/.../GovernmentBlockInteractions.java`
- `addons/government/.../service/GovernmentStateService.java`
- `addons/government/.../service/GovernmentProposalDecisionPolicy.java`
- `addons/government/.../service/GovernmentDefinitionService.java`
- `addons/government/.../service/GovernmentUiSessionService.java`
- `addons/government/.../command/GovernmentCommands.java`
- `addons/government/.../network/GovernmentUiOpenPayload.java`
- `addons/government/.../network/GovernmentUiActionPayload.java`

## Entry Points

- Fabric mod entrypoint: `ElarionGovernmentAddon`
- Client entrypoint: `ElarionGovernmentClient`
- Block interactions: Civic Forum and Seat of Rule use
- Commands: `/e government ...`
- Authority chat: `/lc`

## Commands

Admin commands are documented in `wiki/admin/government.md` and
`wiki/admin/commands.md`.

Key command groups:

- definitions: reload/forms/inspect
- state: state/gates/set-form/identity/founding
- reset: `/e government reset <realm>` for targeted admin reset
- authority: cleanup/office assign/office remove
- testing: `/e test government reset [realm]`, `/e test government advance <realm>`
- blocks: block remove

## Network Packets

- `GovernmentUiOpenPayload`: authoritative server snapshot for Civic Forum and
  Seat screens.
- `GovernmentUiActionPayload`: client request for proposal, vote, nomination,
  or module action.
- `GovernmentUiFeedbackPayload`: lightweight S2C notice for stale/rejected
  Civic actions when the open screen can stay in place.

Every mutating UI request carries a server-issued session id. The session is
validated against player, Realm, screen/block type, world, block position, and
interaction range before state changes are allowed.

## GUI / Screens

The Government UI uses shared Core UI primitives but owns two separate screens:

- `CivicForumScreen`: citizen-facing Realm founding and civic participation.
- `SeatOfRuleScreen`: authority-facing proposal review, official records,
  office summaries, and archive actions.

Both screens consume `GovernmentUiOpenPayload`, but they render distinct
layouts and interaction flows instead of sharing one dashboard class. The
payload carries a server-authored primary `authorityLabel` resolved through
Core nicknames, plus semantic row metadata such as row kind, icon id, category,
actor nickname, selected-by-viewer, live vote counts, approval/rejection counts,
thresholds, and timestamps. Client rendering must use these fields instead of
parsing labels like `Your vote` or `Vote`.

The Civic Forum screen covers:

- Realm Name proposal/vote.
- Realm Color vote.
- Government Form vote.
- Founding Faith, only for Theocracy.
- Founding Election, including leader/office candidate voting.
- Citizen proposals, active laws, active projects, office viewing, and civic
  history after founding is complete.

The Seat of Rule screen covers:

- Authority proposal review.
- Final official title/body text for approved proposals.
- Direct authority record creation for laws and projects when the current form
  allows it.
- Realm notices as an authority action that publishes a Realm notification and
  Government chronicle entry, not a permanent Civic tab.
- Active office holder summaries, tenure, and approval/refusal statistics.
- Civic record archive and restore actions.

Both screens use bounded hidden scrolling for long row lists. Inner input
overlays use the same brown/gold Elarion theme: founding identity overlays use
name/tag fields, citizen proposals use title/body/category fields, and Seat
official records use title/body fields. All mutations still go through
server-issued UI sessions and `GovernmentUiActionPayload`.

Civic module routing stays Civic-owned for every Civic screen type:
`civic_name`, `civic_color`, `civic_form`, `civic_theocracy_faith`,
`civic_election`, `civic_features`, and `civic_module_*`. Opening tabs or
submodules from those screens must never swap to Seat snapshots. Empty Civic
states still render their primary action so proposal, faith, name, and leader
nomination flows remain reachable before rows exist. During a founding
nomination phase, eligible citizens see `Nominate Yourself`; after nomination,
their candidate row is marked `You are nominated` until candidate voting opens.

The Civic Forum Current Votes tab keeps founding stages and stage summaries in
the left list. Realm Color, Government Form, and Leadership Election options
render in the right detail panel with their own hidden scrolling. The payload
still carries those options in `formRows` or `officeRows`; the client must not
reuse them as left-list Active/Recent rows. Choice cards use
`selectedByViewer` plus live vote counts from the payload for visual state.

The Civic header presents the official Realm name, current primary authority
and Core nickname (or a form-specific vacancy label), citizen assembly, and
the resolved Realm color. Examples include `President Biggus Testerus`,
`Monarch Terea`, `High Priest Vacant`, and `Delegates 2/3`.

Post-founding Civic tabs are `Current Votes`, `Proposals`, `Laws`, `Projects`,
`Offices`, and `History`. While founding is incomplete, every post-founding tab
is visibly disabled and rejected/stale clicks reopen Current Votes with a
server-authored Civic notice naming the remaining requirement, such as electing
at least one Republic Councilor. Feature modules do not appear as Active/Recent
votes in Current Votes. Proposal rows only show Yes/No vote controls while a
proposal is in the citizen-ratification phase; ordinary pending proposals and
office rows render informational details instead of fake vote controls.
Resolved proposal outcomes can remain visible as Recent Votes for 24 hours
while the full trail is read from Core public history.

Seat of Rule tabs are `Review`, `Laws`, `Projects`, `Offices`, and `Archive`.
Review is the default tab and shows pending authority work directly; it does
not render a second internal list of tab shortcut rows. Laws folds legacy
`civic_rule` records into the law list; Projects shows `realm_project`
records; Archive combines archived records with bounded Government
public-history rows. Seat and Civic share the same compact Government chrome:
attached header/body frame, four equal metadata cells, tabs spanning the full
content width, compact tags, and dark-track progress rows.

The client renderer now uses shared Government row/detail/vote components for
both Civic and Seat. Record rows reserve fixed slots for icon, title, compact
tag, metrics, and secondary time/state text. Detail panels share the same
large icon/title/tag/body/divider structure. Vote and decision rows use dark
tracks with colored fills and only show checkboxes on clickable options; office
rows show holder tenure and seat counts instead of vote labels.

Government UI icons are exact `16x16` PNG textures exported from hand-authored
pixel matrices in `dev/tools/generate_government_icons.py`. The generator is
used for repeatable export, dimension validation, and contact-sheet QA; the
icon silhouettes themselves are authored pixel rows with the Government
gold/parchment/green/red/blue palette.

Government screens refuse to render below a readable `75%` scale. If the
current Minecraft window cannot fit the logical `760 x 500` frame at that
scale, the client shows a themed "window too small" fallback instead of
shrinking text into unreadable pixels.

The current visual reference set for these screens is saved under
`docs/ui/government/` and indexed in `docs/systems/UI_JOURNAL.md`:

- `CF-01-civic-forum-states.png`
- `SR-01-seat-of-rule-states.png`
- `IF-01-government-inner-flows.png`

These references define the target composition for warm root surfaces, framed
header cards, top tabs, category tags, vote/detail panels, modal spacing, and
composed civic identity labels such as `Republic of Oak`.

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
holders, selected identity, selected Realm color, selected form, selected
Theocracy faith identity, citizen proposals, typed civic records, archived
records, founding-completion timestamps, active/removed office term statistics,
and the previous active title for any citizen whose active title is temporarily
replaced by a Government office title.

Office assignment and founding elections unlock and activate the matching Core
title while the citizen holds that office: Monarch, Heir, President, Councilor,
Holy Priest, Synod Member, Delegate, or Officer. Government stores only the
restore pointer; Core remains the owner of title definitions, citizen active
title state, and title rendering. These office title unlocks are temporary:
Government removes them from the citizen's unlocked title list when the citizen
no longer holds the matching office, so they disappear from Collection. Office
removal, authority cleanup, Confederation delegate invalidations, and
Government reset restore the previous active title. True Death cleanup clears
the Government restore pointer for the dead character and promotes a monarchy
heir title when succession happens.

Targeted Government reset clears one Realm's Government runtime state, votes,
proposals, offices, civic records, and Confederation delegate locks. It does not
touch Core Realm membership, Shrine progress, Portal routes, NPC placements, or
Government blocks. It restores any active titles that Government had overridden
for office holders in the reset scope.

Vote windows with no valid ballots reopen the same stage with a fresh 24h
window and publish a Realm notification/domain event. They do not advance the
Realm to the next founding stage.

Government votes use plurality resolution with live option counts and private
voter identities. Ties create a 12h runoff containing only the tied leading
options. Realm name proposals are normalized to one or two Unicode-letter words
in title case; tags remain server-normalized uppercase.

Republic founding is phased: President first, then at least one Councilor.
After the President resolves, the next founding screen is the Councilor phase.
The elected President is not eligible to nominate for, be assigned to, or hold
a Councilor seat in the same Republic. President-only legacy/dev states remain
in the Councilor phase until a Councilor is elected; bind-time reconciliation
only marks founding complete when all required offices already exist.

Citizen proposal flow is server-authoritative after founding. Republic law
proposals open first as citizen petitions in Civic Forum; reaching the citizen
threshold moves them to authority review. Other proposal categories enter
authority review directly. Approved proposals wait for official title/body
finalization, and finalization creates the active civic record under Laws or
Projects. Legacy rule records are shown under Laws. Notices are Realm
notifications plus Government history entries, not records in the Civic Forum.
Enacted and rejected proposals are not kept in the active Proposals list.

Founding-election winners receive an office-term record at resolution time, so
Offices can show holder nickname, election age, occupied seats, and subsequent
law approval/refusal counts without reconstructing those values from history.
Government history and UI rows resolve actor UUIDs through Core citizen
identity and use player-facing event labels; raw UUIDs are not part of normal
Civic or Seat UI, even as missing-identity fallback text.

After founding, removing or resigning the final holder of the form's primary
elected office clears only the founding-election completion marker and creates a
fresh founding-election vote state. Civic Forum returns to nominations and
publishes a Realm notification/domain event. Removing support-office holders or
one holder from a still-occupied multi-seat primary office does not reopen the
election. Startup reconciliation applies the same repair to a persisted
completed Realm whose primary office is already vacant, so vacancies created by
older builds return to player nominations after restart.

## Dependencies

- Core: citizens, Realms, identity presentation, history, authority markers,
  command registration, networking, notifications, and domain events.
- Offerings: Foundation flags `foundation_i`, `foundation_ii`,
  `foundation_iii`.
- Groups: Confederation delegate eligibility and group lock behavior.

Government block snapshots and rejected voting actions include explicit lock
messages naming the blocking Foundation level: Foundation I for Realm naming,
Foundation II for Government-form voting, and Foundation III for founding
elections and Seat of Rule access.

Confederation stores both the delegate citizen UUID and represented group ID in
Government runtime state. Groups remains the source of truth for membership and
leadership. Government locks the represented group while it holds a delegate
seat and releases the lock when that office is removed or vacated.

Theocracy stores its selected faith name/mark in Government runtime state. It
is not a separate Religion addon yet.

## Extension Points

- Government form definitions.
- Office definitions and authority-office metadata.
- Future civic-record effects for laws and projects.
- Future Government UI sub-screens.
- Authority checks for future enforcement systems.
- Core notification projections for civic windows, results, and office changes.
- Core domain events for lifecycle changes that future systems can consume.

## Risks

- `GovernmentStateService` still owns vote lifecycle, proposal review,
  civic-record finalization, authority cleanup, identity presentation, and
  office assignment in one service. Split vote/civic-record services before
  adding reforms, enforcement, or many record effects.
- Seat office management has server-authoritative appoint/remove/resign actions
  for leader-driven support offices. Primary elected offices cannot be
  appointed or removed from Seat actions, but their current holder can resign
  themselves; the final primary-office vacancy reopens its election. Future work
  should add richer delegate-majority proposal UX for Confederation officer
  changes.
- Vote expiration is currently checked from the existing server tick path.
  Replace with interval/deadline scheduling before many Realms have concurrent
  elections.
- Theocracy has faith identity and Holy Priest/Synod elections, but not yet
  doctrine, rituals, holy sites, or succession-crisis UI.
- Command/GameTest coverage for full in-game Civic Forum and Seat actions is
  still needed.

## Do Not Duplicate This System By Creating

- A second Realm identity owner.
- A second office/authority owner.
- A second voting runtime store.
- A second Government UI packet stack.
- Laws/taxes/treaties inside Core or Groups.
- A separate civic-record manager outside Government.
