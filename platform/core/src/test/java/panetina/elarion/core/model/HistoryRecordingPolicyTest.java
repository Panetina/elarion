package panetina.elarion.core.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HistoryRecordingPolicyTest {
    @Test
    void disablesCategoriesWhenDefaultCategoryIsEnabled() {
        HistoryRecordingPolicy policy = new HistoryRecordingPolicy(
                true, true, Set.of(), Set.of("progression"), true, Set.of(), Set.of());

        assertFalse(policy.allows("progression", "region-enter"));
        assertTrue(policy.allows("citizen", "realm-assigned"));
    }

    @Test
    void enabledListsAllowOptInRecording() {
        HistoryRecordingPolicy policy = new HistoryRecordingPolicy(
                true, false, Set.of("realm"), Set.of(), false, Set.of("realm:leader-set"), Set.of());

        assertTrue(policy.allows("realm", "leader_set"));
        assertFalse(policy.allows("realm", "reward"));
        assertFalse(policy.allows("citizen", "leader_set"));
    }

    @Test
    void scopedTypeDisableDoesNotDisableSameTypeInOtherCategory() {
        HistoryRecordingPolicy policy = new HistoryRecordingPolicy(
                true, true, Set.of(), Set.of(), true, Set.of(), Set.of("citizen:changed"));

        assertFalse(policy.allows("citizen", "changed"));
        assertTrue(policy.allows("realm", "changed"));
    }
}
