# Names Addon

Status: Implemented.

## Purpose

`addons/names` owns client-side nameplate and tablist presentation hooks for
Elarion identity.

## Main Source

- `addons/names/src/main/java/panetina/elarion/addons/names/`

## Ownership

Core owns identity truth and sync payloads. Names renders the synchronized
identity data and must not become a second identity store.

## Notes

- Visibility and display behavior should use Core identity APIs.
- Overhead names and titles are public; tablist visibility is separately
  controlled by Core identity sync so non-global Realm citizens can be hidden
  from outsiders while remaining visible to their own Realm.
- The Core tablist hook filters hidden entries, sorts visible players by
  official Realm name, and renders true Realm header rows when no vanilla
  scoreboard objective is active. Scoreboard-objective tab rendering falls back
  to vanilla to avoid breaking score displays.
- Keep future realm-visibility settings configurable, not hard-coded.
