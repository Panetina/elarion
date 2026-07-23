package panetina.elarion.addons.underworld.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.service.PlayerRestrictionService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UnderworldBanishmentRestrictionTest {
    @Test
    void movementOnlyPolicyCoversCommunicationCombatAndInteractions() {
        assertTrue(UnderworldService.isBanishmentRestrictedAction(PlayerRestrictionService.CHAT));
        assertTrue(UnderworldService.isBanishmentRestrictedAction(PlayerRestrictionService.PORTAL_TRAVEL));
        assertTrue(UnderworldService.isBanishmentRestrictedAction(PlayerRestrictionService.BREAK_BLOCK));
        assertTrue(UnderworldService.isBanishmentRestrictedAction(PlayerRestrictionService.ATTACK_ENTITY));
        assertTrue(UnderworldService.isBanishmentRestrictedAction(PlayerRestrictionService.INTERACT_BLOCK));
        assertTrue(UnderworldService.isBanishmentRestrictedAction(PlayerRestrictionService.INTERACT_ENTITY));
        assertTrue(UnderworldService.isBanishmentRestrictedAction(PlayerRestrictionService.USE_ITEM));
        assertFalse(UnderworldService.isBanishmentRestrictedAction(PlayerRestrictionService.NAMEPLATE));
        assertFalse(UnderworldService.isBanishmentRestrictedAction(PlayerRestrictionService.QUEUED_ADMISSION));
    }
}
