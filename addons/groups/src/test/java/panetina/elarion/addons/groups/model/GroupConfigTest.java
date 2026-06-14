package panetina.elarion.addons.groups.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupConfigTest {
    @Test
    void defaultsNormalizeUnsafeValues() {
        GroupConfig config = new GroupConfig(
                true,
                -10,
                0,
                0,
                0,
                "",
                "",
                null);

        assertEquals(0, config.creationFee());
        assertEquals(1, config.minTagLength());
        assertEquals(1, config.maxTagLength());
        assertEquals(3, config.maxNameLength());
        assertEquals("[a-z0-9_-]{3,32}", config.idPattern());
        assertEquals("[A-Z0-9]{2,6}", config.tagPattern());
        assertTrue(config.blockedTags().isEmpty());
    }

    @Test
    void copiesBlockedTagSet() {
        GroupConfig config = new GroupConfig(
                true,
                25,
                2,
                6,
                48,
                "[a-z]+",
                "[A-Z]+",
                Set.of("ADMIN"));

        assertEquals(Set.of("ADMIN"), config.blockedTags());
    }
}
