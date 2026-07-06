# Chronicle And Public History System

Purpose: store durable audit history, build indexes, generate weekly Chronicles, and serve bounded public-memory views.

Main classes: `HistoryService`, `HistoryStorage`, `HistoryIndexStorage`, `ChronicleArchiveStorage`, `ElarionPublicHistoryApi`.

Entry points: Core services, history commands, addon history emissions.

Commands: `/e history ...`, `/e history chronicle ...`.

Network packets: none yet; future GUI/search/news views should use bounded public-history APIs.

GUI/screens: future Chronicle bookshelf, newspaper, Ledger, NPC rumor, and search views.

Storage/persistence: `world/elarion/history`, `history-index`, `chronicles/weekly`.

Dependencies: Core task service, config recording policy, addon event emissions.

Related systems: Newspapers, Ledger, NPC rumors, Offerings, Government, Economy.

Extension points: `api.history()`, `api.publicHistory()`, category/type filters, Chronicle text.

Risks: raw JSONL scans for player-facing views; noisy event spam; missing chronicle text for major events.

Do not duplicate this system by creating: addon-local history logs, separate newspaper event storage, or unbounded GUI searches.
