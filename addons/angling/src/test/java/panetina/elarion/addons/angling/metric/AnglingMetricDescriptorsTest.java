package panetina.elarion.addons.angling.metric;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.metric.MetricOperation;
import panetina.elarion.core.metric.MetricScopeType;
import panetina.elarion.core.metric.MetricSortDirection;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingMetricDescriptorsTest {
    @Test
    void frozenMetricRosterIsUniqueAndUsesBoundedMaterializations() {
        assertEquals(18, AnglingMetricDescriptors.ALL.size());
        assertEquals(18, new HashSet<>(AnglingMetricDescriptors.ALL.stream().map(value -> value.metricId()).toList()).size());
        assertEquals(18, AnglingMetricDescriptors.registry().snapshot().size());
        AnglingMetricDescriptors.ALL.forEach(descriptor -> {
            assertTrue(descriptor.metricId().getNamespace().equals("elarion_angling"));
            assertTrue(descriptor.indexedDimensions().size() <= 2);
        });

        var fastest = AnglingMetricDescriptors.registry().require(
                Identifier.of("elarion_angling", "catch/fastest_ticks"));
        assertEquals(MetricOperation.MIN, fastest.operation());
        assertEquals(MetricSortDirection.ASCENDING, fastest.sortDirection());
        assertTrue(fastest.legalScopes().contains(MetricScopeType.REALM));
    }
}
