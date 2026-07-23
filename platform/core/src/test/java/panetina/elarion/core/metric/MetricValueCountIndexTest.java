package panetina.elarion.core.metric;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MetricValueCountIndexTest {
    @Test
    void orderStatisticsMatchReferenceCountsAcrossInsertionsAndRemovals() {
        MetricValueCountIndex index = new MetricValueCountIndex();
        List<Long> values = new ArrayList<>();
        Random random = new Random(41);
        for (int iteration = 0; iteration < 1_000; iteration++) {
            long value = random.nextInt(101) - 50;
            index.add(value, 1);
            values.add(value);
        }
        for (long probe = -60; probe <= 60; probe++) {
            long currentProbe = probe;
            long expectedLess = values.stream().filter(value -> value < currentProbe).count();
            long expectedGreater = values.stream().filter(value -> value > currentProbe).count();
            assertEquals(expectedLess, index.countLess(probe));
            assertEquals(expectedGreater, index.countGreater(probe));
        }
        for (int iteration = 0; iteration < 500; iteration++) {
            long removed = values.remove(values.size() - 1);
            index.add(removed, -1);
        }
        for (long probe = -60; probe <= 60; probe++) {
            long currentProbe = probe;
            assertEquals(values.stream().filter(value -> value < currentProbe).count(), index.countLess(probe));
            assertEquals(values.stream().filter(value -> value > currentProbe).count(), index.countGreater(probe));
        }
    }
}
