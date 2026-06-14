# Elarion Future Plans

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

This file is the future design book for Elarion. It is not the active TODO
list. Move work into `TODO.md` only when ownership, config, runtime state,
interaction surface, events, and tests are clear.

## Promoting Plans To TODO

Before moving an idea into `TODO.md`, define:

- owner addon or Core owner
- config files and generated defaults
- runtime state files
- APIs, events, abilities, and registries consumed or exposed
- commands, blocks, NPCs, GUI screens, or other interaction surfaces
- history/progression events emitted
- performance budget and cache strategy
- tests required

If those are not known, keep the idea here.

## Season 2 Purpose

Season 2 turns Elarion into a social civilization game built from modular
systems:

- Realm identity and public memory
- Realm onboarding and active needs
- contribution projects and society progression
- NPCs with placeable identities, skins, dialogues, and GUI interactions
- economy, banking, treasuries, tickets, and adaptive prices
- closed Worldheart Market and manual stall trade
- Adventure Guild quests and quest HUD
- government forms, offices, laws, reforms, and treasury spending
- diplomacy, treaty flags, embargoes, alliances, and seclusion
- objective war, invasion windows, war fatigue, and lawful conflict records
- Citizen Ledger, Chronicles, newspapers, rumors, monuments, and ceremonies

Core remains the canonical owner of citizens, Realms, titles, nicknames,
statuses, abilities, identity, relationships, rewards, progression, and
history. Addons interpret Core truth through APIs, events, registries, ability
checks, and history/progression events.

## Design Pillars

Elarion should optimize for identity attachment, not manipulative compulsion.

Players should return because:

```text
My Realm needs me.
My friends notice me.
My work remains.
My enemies remember me.
My vote matters.
My name is part of the world.
```

Design for:

- autonomy: multiple roles, votes, quests, offices, markets, and titles
- competence: visible contribution, mastery, ranks, projects, and progression
- relatedness: Realm chat, allies, shared danger, ceremonies, and public work
- identity: titles, ledgers, offices, monuments, newspapers, and Chronicles
- rivalry: markets, scoreboards, embargoes, treaties, wars, and elections
- anticipation: market days, summits, elections, festivals, and invasion windows

Avoid:

- harsh daily login streaks
- random reward gambling
- permanent base destruction by default
- offline raid anxiety
- passive income shops
- invisible corruption
- untraceable theft
- public shame boards for weak, new, inactive, or low-contribution players

## Social Memory

Every meaningful action should enter a memory system when appropriate:

- durable history
- Citizen Ledger
- Chronicle archives
- newspapers
- NPC rumors
- notifications
- Realm monuments and plaques
- office and election records
- war records
- treasury audit logs
- project completion ceremonies

Recognition should matter more than raw reward inflation. Examples:

```text
Aurel contributed the final blocks to the East Bridge.
Mira defended Oak during the Ash invasion.
Niko completed 7 courier contracts this week.
The Realm of Stone has elected its first Council.
```

Major systems should emit reader-friendly history prose so weekly Chronicles
can later generate immersive archive volumes without rewriting raw events.

## Hosting, Performance, And Modpack Baseline

The baseline target is an unknown online host with possible shared CPU,
variable disk, and network variance. Stronger hardware is extra headroom, not
a design requirement.

Elarion must also share the server with a Vanilla+ modpack. Expected companion
mods may include food/farming addons, Scholar-style knowledge systems,
Exposure-style photos/artifacts, Simple Voice Chat, custom villagers, terrain
generation, mobs, backpacks, storage utilities, and similar gameplay mods.

Design rules:

- no global per-tick scans
- no config parsing during gameplay actions
- no background thread world/entity/player/inventory mutation
- event-driven updates before polling
- sampled diagnostics before persistent snapshots
- bounded queues for IO, compute, and server-thread apply work
- registry IDs, tags, and optional adapters instead of vanilla-only assumptions
- conservative defaults until final host profiling proves spare headroom

