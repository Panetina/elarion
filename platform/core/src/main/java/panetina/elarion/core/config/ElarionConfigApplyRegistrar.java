package panetina.elarion.core.config;

@FunctionalInterface
public interface ElarionConfigApplyRegistrar {
    void register(
            ElarionConfigEditTarget target,
            ElarionConfigApplyCapability capability,
            ElarionConfigApplier applier
    );
}
