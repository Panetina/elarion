package panetina.elarion.core.metric;

import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class MetricDescriptorRegistry {
    private final Map<Identifier, MetricDescriptor> descriptors;

    private MetricDescriptorRegistry(Map<Identifier, MetricDescriptor> descriptors) {
        this.descriptors = Map.copyOf(descriptors);
    }

    public MetricDescriptor require(Identifier metricId) {
        MetricDescriptor descriptor = descriptors.get(metricId);
        if (descriptor == null) throw new IllegalArgumentException("Unknown metric " + metricId);
        return descriptor;
    }

    public Map<Identifier, MetricDescriptor> snapshot() {
        return descriptors;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<Identifier, MetricDescriptor> descriptors = new LinkedHashMap<>();
        private boolean built;

        public Builder register(MetricDescriptor descriptor) {
            if (built) throw new IllegalStateException("metric descriptor builder is frozen");
            Objects.requireNonNull(descriptor, "descriptor");
            if (descriptors.putIfAbsent(descriptor.metricId(), descriptor) != null) {
                throw new IllegalArgumentException("Duplicate metric " + descriptor.metricId());
            }
            return this;
        }

        public MetricDescriptorRegistry build() {
            if (built) throw new IllegalStateException("metric descriptor builder is frozen");
            built = true;
            return new MetricDescriptorRegistry(descriptors);
        }
    }
}
