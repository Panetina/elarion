# Map And Atlas System

Purpose: future political/fantasy Atlas and minimap-style views for Realms, NPCs, portals, roads, projects, towns, and labels.

Main classes: `WebsiteMapMarker` and `MinecraftProjectionPublisher` provide the
first typed server-to-website marker boundary. A terrain Atlas remains future.

Entry points: future `addons/atlas`.

Commands: future `/e map ...`.

Network packets: future bounded atlas snapshot payloads.

GUI/screens: future fullscreen Atlas and minimap HUD.

Storage/persistence: future server-owned region/marker files under `world/elarion/addon-state/atlas`.

Dependencies: Realms, NPCs, Portals, Offerings, Worlds, visibility rules.

Related systems: Realm visibility, NPC placement, Shrine/project locations, portal access, public history.

Extension points: addons publish bounded markers through
`api.system().webProjections().publishMapMarker(...)`. Marker type, stable ID,
Realm, label, dimension, block position, visibility, active tombstone, and at
most 16 metadata fields are validated by Core. The website exposes active
public markers through `/api/public/world-map`; it never scans chunks or addon
storage. Offerings is the first provider with `map.marker.shrine`.

Risks: building a full terrain minimap too early; chunk scanning/storage bloat; exposing hidden Realm data.

Do not duplicate this system by creating: per-addon website wire formats,
per-addon map marker renderers, raw chunk scans, or client-owned political map
data.
