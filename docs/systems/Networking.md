# Elarion Networking System

Purpose: move authoritative snapshots and bounded client intents between server and client.

Main classes: payload records under Core and addons,
`platform/core/.../network/ElarionPacketCodecs.java`, and addon initializers
registering payloads.

Entry points: `PayloadTypeRegistry`, `ServerPlayNetworking`, `ClientPlayNetworking`.

Commands: none.

Network packets: Core identity/theme/character/admin/collection/notification
payloads, NPC dialogue/visual, Shrine UI/contribution, Government UI,
Portal prompt/status/visual, Underworld grave/status, and Mount input.

GUI/screens: NPC and Shrine screens consume network snapshots.

Storage/persistence: none directly; packets refresh from service-owned state.

Dependencies: Fabric networking API, Core/addon services.

Related systems: GUI, NPCs, Offerings, Identity, future Market/Atlas/Government.

Extension points: new typed payload records and server receivers.

Risks: client-trusted mutation, oversized snapshots, repeated packet frameworks.

Do not duplicate this system by creating: custom packet buses when Fabric typed payloads and existing Elarion patterns are enough.

Cross-addon server integrations use `ElarionApi.system().events()` and
`ElarionDomainEvent`; they do not require client packets. Addons emit compact
events after authoritative mutations. Player-facing notification projections
use `ElarionApi.notifications()`, while Core handles persistence and sync.
Events must not carry large snapshots or become a substitute for owner APIs.

## Packet Safety

All Elarion payload string fields must use `ElarionPacketCodecs.writeString`
and `ElarionPacketCodecs.readString` with an explicit maximum length. Server
snapshots are still server-authored, but the codec boundary strips unsafe
control/format characters and clamps strings before decode can crash a client.

Payload list counts must be read through `ElarionPacketCodecs.readBoundedCount`
or equivalent explicit clamping. Client intent packets must carry bounded IDs,
enums, and numeric values only; the server revalidates ownership, distance,
permissions, session state, and current world state before mutating anything.

Outbound snapshot writers must apply the same limits as their decoders. The
Core Collection payload filters invalid actionable IDs, deduplicates IDs within
each scope, and caps snapshots at 32 tabs, 512 entries per tab, and 16 actions
per entry before encoding. If the selected tab is omitted by validation or a
limit, the transmitted snapshot selects its first valid tab. This transport
policy does not authorize actions; Collection providers still validate every
request on the server.

Character Menu profile transport uses `CitizenProfileRequestPayload` and
`CitizenProfileSnapshotPayload`. The request packet carries a target citizen
UUID plus an optional section id. The server derives the viewer from the
connection, builds the snapshot through `CitizenProfileService`, optionally
narrows the response to one visible section, and sends back only filtered
presentation data. A zero target UUID requests the connected player's own
profile. The snapshot packet serializes one bounded `CitizenProfileSnapshot`
with the same caps enforced by `CitizenProfileService`: 16 sections, 24 fields
per section, and 8 cards per section. Strings are clamped at the codec
boundary. The client caches the latest snapshot in `CitizenProfileClientState`
for future UI use and remains read-only; no client profile mutation packet
exists.

Profile aggregation requests are limited to four requests per connected
player per one-second fixed window before any contributor runs. Limiter state
is cleared on disconnect. Contributor failures do not break the Core snapshot;
Core logs one diagnostic per failing contributor and suppresses repeats until
that contributor successfully recovers.

Character onboarding transport uses `CharacterCreationRequirementPayload` for
mandatory creation/cooldown state, `CharacterCreationSubmitPayload` for bounded
name/biography submission, `CharacterRealmAssignmentPayload` for the
server-authored balanced Realm placement panel, and
`CharacterRealmAssignmentConfirmPayload` as the final C2S continue intent.
Realm membership remains server-assigned during submission, but teleporting to
the Realm spawn is gated by the confirm payload so clients can review placement
without silently entering the world.

Identity sync transport uses `IdentitySyncPayload` for player presentation
state visible to each viewer. It carries username, nickname, prefix/suffix,
active title text, active title ARGB color, leader label, Realm name/id,
tab-list visibility, and overhead visibility. The Titles addon renders the
active-title line above players from this payload; configured title colors from
Core `titles.yml` must be sent here rather than hard-coded in the renderer.

NPC dialogue transport uses `NpcDialogueOpenPayload` as the server-authored
session snapshot and `NpcDialogueSelectPayload` /
`NpcDialoguePromptSubmitPayload` as bounded client intents. The open payload
includes an explicit `presentationKind`, and each option includes a stable
  `presentationRole`. The client chooses ordinary conversation, dedicated bank,
  or dedicated trade rendering from those fields; it must not parse localized
  labels or NPC names to infer behavior. Bank amount submission still passes
  through the active NPC session, range, option, prompt, and Economy validation
  path before any wallet or inventory mutation occurs. Trade-node dialogue
  options reject prompts/actions; real BUY mutation uses the dedicated trade
  purchase request path.

NPC bank quote transport uses `NpcBankQuoteRequestPayload` as a bounded C2S
intent containing only NPC id, node id, presentation mode, and amount. The
server revalidates the active NPC session, range, bank node, and visible
Deposit/Withdraw option before asking Economy for a quote. `NpcBankQuotePayload`
returns Economy-authored balance, carried physical currency count,
withdrawal-tax basis points, fee, total, validity, and a short message. Quote
packets never mutate wallet balances or physical Sigils; Confirm still sends
the existing `NpcDialoguePromptSubmitPayload` and Economy revalidates the
transaction before settlement.

NPC trade preview transport uses `NpcTradeSnapshotPayload` as a bounded S2C
catalog snapshot sent only after the server opens a `trade` presentation node.
The snapshot carries NPC/node/catalog IDs, a catalog revision, up to 64
server-built offer previews, prices, and disabled-state text. Each offer
carries an `ItemStack` preview so vanilla tooltip rendering preserves
enchantments, custom names, lore, and attributes without duplicating item
metadata in client UI code. The client may display this data only; it cannot
authoritatively mutate stock, price, tax, or delivery.

`NpcTradeQuoteRequestPayload` is a bounded C2S intent containing only NPC,
node, catalog ID/revision, offer ID, and quantity. The server revalidates the
session and returns `NpcTradeQuotePayload` with Economy-authored subtotal, tax,
total, policy revision, and authority label. Quantity is capped at 64. Quote
packets never mutate currency, treasury, stock, or inventory.

`NpcTradePurchaseRequestPayload` carries purchase UUID, NPC, node, catalog
ID/revision, offer ID, and bounded quantity. It never carries client-authored
price, tax, treasury destination, item stack, display name, reward action, or
stock value. The server revalidates the active session, range, trade node,
catalog revision, offer, item availability, and quote, then records the NPC
purchase journal and settles physical Sigils through Economy. The response
`NpcTradePurchaseResultPayload` is presentation feedback only.
