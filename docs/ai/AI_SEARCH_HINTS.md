# AI Search Hints

Use the machine-readable `docs/ai/routes.json` and the bounded context command
instead of loading broad navigation documents:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\ai-context.ps1 -Task "<task>" -Mode explore -BudgetTokens 6000 -Format markdown
```

The command selects authority docs, current source, relevant tests, and
verification obligations. Use `-Format json` for tool integration.

Rules:

- Read full source before editing a file.
- Expand deliberately when confidence is insufficient; never guess to satisfy
  a token budget.
- Exclude `docs/ai/archive/**`, `external/**`, and
  `addons/angling/reference/**` from ordinary work.
- Source decides implementation reality. `RULES.md` decides policy, and
  `INDEX.md` is the complete human navigation/ownership index when a deliberate
  broader lookup is required.
- Apply the documentation maintenance matrix in `RULES.md` before handoff.
