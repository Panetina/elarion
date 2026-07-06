package panetina.elarion.core.config;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public final class ElarionConfigApplyCoordinator {
    private static final ReentrantLock MUTATION_LOCK = new ReentrantLock(true);

    private final ElarionConfigRegistry descriptors;
    private final ElarionConfigApplyRegistry appliers;
    private final ElarionConfigApplyAuditSink auditSink;

    public ElarionConfigApplyCoordinator(
            ElarionConfigRegistry descriptors,
            ElarionConfigApplyRegistry appliers,
            ElarionConfigApplyAuditSink auditSink
    ) {
        this.descriptors = Objects.requireNonNull(descriptors, "Config descriptor registry is required");
        this.appliers = Objects.requireNonNull(appliers, "Config apply registry is required");
        this.auditSink = Objects.requireNonNull(auditSink, "Config apply audit sink is required");
    }

    public ElarionConfigChangeResult apply(
            ElarionConfigChangeRequest request,
            ElarionConfigPermission actorPermission
    ) {
        Objects.requireNonNull(request, "Config change request is required");
        MUTATION_LOCK.lock();
        try {
            ElarionConfigChangeResult validated = ElarionConfigChangeValidator.validate(
                    descriptors, request, actorPermission);
            if (!validated.success()) return validated;

            ElarionConfigEditTarget target = new ElarionConfigEditTarget(
                    request.domainId(), request.categoryId(), request.entryId());
            ElarionConfigApplyReadiness readiness = appliers.readiness(descriptors, target);
            if (!readiness.ready()) {
                return ElarionConfigChangeResult.rejected(request, readiness.errors());
            }

            ElarionConfigApplyRegistry.Registration registration = appliers.registration(target).orElse(null);
            ElarionConfigDomain domain = descriptors.domain(target.domainId()).orElse(null);
            ElarionConfigCategory category = domain == null
                    ? null
                    : domain.category(target.categoryId()).orElse(null);
            ElarionConfigEntry<?> entry = category == null
                    ? null
                    : category.entry(target.entryId()).orElse(null);
            if (registration == null || domain == null || category == null || entry == null) {
                return failed(request, target.targetKey(), "Config apply target changed during execution.",
                        null, null);
            }

            ElarionConfigApplyContext context = new ElarionConfigApplyContext(
                    descriptors, domain, category, entry, request);
            ElarionConfigPreparedChange prepared;
            try {
                prepared = registration.applier().prepare(context);
            } catch (RuntimeException exception) {
                return failed(request, entry.path(), "Config apply preparation failed: " + message(exception),
                        null, null);
            }
            if (prepared == null) {
                return failed(request, entry.path(), "Config applier returned no prepared change.", null, null);
            }

            ElarionConfigApplyAuditRecord audit = new ElarionConfigApplyAuditRecord(
                    target,
                    request.actorId(),
                    request.reason(),
                    validated.oldDisplayValue(),
                    validated.newDisplayValue(),
                    validated.reloadRequired(),
                    validated.restartRequired(),
                    registration.capability().auditEventType(),
                    registration.capability().affectedFiles());
            ElarionConfigApplyAuditSession auditSession;
            try {
                auditSession = auditSink.prepare(audit);
            } catch (RuntimeException exception) {
                return failed(request, entry.path(), "Config audit preparation failed: " + message(exception),
                        prepared, null);
            }
            if (auditSession == null) {
                return failed(request, entry.path(), "Config audit sink returned no session.", prepared, null);
            }

            try {
                ElarionConfigChangeResult committed = prepared.commit();
                requireTrustedResult(validated, registration.capability(), committed);
                auditSession.committed();
                return ElarionConfigChangeResult.applied(
                        request,
                        validated.oldDisplayValue(),
                        validated.newDisplayValue(),
                        validated.reloadRequired(),
                        validated.restartRequired(),
                        registration.capability().auditEventType());
            } catch (RuntimeException exception) {
                return failed(request, entry.path(), "Config apply failed: " + message(exception),
                        prepared, auditSession);
            }
        } finally {
            MUTATION_LOCK.unlock();
        }
    }

    private static void requireTrustedResult(
            ElarionConfigChangeResult validated,
            ElarionConfigApplyCapability capability,
            ElarionConfigChangeResult committed
    ) {
        if (committed == null) throw new IllegalStateException("Config commit returned no result");
        if (committed.status() != ElarionConfigChangeResult.Status.APPLIED) {
            throw new IllegalStateException("Config commit did not return APPLIED");
        }
        if (!committed.request().equals(validated.request())) {
            throw new IllegalStateException("Config commit returned a mismatched request");
        }
        if (!committed.oldDisplayValue().equals(validated.oldDisplayValue())
                || !committed.newDisplayValue().equals(validated.newDisplayValue())) {
            throw new IllegalStateException("Config commit returned mismatched values");
        }
        if (committed.reloadRequired() != validated.reloadRequired()
                || committed.restartRequired() != validated.restartRequired()) {
            throw new IllegalStateException("Config commit returned mismatched runtime policy");
        }
        if (!committed.auditEventType().equals(capability.auditEventType())) {
            throw new IllegalStateException("Config commit returned a mismatched audit event type");
        }
    }

    private static ElarionConfigChangeResult failed(
            ElarionConfigChangeRequest request,
            String path,
            String failure,
            ElarionConfigPreparedChange prepared,
            ElarionConfigApplyAuditSession auditSession
    ) {
        String message = failure;
        boolean rolledBack = true;
        if (prepared != null) {
            try {
                prepared.rollback();
            } catch (RuntimeException rollbackFailure) {
                rolledBack = false;
                message += " Rollback failed: " + message(rollbackFailure);
            }
        }
        if (auditSession != null) {
            try {
                if (rolledBack) auditSession.rolledBack(message);
                else auditSession.failed(message);
            } catch (RuntimeException auditFailure) {
                message += " Audit outcome failed: " + message(auditFailure);
            }
        }
        return ElarionConfigChangeResult.rejected(request, List.of(
                ElarionConfigChangeError.of(ElarionConfigChangeError.Code.APPLY_FAILED, path, message)));
    }

    private static String message(RuntimeException exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? exception.getClass().getSimpleName()
                : exception.getMessage();
    }
}