Bobby and Distant Horizons are compatibility targets only. Server-side
authority, protection, visibility, anti-cheat, and gameplay rules must never
trust client-side render/cache state.

## Shared Registry Direction

Future data-driven systems should use shared registries where practical.

Conditions are used by NPC dialogues, government pages, quests, vendors,
project requirements, portals, trade, and titles.

Actions are used by NPC buttons, government actions, quest results, vendors,
progression projects, and milestone events.

Requirements are used by projects, quests, titles, portals, laws, tickets, and
vendor offers.

Milestone events are used by society progression and contribution projects.

Unknown IDs must fail config validation clearly. Addon-specific handlers should
register with the shared infrastructure instead of hard-coding special cases in
Core.

## Realm Society

Joining a Realm should be a ritual, not only a teleport.

Future onboarding should include:

- welcome message from the Realm
- first civic quest
- small Realm badge or starter title hook
- contribution prompt
- NPC introduction
- online or notable citizen list
- one active public project that can be helped immediately

Example:

```text
Welcome to the Realm of Oak.
Current Goal: Restore the Council Hall.
Needed: 92 stone bricks, 12 books, 3 zombie kills.
Your first contribution will be recorded in the Citizen Ledger.
```

Realm active needs should create small useful tasks without punishing absence:

- project materials
- food and crops
- arrows, potions, armor, tools
- courier work
- votes needed
- scouts or defenders
- market delivery requests

Soft roles should emerge from play instead of rigid classes: builder, scout,
fighter, trader, diplomat, miner, courier, farmer, defender, lorekeeper,
newspaper writer, and civic organizer.

## NPC Placement And Dialogue System

NPCs are the main future interaction layer for Worldheart, Realm onboarding,
quests, economy, government, markets, portals, titles, ledgers, and lore.

NPCs should feel like characters, not hard-coded menus.

The NPCs addon should support:

- placeable NPCs with durable IDs
- configurable names, descriptions, skins, portraits, and profiles
- static NPCs by default for performance
- optional controlled entity or modded NPC adapters later
- server-authoritative interaction state
- branching dialogue trees
- conditional options
- registered actions
- per-player dialogue/session state
- GUI screens with avatar/portrait, dialogue text, responses, and action
  buttons
- history events for meaningful actions

Config should define content:

```text
config/elarion/addons/npcs/
  npcs.yml
  skins.yml
  dialogues/<dialogue-id>.yml
```

Runtime state should store placement and session data:

```text
world/elarion/addon-state/npcs/
  placed-npcs.json
  sessions/
```

NPCs should call other systems through registered actions. Example actions:

- open bank
- buy or use portal ticket
- open market entry
- open seller permit
- open quest board
- accept or complete quest
- open contribution project
- open government page
- open title selection
- open ledger view
- emit history
- run reward

Do not hard-code `banker`, `portal_keeper`, or `quest_officer` logic in Java.
Define character content in config and route behavior through action handlers.

Important NPC examples:

- Worldheart Banker
- Market Gatekeeper
- Market Registrar
- Portal Keeper
- Backpack Vendor
- Quest Officer
- Realm Registrar
- Treasury Clerk
- Government Clerk
- Chronicle Keeper
- rumor/lore NPCs
- future Mount Stablemaster
- future Plot Registrar

Performance rules:

- static/event-driven NPCs first
- no global NPC AI scans
- no pathing unless the feature truly needs it
- sync dialogue state only on open, option click, state change, reload, or login
- validate all GUI actions server-side
- keep session state compact and disposable when expired

## Contributions And Society Progression

Contribution projects should be the emotional backbone. Players should feel
they placed stones in a civilization, not donated into a number.

Default society pillars:

- infrastructure
- economy
- governance
- military
- culture
- portalcraft

Default society ranks:

- Founding Camp
- Settlement
- Village
- Town
- City
- Nation
- Great Realm

