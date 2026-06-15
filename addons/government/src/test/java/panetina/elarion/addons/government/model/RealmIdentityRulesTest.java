package panetina.elarion.addons.government.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class RealmIdentityRulesTest {
    @Test
    void acceptsShortNeutralNamesAndNormalizesTags() {
        assertEquals("Silver Coast", RealmIdentityRules.validateName("  Silver   Coast "));
        assertEquals("OAK1", RealmIdentityRules.validateTag("oak1"));
    }

    @Test
    void rejectsLongAndReservedPoliticalNames() {
        assertThrows(IllegalArgumentException.class,
                () -> RealmIdentityRules.validateName("Three Word Realm"));
        assertThrows(IllegalArgumentException.class,
                () -> RealmIdentityRules.validateName("Oak Kingdom"));
        assertThrows(IllegalArgumentException.class,
                () -> RealmIdentityRules.validateName("Holy Land"));
    }
}
