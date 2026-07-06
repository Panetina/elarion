# Treasury And Economy System

Purpose: own deposited balances, Realm treasuries, physical currency conversion, transaction records, and Economy pulse/governor foundations.

Main classes: `ElarionEconomyAddon`, `ElarionEconomyApi`, `EconomyTransactionService`, `EconomyInventoryService`, `EconomyGovernorService`, `EconomyItems`.

Entry points: `addons/economy` addon initializer and Economy API singleton.

Commands: `/e economy ...`.

Network packets: currently consumed through NPC and Offering payload paths rather than Economy-owned screens.

GUI/screens: NPC banker service UI displays Economy-owned balances through payloads.

Storage/persistence: `world/elarion/addon-state/economy`.

Dependencies: Core history, Core server identity, Core rewards, NPC registry actions.

Related systems: Offerings, NPCs, future market, contracts, portal tickets, Government treasury spending.

Extension points: reward action handlers, NPC action handlers, Economy API.

Risks: direct wallet mutation outside Economy; transaction records without source system/reason; sinks/rewards that bypass audit.

Do not duplicate this system by creating: separate wallets, separate coin managers, or addon-local treasury files.
