# Bank And Economy UI Audit

Date: 2026-07-09

## Scope

This audit covers the dedicated NPC bank service screen and the Economy-owned
money rules that the screen must present. No production code was changed.

Inspected source:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcInteractionService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcDialogueOpenPayload.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcDialogueOptionPayload.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcTradeQuotePayload.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcTradeQuoteRequestPayload.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyInventoryService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTransactionService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/registry/EconomyNpcActions.java`

## Verified Ownership

- Economy owns wallet balances, physical Sigil conversion, withdrawal tax,
  bank interest, transactions, receipts, and settlement.
- NPCs owns conversation state, service presentation, bank screen layout, and
  prompt submission.
- The bank UI currently sends the same bounded NPC prompt request as dialogue.
  The server revalidates session, option, prompt range, and Economy transaction
  before mutating any money or inventory.
- Portal, Shrine, and NPC trade service spending currently use carried physical
  Sigils only. Banked money is not spendable directly for those services.

## Current Behavior

- `NpcInteractionService` sends a generic `NpcDialogueOpenPayload` for bank
  nodes.
- The payload includes the current bank balance through
  `elarion:economy_bank_balance`.
- `NpcBankScreen` renders Deposit/Withdraw tabs, amount input, presets, a Fee
  row, a Total row, and Confirm.
- The screen remembers Deposit/Withdraw mode per NPC after a server feedback
  refresh.
- The Fee row currently renders `0` locally. It does not receive the configured
  withdrawal tax quote before submit.
- Withdrawals still charge the correct tax server-side through
  `EconomyInventoryService.withdraw(...)`, which calls
  `EconomyTransactionService.calculateBankWithdrawalTax(...)`.
- The only current player-visible withdrawal tax detail before this audit is
  feedback after submit, assembled by Economy NPC action feedback metadata.

## Risks

- The visible Fee and Total rows can become misleading when
  `bank.withdrawal-tax-basis-points` is nonzero.
- Client-side fee math would duplicate Economy policy and become wrong once
  taxes, exemptions, dynamic service rules, or future Governor/Admin controls
  expand.
- Extending the generic dialogue open payload with many bank-specific fields
  would bloat a general NPC transport used by ordinary dialogue and trade entry.
- The trade screen already has a better pattern: server quote request, server
  quote payload, compact client rendering, and server revalidation on mutation.

## Recommended Next Slice

Phase 4 Slice 34 should add an Economy-owned bank quote contract and a small NPC
bank quote transport before changing bank visuals again.

Suggested implementation boundaries:

- Add an Economy model/API for bank quotes, covering Deposit and Withdraw.
- Quote Deposit as fee `0`, total equal to the requested amount, and validity
  based on carried physical Sigils.
- Quote Withdraw using Economy's current withdrawal tax policy, total bank
  debit `amount + tax`, and validity based on wallet balance.
- Add dedicated NPC bank quote request/response packets instead of expanding
  generic dialogue payloads.
- Update `NpcBankScreen` to request quotes when mode or amount changes and to
  render Fee/Total only from the latest server quote.
- Keep Confirm using the existing NPC prompt submit path for this slice, so the
  mutation path remains unchanged and server-authoritative.

Explicitly out of scope for the next slice:

- Bank interest UI.
- Admin or Seat of Rule bank-tax editing UI.
- New transaction history screens.
- Direct bank spending for Portal, Shrine, or trader services.
- Physical inventory-capacity prediction beyond the current server mutation
  behavior.

## Verification Needed

- Economy quote unit tests:
  - deposit quote has zero fee
  - withdraw quote includes configured tax
  - insufficient balance invalidates withdraw quote
  - quote does not mutate wallet or inventory
- NPC packet round-trip tests for bank quote request/response.
- Bank screen layout test or targeted compile after wiring the quote.
- Focused build/test:
  `.\gradlew.bat :addons:economy:test :addons:npcs:test`.

