# Placeholder System

Core exposes the shared placeholder service through
`ElarionApi.get().system().placeholders()`.

## Addon Registration

1. Keep canonical values and caches in the owning addon.
2. Register a namespaced `PlaceholderDescriptor` during initialization.
3. Resolve only immutable request context or an owner-maintained bounded
   snapshot.
4. Add aliases only for compatibility and mark deprecation truthfully.
5. Test visibility, missing data, aliases, cycles, and limits.
6. Document the key here and in the owning addon guide.

Resolvers must never perform storage/history/world/player scans, file or
network IO, mutation, or unbounded parsing. Private data is filtered by the
server-side visibility policy, not hidden after synchronization.

Use `resolveSchema` for bounded event/request-local templates such as Chronicle
metadata. Do not register one global resolver per event instance. Unknown
tokens are preserved; diagnostics are bounded and returned to the caller
instead of logged for every player render.

## Contract

Canonical identifiers are lower-case and namespaced. A descriptor declares its
owner, value type, allowed contexts, required context, visibility, and
missing/unauthorized behavior. Compatibility aliases normalize to that
canonical id and may apply only their declared case transform. Resolution
bounds token count, output length, nesting, cycles, diagnostics, and
request-local memoization.

`ServerIdentityConfig.replace` is bootstrap-only because identity config loads
before the service exists. Government's case-sensitive `%REALM%` remains a
local compatibility adapter until a tested alias migration preserves authored
configuration exactly; Chat retains its component-aware message adapter so
styled text is not flattened.
