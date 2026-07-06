package panetina.elarion.addons.underworld.model;

import java.util.UUID;

public record CombatTag(UUID attackerId, long lastHitAt) {
}