Projects may require:

- items
- blueprint blocks
- currency
- Realm treasury funds
- kills
- quest completions
- society pillar levels
- society rank
- government approval
- leader or office approval
- specific titles or abilities
- time windows
- discoveries
- history events
- unique contributors
- active citizen votes
- market deliveries
- defense objectives
- crops, food, ores, and tools

Public good projects should unlock Realm-wide benefits when enough people help.
Contributors receive visible credit and eligibility, while inactive or low
contribution players are not punished harshly.

Major projects should support:

- visible progress
- hologram or blueprint rendering
- mixed requirements
- unique contributor requirements
- personal ledger credit
- Realm-wide unlocks
- completion ceremony
- monument or plaque credit
- history, newspaper, NPC rumor, and Chronicle entries

## Blueprint Building

Use an internal blueprint format first. Optional importers can be added later:

- `.litematic`
- `.schematic`
- JSON blueprint

Rendering and placement rules:

- client caches blueprint data
- server sends compact progress state
- no full blueprint sync every tick
- render by chunk/section
- configurable hologram range
- client option to disable holograms
- batched placement only
- resume after restart
- place only in loaded chunks
- check protection and ownership
- pause and report conflicts instead of overwriting protected builds

## Economy Philosophy

Sigil should be official Worldheart/system money. It should not replace Realm
barter, Realm scarcity, or player trade in scarce resources.

Realms remain:

- barter-heavy
- resource-gathering heavy
- building and contribution focused
- locally cooperative
- shaped by scarcity and specialization

Worldheart is:

- sigil-heavy
- official market territory
- NPC service center
- ticket and license hub
- backpack/vendor hub
- future mount, plot, and premium convenience hub

Sigil should be used for access, convenience, official trade, licenses, tickets,
services, prestige, and progression accelerators. Sigil should not let NPCs sell
unlimited scarce resources or erase inter-Realm trade.

## Economy Addon

The Economy addon owns:

- currency item
- wallets
- Realm treasuries
- deposits and withdrawals
- bank state
- interest
- transaction history
- Economy Governor
- adaptive price state
- economy reward/action handlers
- economy admin commands

Default currency:

```text
elarion:sigil
```

Players can carry physical sigils, deposit them into wallets, withdraw them, and
spend wallet balance in official GUI flows. Physical sigils can be risky because
they may be lost like normal items.

Interest is optional and conservative:

- weekly interval
- 0.5 to 1 percent default range
- cap per interval
- active players only by default
- disabled or reduced when the economy overheats

Treasury transactions must be atomic and auditable.

## Closed Worldheart Market

Do not implement a dedicated merchant hierarchy or separate trade political
class. Use a broader Worldheart Market System.

The closed market district should use:

- 1 sigil entry fee by default
- temporary market access
- first-entry-free option
- re-entry grace period
- 15 sigil seller session fee by default
- seller must be online and present
- no passive offline selling
- manual stall sessions
- server-validated transaction GUI
- market transaction tax

Inside the closed market:

- block casual item dropping for trade
- block container bypasses where practical
- block hopper/dropper/dispenser bypasses where practical
- allow only official trade GUI flows
- handle death drops and normal gameplay drops separately

Relationship market rules:

- Same Realm: free barter inside the Realm, normal Worldheart market access.
- Allied Realms: possible treaty discounts; direct trade requires treaty flag.
- Neutral Realms: official Worldheart market only.
- Embargoed Realms: trade may be blocked between affected Realms.
- Hostile Realms: trade blocked; war loot is separate.
- Hidden Realms: excluded from public trade listings.

## Player Contract System

Future Worldheart contract gameplay should be a server-verifiable work-order
layer, not free-form player promises.

Planned interaction surfaces:

- `Contract Paper` item
- NPC or desk in Worldheart
- contract creation, acceptance, progress, completion, expiry, and cancel GUI

Contract rules:

