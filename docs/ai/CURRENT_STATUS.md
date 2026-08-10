# Current Project Status

Bounded handoff snapshot. Code and focused verification are authoritative;
historical completion evidence lives under `docs/ai/archive/`.

## Current Truth

- Target runtime: Fabric 1.21.1. Core owns shared canonical state and services;
  addons own only their domain state and integrate through public APIs/events.
- The Desktop Modpack worktree contains a large uncommitted implementation
  batch. P0.0 has preserved a local recovery snapshot before integration.
- The batch includes the Groups-to-Guilds migration, Yyz Backpack/Trinkets
  integration, typed chat channel work, Government tax/heraldry, NPC quest
  markers, managed-world reset changes, Core UI primitives and documentation.
  None is release-complete until its focused verification and commit pass.
- Backpacks are intended to retain upstream-owned inventory/equip state while
  Elarion owns acquisition policy: no crafting/smithing/dye recipes and four
  Creative/NPC-ready backpack items.
- Guilds is the canonical replacement for Groups. Its player-facing foundation
  is still under verification: typed registration, physical Sigil fees, `G`,
  invitations, hierarchy-safe state and Core chat/context integration.
- Angling is active development but public gameplay remains release-gated off.
- Atlas, Jail, Newspapers, Tablist and Voice Chat Hooks remain shells unless
  their current source and owner documentation state otherwise.

## Active Milestone

P0.0 classifies the protected WIP into focused commits, verifies each slice,
then runs the cross-module build, GameTests and a local dedicated-server smoke
test. `TODO.md` is the execution order; `PLAN.md` is the full roadmap.

## Known Gates

- The two isolated staging releases `20260718-005248` and `20260718-005342`
  require manifest/hash/reference audit before removal.
- Live deployment, production website promotion and public launcher release all
  require separate explicit owner approval.
- Dual-client visual QA is host-limited by native GLFW crashes and must be
  repeated on a capable QA host.
