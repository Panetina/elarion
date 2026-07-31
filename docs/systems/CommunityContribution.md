# Shrine And Offering System

Purpose: server-wide and Realm/location-scoped public projects through Shrines of Foundation and player offerings.

Main classes: `ElarionOfferingsAddon`, `OfferingService`, `OfferingDefinitionService`, `OfferingStorage`, `ShrineOfFoundationBlock`, `ShrineOfFoundationScreen`.

Entry points: `addons/offerings` addon initializer, Shrine block interaction, Offering registry actions.

Commands: `/e offerings ...`.

Network packets: `ShrineUiOpenPayload`, `ShrineContributionSubmitPayload`.

GUI/screens: `ShrineOfFoundationScreen`, using the shared civic shell, selected
tabs, compact rows, reward slots, numeric modal, and native item tooltips.

Storage/persistence: `world/elarion/addon-state/offerings/state.json`,
`projects.json`, `anchors.json`. Recoverable null collections/rows are
normalized before bind so valid project progress from the same parsed snapshot
remains available. The persisted anchor map is canonical; a runtime-only
world-and-block-position index is rebuilt on bind and maintained on anchor
mutations so ordinary Shrine interaction does not scan all anchors.

Config reload safety: Offering project definitions and Shrine UI config are
committed as one service snapshot. A failed second-stage UI load preserves the
previous active project/UI pair.

Dependencies: Core history, Core rewards, Core active citizen eligibility,
Economy for carried physical-currency offerings and audited refunds.

Related systems: Economy, Realms, Chronicles, NPCs, future Ledger and ceremonies.

Extension points: project YAML, milestone actions, event requirements, reward definitions, and API-driven Shrine display-name overrides.

Risks: trusting client contribution amounts; storing runtime progress in config; duplicating Economy or Realm ownership.

Do not duplicate this system by creating: a separate contribution block system, untracked project progress counters, client-owned donation logic, or quest-owned Shrine progress.
