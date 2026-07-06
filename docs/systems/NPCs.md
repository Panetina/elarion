# NPC System

Purpose: place static, server-authoritative NPCs with configurable visuals, dialogue trees, conditional text variants, prompts, and registry-driven actions.

Main classes: `ElarionNpcsAddon`, `ElarionNpcEntity`, `NpcPlacementService`, `NpcInteractionService`, `NpcDefinitionService`, `NpcDialogueScreen`.

Entry points: `addons/npcs/src/main/resources/fabric.mod.json`, custom `elarion:addon`, client initializer.

Commands: `/e npc ...`.

Network packets: `NpcDialogueOpenPayload`, `NpcDialogueSelectPayload`, `NpcDialoguePromptSubmitPayload`, `NpcVisualSyncPayload`, close/dismiss payloads.

GUI/screens: `NpcDialogueScreen`, shared Core UI primitives.

Storage/persistence: `world/elarion/addon-state/npcs/placed-npcs.json`.

Dependencies: Core API, Core registries, Core UI theme, optional Economy action handlers.

Related systems: Economy, Offerings, Portals, Quests, Government, Ledger, Public History.

Extension points: dialogue actions, dialogue conditions, conditional node text variants, skin/portrait profiles, NPC tags, service cards.

Risks: duplicating addon-owned state inside NPCs; client-trusted action execution; broad entity scans; hard-coded banker/shop behavior.

Do not duplicate this system by creating: a second NPC manager, hard-coded vendor entity system, or NPC-local wallet/quest/progression storage.
