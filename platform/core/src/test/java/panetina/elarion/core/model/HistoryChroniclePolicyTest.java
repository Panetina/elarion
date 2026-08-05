package panetina.elarion.core.model;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HistoryChroniclePolicyTest {
    @Test
    void scopedDenyListKeepsAuditCategoryEligibleButBlocksOnlyThatChronicleType() {
        HistoryChroniclePolicy policy = new HistoryChroniclePolicy(
                Set.of("realm"), true, Set.of(), Set.of("realm:member-joined"));

        assertTrue(policy.allows("realm", "leader-set"));
        assertFalse(policy.allows("realm", "member-joined"));
        assertFalse(policy.allows("citizen", "realm-assigned"));
        assertFalse(policy.mayContain(Map.of("realm", 1), Map.of("realm:member-joined", 1)));
        assertTrue(policy.mayContain(Map.of("realm", 1), Map.of("realm:leader-set", 1)));
    }

    @Test
    void allowListWorksWhenDefaultTypesAreDisabled() {
        HistoryChroniclePolicy policy = new HistoryChroniclePolicy(
                Set.of("government"), false, Set.of("government:proposal-approved"), Set.of());

        assertTrue(policy.allows("government", "proposal-approved"));
        assertFalse(policy.allows("government", "proposal-created"));
    }
}
