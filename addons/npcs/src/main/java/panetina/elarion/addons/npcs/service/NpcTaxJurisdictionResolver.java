package panetina.elarion.addons.npcs.service;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.npcs.model.NpcTaxJurisdictionKind;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public final class NpcTaxJurisdictionResolver {
    private final Function<String, Optional<String>> realmOwnerForWorld;

    public NpcTaxJurisdictionResolver(Function<String, Optional<String>> realmOwnerForWorld) {
        this.realmOwnerForWorld = realmOwnerForWorld == null ? ignored -> Optional.empty() : realmOwnerForWorld;
    }

    public PlacedNpcRecord resolve(PlacedNpcRecord record, String policy) {
        if (record == null) throw new IllegalArgumentException("Placed NPC is required");
        String worldId = normalizeWorld(record.worldId());
        String normalized = normalizePolicy(policy);
        if ("auto".equals(normalized)) {
            Optional<String> realm = realmOwnerForWorld.apply(worldId)
                    .filter(value -> value != null && !value.isBlank())
                    .map(value -> value.trim().toLowerCase(Locale.ROOT));
            return realm.map(id -> record.withTaxJurisdiction(NpcTaxJurisdictionKind.REALM, id))
                    .orElseGet(() -> record.withTaxJurisdiction(NpcTaxJurisdictionKind.WORLD, worldId));
        }
        if (normalized.startsWith("realm:")) {
            String realmId = normalized.substring("realm:".length());
            String owner = realmOwnerForWorld.apply(worldId).orElse("");
            if (!realmId.equalsIgnoreCase(owner)) {
                throw new IllegalArgumentException("NPC jurisdiction " + normalized
                        + " does not own placement world " + worldId);
            }
            return record.withTaxJurisdiction(NpcTaxJurisdictionKind.REALM, realmId);
        }
        if (normalized.startsWith("world:")) {
            String configuredWorld = normalized.substring("world:".length());
            if (!configuredWorld.equals(worldId)) {
                throw new IllegalArgumentException("NPC jurisdiction " + normalized
                        + " does not match placement world " + worldId);
            }
            return record.withTaxJurisdiction(NpcTaxJurisdictionKind.WORLD, configuredWorld);
        }
        throw new IllegalArgumentException("Invalid NPC tax jurisdiction policy: " + normalized);
    }

    public static boolean validPolicy(String policy) {
        String normalized = normalizePolicy(policy);
        if ("auto".equals(normalized)) return true;
        if (normalized.startsWith("realm:")) {
            return normalized.substring("realm:".length()).matches("[a-z0-9_.-]+")
                    && !normalized.substring("realm:".length()).isBlank();
        }
        if (normalized.startsWith("world:")) {
            return Identifier.tryParse(normalized.substring("world:".length())) != null;
        }
        return false;
    }

    public static String explicitRealm(String policy) {
        String normalized = normalizePolicy(policy);
        return normalized.startsWith("realm:") ? normalized.substring("realm:".length()) : "";
    }

    private static String normalizePolicy(String policy) {
        return policy == null || policy.isBlank() ? "auto" : policy.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeWorld(String worldId) {
        String normalized = worldId == null ? "" : worldId.trim().toLowerCase(Locale.ROOT);
        if (Identifier.tryParse(normalized) == null) {
            throw new IllegalArgumentException("Invalid NPC placement world: " + worldId);
        }
        return normalized;
    }
}
