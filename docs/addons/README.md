# Addon Technical Docs

Source-backed technical notes for Elarion addons. `INDEX.md` is still the main
repository entry point; use this folder after selecting an addon.

## Events And Notifications Requirement

Every active or newly created addon document must state:

- authoritative lifecycle events emitted through
  `ElarionApi.system().events()`
- Personal, Realm, World, or Quest notifications published through Core
- audience, deduplication, expiry, and action ownership
- meaningful events intentionally kept silent to avoid notification spam
- future consumers expected to subscribe, such as Chronicle, newspapers, NPC
  rumors, the website bridge, or diagnostics

Addons must not create their own inbox, notification persistence, HUD rail, or
polling loop. Shell addons may state that they emit no events yet, but must
follow this contract when real behavior is added.

## Maintenance Rule

Use the canonical maintenance matrix in `../../RULES.md`. This folder must stay
source-backed: update the affected addon doc when addon ownership, commands,
config, runtime state, APIs, packets, UI behavior, permissions, events,
notifications, or status changes.

## Active / Implemented Foundations

- `core.md`
- `economy.md`
- `offerings.md`
- `government.md`
- `groups.md`
- `npcs.md`
- `quests.md`
- `portals.md`
- `worlds.md`
- `realms.md`
- `names.md`
- `titles.md`
- `optimization.md`
- `security.md`
- `angling.md`
- `underworld.md`
- `mounts.md`

## Shell / Early Integration Modules

- `atlas.md`
- `jail.md`
- `newspapers.md`
- `tablist.md`
- `voicechat-hooks.md`

## Fast Recovery

Use `../ai/CURRENT_STATUS.md` for the current repository-wide handoff snapshot.
Use the per-addon files in this folder for ownership, source locations,
commands, config/state, events, notifications, and verification notes.

## Reference Exclusion

`addons/angling/reference/**` is upstream reference material. Ignore it unless
Angling porting is explicitly active.
