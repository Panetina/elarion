# Map And Atlas System

Purpose: future political/fantasy Atlas and minimap-style views for Realms, NPCs, portals, roads, projects, towns, and labels.

Main classes: none implemented yet.

Entry points: future `addons/atlas`.

Commands: future `/e map ...`.

Network packets: future bounded atlas snapshot payloads.

GUI/screens: future fullscreen Atlas and minimap HUD.

Storage/persistence: future server-owned region/marker files under `world/elarion/addon-state/atlas`.

Dependencies: Realms, NPCs, Portals, Offerings, Worlds, visibility rules.

Related systems: Realm visibility, NPC placement, Shrine/project locations, portal access, public history.

Extension points: marker providers and visibility policies.

Risks: building a full terrain minimap too early; chunk scanning/storage bloat; exposing hidden Realm data.

Do not duplicate this system by creating: per-addon map marker renderers or client-owned political map data.
