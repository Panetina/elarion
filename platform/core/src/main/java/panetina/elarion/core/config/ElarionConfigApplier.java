package panetina.elarion.core.config;

@FunctionalInterface
public interface ElarionConfigApplier {
    ElarionConfigPreparedChange prepare(ElarionConfigApplyContext context);
}