- reward is escrowed immediately on creation
- creator pays a non-refundable creation fee
- completion takes a small sink tax from the reward
- optional cancellation fee can exist later
- the first version must use server-checkable templates, not open text

Good contract types:

- delivery
- work order
- Realm public work
- trade contract
- bounty or PvP only after lawful conflict systems exist

Public memory and recognition:

- acceptance, completion, failure, expiry, cancellation, and payment emit history events
- meaningful completions also feed Citizen Ledger recognition
- contract prose should be readable in history, newspapers, ledgers, and future Chronicle summaries

Design constraints:

- no scam-prone free-form contracts
- no server judgment of vague text
- no dependency on NPCs selling infinite resources
- no duplicate ownership of economy, ledger, or history state

Economy effect:

- creation fee is a sigil sink
- completion tax is a sigil sink
- escrow prevents fake rewards and scam contracts
- Realm contracts can create useful public sigil flow without replacing player trade

## Economy Governor And Adaptive Pricing

The Economy Governor monitors economic health and eventually suggests or makes
bounded adjustments.

Modes:

- `OFF`
- `MONITOR_ONLY`
- `SUGGEST_ONLY`
- `AUTO_LIMITED`
- `AUTO_FULL`

Development default is `MONITOR_ONLY`. `AUTO_FULL` must never be the default.

Track:

- total sigil supply
- wallet, bank, treasury, and inventory sigils where practical
- sigils created and destroyed by day/week
- faucet:sink ratio
- quest payouts
- market entry fees, seller fees, and market taxes
- portal tickets bought and consumed
- backpack, license, plot, mount, and NPC purchases
- market buyers, sellers, and transaction volume
- top player and Realm concentration
- active player average and median balances

Health states:

- `DEFLATIONARY`
- `HEALTHY`
- `WARM`
- `INFLATIONARY`
- `OVERHEATED`
- `STAGNANT`
- `CONCENTRATED`

Adjustment levers:

- quest sigil rewards
- number of sigil quests offered
- treasure sigil chance
- NPC prices
- ticket prices
- backpack prices
- seller fee
- market access duration
- market transaction tax
- bank interest and fees
- plot rent
- mount license costs

Every automatic adjustment needs base value, min/max bounds, maximum daily and
weekly change, reason text, and admin visibility. Prices must not change during
open transactions.

Adaptive NPC prices should mostly affect tickets, backpacks, licenses,
permits, cosmetics, plot rent, mount services, bank fees, market fees, and
event entries. Avoid dynamic pricing for basic food, basic blocks, scarce Realm
resources, and core survival items.

## Ticketed Portals

Nether and End access should use tickets, not cooldowns by default.

Recommended defaults:

- Nether ticket: 25 sigils, single-use
- End ticket: 150 sigils, single-use
- tickets consumed on entry
- no cooldowns by default
- ticket availability depends on Realm progression and portal state
- ticket prices have base/min/max bounds for future Economy Governor control

Future ticket types:

- Worldheart Market Pass
- dungeon ticket
- boss arena ticket
- mount trial permit
- plot visit permit
- special event ticket

## Government And Diplomacy

Government addon owns government forms, Realm government runtime state,
government blocks, founding votes, reform votes, offices beyond Core leader,
laws, taxes, treasury spending proposals, treaty metadata, and government UI.

Government forms live in folders:

```text
config/elarion/addons/government/forms/<form-id>/
  form.yml
  offices.yml
  actions.yml
  laws.yml
  transitions.yml
```

Adding a government form should mostly mean copying a folder, editing YAML,
reloading, and testing.

Politics should create consequence, status, and accountability:

- offices grant real authority
- treasury spending is audited
- laws are typed and validated
- reforms require high approval
- recalls have cooldowns
- campaign promises and outcomes can become public memory

Recommended cadence:

- leader term: 3 real weeks
- council term: 2 real weeks
- reform threshold: 80 percent active-citizen approval by default
- law and treasury votes use simple yes/no first
- leadership elections can later use richer ballot systems

