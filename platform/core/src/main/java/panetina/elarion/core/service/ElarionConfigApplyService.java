package panetina.elarion.core.service;

import panetina.elarion.core.config.ElarionConfigApplyAuditJournal;
import panetina.elarion.core.config.ElarionConfigApplyCoordinator;
import panetina.elarion.core.config.ElarionConfigApplyExecutor;
import panetina.elarion.core.config.ElarionConfigApplyReadiness;
import panetina.elarion.core.config.ElarionConfigApplyRegistry;
import panetina.elarion.core.config.ElarionConfigChangeError;
import panetina.elarion.core.config.ElarionConfigChangeRequest;
import panetina.elarion.core.config.ElarionConfigChangeResult;
import panetina.elarion.core.config.ElarionConfigEditTarget;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.config.ElarionConfigRegistry;

import java.nio.file.Path;
import java.util.Objects;

public final class ElarionConfigApplyService implements ElarionConfigApplyExecutor {
    private static final int DEFAULT_RECOVERY_TAIL_LINES = 256;
    private static final String UNBOUND_REASON = "Config apply execution is not bound to a server yet.";

    private final ElarionConfigRegistry descriptors;
    private final ElarionConfigApplyRegistry appliers;
    private final int recoveryTailLines;

    private ElarionConfigApplyCoordinator coordinator;
    private Path journalFile;
    private String disabledReason = UNBOUND_REASON;

    public ElarionConfigApplyService(
            ElarionConfigRegistry descriptors,
            ElarionConfigApplyRegistry appliers
    ) {
        this(descriptors, appliers, DEFAULT_RECOVERY_TAIL_LINES);
    }

    ElarionConfigApplyService(
            ElarionConfigRegistry descriptors,
            ElarionConfigApplyRegistry appliers,
            int recoveryTailLines
    ) {
        this.descriptors = Objects.requireNonNull(descriptors, "Config descriptor registry is required");
        this.appliers = Objects.requireNonNull(appliers, "Config apply registry is required");
        if (recoveryTailLines <= 0) throw new IllegalArgumentException("recoveryTailLines must be positive");
        this.recoveryTailLines = recoveryTailLines;
    }

    public synchronized void bind(Path elarionRoot) {
        Objects.requireNonNull(elarionRoot, "Elarion world root is required");
        ElarionConfigApplyAuditJournal journal = new ElarionConfigApplyAuditJournal(
                ElarionConfigApplyAuditJournal.journalPath(elarionRoot));
        ElarionConfigApplyAuditJournal.Recovery recovery = journal.recoverUnresolvedTail(recoveryTailLines);
        journalFile = journal.file();
        if (recovery.tailTruncated()) {
            coordinator = null;
            disabledReason = "Config apply audit journal recovery window is truncated; operator review is required.";
            return;
        }
        if (!recovery.unresolved().isEmpty()) {
            coordinator = null;
            disabledReason = "Config apply audit journal has unresolved prepared changes; operator review is required.";
            return;
        }
        coordinator = new ElarionConfigApplyCoordinator(descriptors, appliers, journal);
        disabledReason = "";
    }

    public synchronized void unbind() {
        coordinator = null;
        journalFile = null;
        disabledReason = UNBOUND_REASON;
    }

    public synchronized boolean executionReady() {
        return coordinator != null && disabledReason.isBlank();
    }

    public synchronized String disabledReason() {
        return disabledReason;
    }

    public synchronized Path journalFile() {
        return journalFile;
    }

    public ElarionConfigChangeResult apply(
            ElarionConfigChangeRequest request,
            ElarionConfigPermission actorPermission
    ) {
        Objects.requireNonNull(request, "Config change request is required");
        ElarionConfigApplyCoordinator active;
        String blockedReason;
        synchronized (this) {
            active = coordinator;
            blockedReason = disabledReason.isBlank() ? UNBOUND_REASON : disabledReason;
        }
        if (active == null) {
            return ElarionConfigChangeResult.rejected(request, java.util.List.of(
                    ElarionConfigChangeError.of(
                            ElarionConfigChangeError.Code.UNSUPPORTED,
                            request.domainId() + ":" + request.categoryId() + ":" + request.entryId(),
                            blockedReason)));
        }
        return active.apply(request, actorPermission);
    }

    @Override
    public ElarionConfigApplyReadiness readiness(ElarionConfigEditTarget target) {
        ElarionConfigApplyReadiness readiness = appliers.readiness(descriptors, target);
        if (!readiness.ready()) return readiness;
        String blockedReason;
        synchronized (this) {
            if (executionReady()) return readiness;
            blockedReason = disabledReason.isBlank() ? UNBOUND_REASON : disabledReason;
        }
        return ElarionConfigApplyReadiness.blocked(target, ElarionConfigChangeError.of(
                ElarionConfigChangeError.Code.UNSUPPORTED, target.targetKey(), blockedReason));
    }
}
