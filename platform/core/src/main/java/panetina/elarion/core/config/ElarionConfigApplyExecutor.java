package panetina.elarion.core.config;

public interface ElarionConfigApplyExecutor extends ElarionConfigApplyReadinessProvider {
    ElarionConfigChangeResult apply(
            ElarionConfigChangeRequest request,
            ElarionConfigPermission actorPermission
    );
}