Diplomacy should be a ritual, not a menu. Major two-Realm decisions should
eventually require leaders to be physically present at a Worldheart summit room
or decision table. Leaders draft treaty flags, see costs and benefits, sign,
and then citizens ratify.

Treaty flags can include:

- direct trade
- portal discount
- shared map visibility
- alliance chat
- military aid
- embargo protection
- ticket subsidy
- market tax discount
- defense pact

`ALLY` should represent treaty-backed cooperation, not an automatic
all-benefits state.

## Active And Inactive Citizens

Default inactivity rule:

- inactive after 14 real-world days
- excluded from active population
- excluded from vote denominator
- skipped for ordinary rewards
- still receives mail
- remains in citizen storage
- reactivates on login

Evaluate lazily during population, vote, reward, and office eligibility checks.
Do not scan all citizens every tick.

## War, Invasions, And Rivalry

Conflict should create stories, demand, and identity without deleting months of
work.

Use objective-based war instead of destructive base griefing:

- capture banner
- generated supply raid
- temporary outpost siege
- caravan ambush
- relic contest
- portal disruption
- border shrine capture

War should require:

- political proposal
- citizen vote
- declaration cost
- supplies
- public countdown
- scheduled invasion window
- victory condition
- peace terms

Defenders and support players must be rewarded, not only attackers. Track
healing, supplies, scouting, objective denial, banner recovery, and time spent
defending when practical.

Separate:

- lawful war kills
- unlawful kills
- duel kills

Add war fatigue to prevent constant war. Fatigue increases declaration cost,
reduces repeated war reward, and falls through peace or time.

Anti-stagnation tools:

- max one active alliance at launch
- alliance upkeep
- treaty expiration
- treaty flags instead of all benefits
- scarce Realm resources
- embargo incentives
- market tax competition
- Realm-specific quest demand
- public prestige for independence
- separate defense pacts and trade pacts

## Adventure Guild And Quests

Adventure Guild is the main active sigil faucet.

Quest types:

- kill
- boss
- fetch
- delivery
- exploration
- building
- Realm contract
- bounty
- achievement reward
- title unlock reward
- treasure

Quest rewards should support bundles:

- sigils
- items
- ledger reputation
- title progress
- badge progress
- contribution credit
- tickets
- discount vouchers
- Realm treasury grants

Quest rewards and availability must respond to Economy Governor state. When
money is abundant, reduce sigil quests and offer more material, reputation,
title, badge, and contribution rewards. When money is scarce, increase beginner
sigil opportunities within bounds.

Anti-abuse rules:

- no infinite same quest farming
- daily/weekly caps
- rotating quest pools
- no NPC resale loops
- meaningful player damage for kill quests
- delivery/building validation

The quest HUD should become the third left-side icon and open a compact drawer
with active quests, progress, rewards, timers, tracking, and cancel controls.

## Citizen Ledger

Ledger tracks public civic reputation and activity:

- activity
- contribution
- government service
- voting participation
- office terms
- trade reputation
- quest completion
- war/legal combat stats
- unlawful kills
- badges

Do not store admin moderation notes in public ledger.

Public badges can include:

- Founder
- Veteran Citizen
- Former Leader
- Former Councilor
- War Veteran
- Major Contributor
- Trusted Trader
- Lawbreaker
- Exiled
- Oathbreaker
- Diplomat
- Public Benefactor

Badges should be config-driven.

## Chronicles

Chronicles are weekly immutable archive records generated from history events,
not physical items.

Future behavior:

- one Chronicle per calendar week
- configurable event categories
- durable archive state
- OP-registered chiseled bookshelf display slots
- assign next free slot in registration order
- queue unassigned volumes when displays are full
- clicking a slot opens the book-reading GUI directly
- no item is given, so volumes cannot be stolen, moved, duplicated, or dropped

Chronicles should read like immersive Realm history, not raw log dumps.

