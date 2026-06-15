# PLANS

Future ideas and design direction. This file is not current implementation work.

## Confirmed Future Directions

- Atlas / fantasy political map and minimap.
- Rich Chronicle archives and public-history read models.
- Newspaper, ledger, NPC rumor, and search views backed by indexes or archives.
- Expanded civic systems for Government once the base founding flow stabilizes.
- More reusable UI surfaces built on the shared Core UI primitives.
- Website / bridge integration using explicit APIs and read models, not direct
  raw file reads.

## Active Design Constraints

- Do not expose raw JSONL or raw runtime files as the long-term player-facing
  interface for growing history systems.
- Do not create duplicate managers, services, screens, or persistence layers.
- Keep special-case addon logic behind addon APIs or registries.
- Preserve the current separation between Core truth and addon behavior.

## Notes

- Some older "Contribution" wording has already been replaced by Shrine /
  Offering terminology in the implemented systems.
- Government, Groups, and Portals should continue to grow as modular systems,
  not as one monolithic civic package.
