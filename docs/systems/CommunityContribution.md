# Shrine And Offering System

Purpose: server-wide and Realm/location-scoped public projects through Shrines of Foundation and player offerings.

Main classes: `ElarionOfferingsAddon`, `OfferingService`, `OfferingDefinitionService`, `OfferingStorage`, `ShrineOfFoundationBlock`, `ShrineOfFoundationScreen`.

Entry points: `addons/offerings` addon initializer, Shrine block interaction, Offering registry actions.

Commands: `/e offerings ...`.

Network packets: `ShrineUiOpenPayload`, `ShrineContributionSubmitPayload`.

GUI/screens: `ShrineOfFoundationScreen`, shared Core UI primitives.

Storage/persistence: `world/elarion/addon-state/offerings/state.json`, `projects.json`, `anchors.json`.

Dependencies: Core history, Core rewards, Core active citizen eligibility, Economy for banked currency offerings.

Related systems: Economy, Realms, Chronicles, NPCs, future Ledger and ceremonies.

Extension points: project YAML, milestone actions, event requirements, reward definitions, and API-driven Shrine display-name overrides.

Risks: trusting client contribution amounts; storing runtime progress in config; duplicating Economy or Realm ownership.

Do not duplicate this system by creating: a separate contribution block system, untracked project progress counters, client-owned donation logic, or quest-owned Shrine progress.
