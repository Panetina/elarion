package panetina.elarion.core.config;

public interface ElarionConfigApplyAuditSession {
    void committed();

    void rolledBack(String failure);

    void failed(String failure);
}
