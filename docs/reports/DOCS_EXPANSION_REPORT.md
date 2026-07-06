# Documentation Expansion Report

## Files Created

Fabric reference:

- `docs/fabric-reference/Registries.md`
- `docs/fabric-reference/Networking.md`
- `docs/fabric-reference/Components.md`
- `docs/fabric-reference/ScreenHandlers.md`
- `docs/fabric-reference/Rendering.md`
- `docs/fabric-reference/Commands.md`
- `docs/fabric-reference/Entities.md`
- `docs/fabric-reference/Datagen.md`
- `docs/fabric-reference/Mixins.md`
- `docs/fabric-reference/PersistentData.md`
- `docs/fabric-reference/FeatureDiscovery.md`

Porting:

- `docs/porting/EventMapping.md`
- `docs/porting/RegistryMapping.md`
- `docs/porting/NetworkingMapping.md`
- `docs/porting/ComponentMapping.md`
- `docs/porting/GuiMapping.md`
- `docs/porting/CommandMapping.md`
- `docs/porting/DatagenMapping.md`
- `docs/porting/RenderingMapping.md`
- `docs/porting/MixinAccessMapping.md`

Systems:

- `docs/systems/NPCs.md`
- `docs/systems/Realms.md`
- `docs/systems/Treasury.md`
- `docs/systems/CommunityContribution.md`
- `docs/systems/Chronicles.md`
- `docs/systems/Teams.md`
- `docs/systems/Maps.md`
- `docs/systems/Permissions.md`
- `docs/systems/GUI.md`
- `docs/systems/Networking.md`
- `docs/systems/Persistence.md`

Architecture and AI navigation:

- `docs/architecture/DEPENDENCY_GRAPH.md`
- `docs/ai/AI_SEARCH_HINTS.md`
- `docs/reports/DOCS_EXPANSION_REPORT.md`

## Files Updated

- `AGENTS.md`
- `CODEX.md`

## Systems Discovered

- Core platform and shared APIs.
- Economy/Treasury.
- NPCs.
- Shrine/Offerings.
- Realms and Realm protection.
- Chronicle/Public History.
- GUI/theme primitives.
- Networking payload pattern.
- Persistence/config/runtime separation.
- Permissions/abilities.
- World management.
- Optimization diagnostics.
- Security evidence/status foundation.
- Angling.
- Future Atlas/Maps.

## Missing Or Unclear Systems

- Government is still mostly future/shell and needs a dedicated system doc after implementation starts.
- Portals are currently shell/future-facing and need a system doc once ticket/access logic exists.
- Ledger is planned but not implemented as a distinct addon yet.
- Quest/Adventure Guild is planned but not implemented yet.
- Contracts are future-facing and should get a system doc when promoted from `PLANS.md`.
- Atlas is documented as future because no `addons/atlas` exists yet.

## External References Successfully Used

- Fabric docs 1.21.1 developer guide: `https://docs.fabricmc.net/1.21.1/develop/`
- NeoForge docs getting started: `https://docs.neoforged.net/docs/gettingstarted/`
- NeoForge primer docs: `https://docs.neoforged.net/primer/docs/`
- Local `external/neoforge` branch `1.21.1`.
- Local Fabric/NeoForge source trees under `external/`.

## External References That Could Not Be Resolved Exactly

The requested remote branches exist:

- `FabricMC/fabric-api` branch `1.21.1`
- `FabricMC/fabric-loom` branch `exp/1.8`
- `FabricMC/yarn` branch `1.21.1`
- `neoforged/NeoForge` branch `1.21.1`

The local Fabric checkouts were not switched in this documentation-only pass:

- `external/fabric-api` remains on `26.2`.
- `external/fabric-loom` remains on `dev/1.17`.
- `external/yarn` remains on `1.21.11`.
- `external/example-mods/fabric-example-mod` remains on `26.1.2`.
- `external/neoforge` is already on `1.21.1`.

Reason: the requested scope for this pass was documentation/index files only.

## Suggestions For Future Documentation Passes

- Decide whether `external/` should be ignored, submodules, or branch-pinned local clones.
- If kept as local references, check out:
  - `external/fabric-api` to `1.21.1`
  - `external/fabric-loom` to `exp/1.8`
  - `external/yarn` to `1.21.1`
- Add system docs when Government, Portals, Ledger, Quest, Contracts, Market, and Atlas become real modules.
- Add source-location indexes for common classes after the Shrine Offering pass is completed and the repository is stable.
- Add docs for website bridge integration before implementing website connectivity.
