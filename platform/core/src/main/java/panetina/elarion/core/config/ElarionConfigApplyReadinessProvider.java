package panetina.elarion.core.config;

@FunctionalInterface
public interface ElarionConfigApplyReadinessProvider {
    ElarionConfigApplyReadiness readiness(ElarionConfigEditTarget target);
}
