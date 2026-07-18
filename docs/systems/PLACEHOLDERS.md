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
