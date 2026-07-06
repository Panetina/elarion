package panetina.elarion.core.config;

@FunctionalInterface
public interface ElarionConfigApplyAuditSink {
    ElarionConfigApplyAuditSession prepare(ElarionConfigApplyAuditRecord record);
}