## Recovered Folklore Archive

Curated Folklore is separate from generated Chronicles.

- Folklore volumes preserve stable-numbered histories and character accounts
  from the lost first age.
- Unique Echo Scrolls can be hidden in ruins, old roads, abandoned halls,
  exploration sites, quests, or administrator-curated locations.
- Finding a scroll begins a server-verifiable return quest.
- Returning it to the Worldheart archive permanently unlocks the matching
  read-only volume and records the discoverer.
- The deposited object is consumed into archive state; no movable book item is
  given back.
- Static Folklore and generated Chronicles may share reader and shelf
  presentation infrastructure while retaining separate definition and runtime
  ownership.
- NPC archivists, newspapers, notifications, and ceremonies may react to major
  recoveries.
- Discovery state must be compact, globally claimable where required, durable
  across restart, and protected against duplicate loot or repeated rewards.

## Long-Term Sinks

Launch sinks:

- market entry fee
- seller session fee
- Nether ticket
- End ticket
- backpacks
- optional bank fees
- market transaction tax

Early and mid-season sinks:

- riding licenses
- mount permits
- stable slots
- cosmetic permits
- newspaper publishing fees
- Realm announcement fees
- contribution project sigil options
- Worldheart plot leases
- premium vaults
- event entry tickets

Worldheart plots are future prestige/convenience spaces. They should be small,
limited, lease-based, rent-charged, inactive-safe, and restricted from passive
selling or mass storage abuse.

## Season 2 Implementation Order

1. Core registry expansion and config-reference validation.
2. Economy data and Economy Governor foundation.
3. Currency, wallets, banking, treasuries, and transactions.
4. NPC placement, skin/profile config, dialogue loader, and interaction GUI.
5. Closed Worldheart Market and manual seller sessions.
6. Ticketed portals.
7. Quest definitions, quest economy, and Adventure Guild.
8. Contributions, society pillars/ranks, and milestone execution.
9. Blueprint building progression.
10. Government forms, offices, votes, laws, and treasury audit.
11. Diplomacy, treaty flags, summits, and war proposals.
12. Ledger, inactivity, recognition feeds, and Chronicles.
13. Long-term sinks such as mounts, plots, premium vaults, events, and
    prestige cosmetics.

## Season 2 Gameplay Loop

Players join a Realm and receive identity, chat, safety, and a first civic
task.

NPCs and Realm onboarding point them toward active needs and visible public
projects.

Players gather, explore, fight, build, trade, and complete quests.

Adventure Guild quests, achievements, titles, and treasure inject official
currency.

Players carry risky physical sigils or deposit them safely in Worldheart banks.

Players spend sigils to enter the closed Worldheart Market, buy tickets, use
NPC services, and access premium convenience.

Citizens donate blocks, items, kills, quest completions, and currency into
progression projects.

Contribution is recorded publicly through history, ledger, monuments,
newspapers, NPC rumors, and future Chronicles.

Milestones unlock society ranks, government forms, laws, trade, portals, NPC
branches, vendor offers, and new Realm capabilities.

Citizens choose government forms, hold offices, pass laws, set taxes, and
reform government with high approval.

Politics decides how shared systems are used, and treasury spending makes those
choices costly and visible.

Sellers manually trade in the closed Worldheart Market through official stall
sessions, not passive offline shops.

The Economy Governor watches inflation, supply, demand, concentration, and
market activity.

Relationships, treaties, embargoes, alliances, and portal access create
political pressure.

Conflict creates demand for supplies, fighters, builders, scouts, couriers, and
diplomats. War creates stories but not permanent ruin.

Citizen Ledger, Chronicles, NPC rumors, newspapers, monuments, and ceremonies
make the world remember.

The target result: survival resources create dependence, contribution projects
create shared pride, currency creates public choices, politics allocates power,
diplomacy creates uncertainty, war creates stories, and history makes players
care because their names remain part of the world.
