package panetina.elarion.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElarionNotificationCategoryTest {
    @Test
    void parsesQuestCategory() {
        assertEquals(ElarionNotificationCategory.QUEST, ElarionNotificationCategory.parse("quest"));
        assertEquals(ElarionNotificationCategory.WORLD, ElarionNotificationCategory.parse("world"));
    }

    @Test
    void filtersCategoriesIntoHudGroups() {
        assertTrue(ElarionNotificationCategory.MAIL.matchesFilter("personal"));
        assertTrue(ElarionNotificationCategory.REWARD.matchesFilter("personal"));
        assertTrue(ElarionNotificationCategory.GOVERNMENT.matchesFilter("realm"));
        assertTrue(ElarionNotificationCategory.WORLD.matchesFilter("world"));
        assertTrue(ElarionNotificationCategory.QUEST.matchesFilter("quest"));
        assertFalse(ElarionNotificationCategory.QUEST.matchesFilter("realm"));
        assertFalse(ElarionNotificationCategory.REALM.matchesFilter("quest"));
        assertFalse(ElarionNotificationCategory.WORLD.matchesFilter("realm"));
    }
}
