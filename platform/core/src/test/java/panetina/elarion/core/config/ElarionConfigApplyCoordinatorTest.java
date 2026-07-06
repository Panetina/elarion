package panetina.elarion.core.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionConfigApplyCoordinatorTest {
    private static final UUID ACTOR = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void appliesTrustedTransactionAndRecordsAudit() {
        Fixture fixture = fixture();
        AtomicReference<ElarionConfigApplyAuditRecord> audit = new AtomicReference<>();
        List<String> phases = new CopyOnWriteArrayList<>();
        registerStateChange(fixture, new AtomicInteger());
        ElarionConfigApplyCoordinator coordinator = coordinator(
                fixture, trackingAuditSink(audit::set, phases));

        ElarionConfigChangeResult result = coordinator.apply(request("false", "true"),
                ElarionConfigPermission.OPERATOR);

        assertEquals(ElarionConfigChangeResult.Status.APPLIED, result.status());
        assertEquals("true", result.oldDisplayValue());
        assertEquals("false", result.newDisplayValue());
        assertTrue(result.reloadRequired());
        assertFalse(result.restartRequired());
        assertFalse(fixture.current().get());
        ElarionConfigApplyAuditRecord record = audit.get();
        assertNotNull(record);
        assertEquals(fixture.target(), record.target());
        assertEquals(ACTOR, record.actorId());
        assertEquals("test apply", record.reason());
        assertEquals(List.of("config/elarion/core/test.yml"), record.affectedFiles());
        assertEquals(List.of("prepared", "committed"), phases);
    }

    @Test
    void rejectsValidationAndMissingRegistrationBeforePreparation() {
        Fixture fixture = fixture();
        AtomicInteger prepares = new AtomicInteger();
        AtomicInteger audits = new AtomicInteger();
        registerStateChange(fixture, prepares);
        ElarionConfigApplyCoordinator coordinator = coordinator(
                fixture, trackingAuditSink(ignored -> audits.incrementAndGet(), new CopyOnWriteArrayList<>()));

        ElarionConfigChangeResult invalid = coordinator.apply(request("invalid", "true"),
                ElarionConfigPermission.OPERATOR);
        assertEquals(ElarionConfigChangeError.Code.PARSE_FAILED, invalid.errors().getFirst().code());
        assertEquals(0, prepares.get());

        ElarionConfigChangeResult stale = coordinator.apply(request("false", "false"),
                ElarionConfigPermission.OPERATOR);
        assertEquals(ElarionConfigChangeError.Code.STALE_VALUE, stale.errors().getFirst().code());
        assertEquals(0, prepares.get());

        Fixture missing = fixture();
        ElarionConfigChangeResult unsupported = coordinator(
                missing, trackingAuditSink(ignored -> audits.incrementAndGet(), new CopyOnWriteArrayList<>()))
                .apply(request("false", "true"), ElarionConfigPermission.OPERATOR);
        assertEquals(ElarionConfigChangeError.Code.UNSUPPORTED, unsupported.errors().getFirst().code());
        assertEquals(0, audits.get());
    }

    @Test
    void preparationFailureDoesNotCommitOrAudit() {
        Fixture fixture = fixture();
        AtomicInteger audits = new AtomicInteger();
        fixture.appliers().register(fixture.target(), fixture.capability(), context -> {
            throw new IllegalStateException("prepare failed");
        });

        ElarionConfigChangeResult result = coordinator(
                fixture, trackingAuditSink(ignored -> audits.incrementAndGet(), new CopyOnWriteArrayList<>()))
                .apply(request("false", "true"), ElarionConfigPermission.OPERATOR);

        assertEquals(ElarionConfigChangeError.Code.APPLY_FAILED, result.errors().getFirst().code());
        assertTrue(result.errors().getFirst().message().contains("prepare failed"));
        assertTrue(fixture.current().get());
        assertEquals(0, audits.get());
    }

    @Test
    void commitFailureRollsBack() {
        Fixture fixture = fixture();
        AtomicInteger rollbacks = new AtomicInteger();
        fixture.appliers().register(fixture.target(), fixture.capability(), context -> {
            boolean old = fixture.current().get();
            return ElarionConfigPreparedChange.of(() -> {
                fixture.current().set(false);
                throw new IllegalStateException("commit failed");
            }, () -> {
                fixture.current().set(old);
                rollbacks.incrementAndGet();
            });
        });

        ElarionConfigChangeResult result = coordinator(fixture, noOpAuditSink())
                .apply(request("false", "true"), ElarionConfigPermission.OPERATOR);

        assertEquals(ElarionConfigChangeError.Code.APPLY_FAILED, result.errors().getFirst().code());
        assertTrue(fixture.current().get());
        assertEquals(1, rollbacks.get());
    }

    @Test
    void reportsRollbackFailureWithOriginalApplyFailure() {
        Fixture fixture = fixture();
        AtomicInteger failedAudits = new AtomicInteger();
        fixture.appliers().register(fixture.target(), fixture.capability(), context ->
                ElarionConfigPreparedChange.of(
                        () -> { throw new IllegalStateException("commit failed"); },
                        () -> { throw new IllegalStateException("rollback failed"); }));

        ElarionConfigChangeResult result = coordinator(fixture, record -> new ElarionConfigApplyAuditSession() {
            @Override
            public void committed() {
            }

            @Override
            public void rolledBack(String failure) {
            }

            @Override
            public void failed(String failure) {
                failedAudits.incrementAndGet();
            }
        })
                .apply(request("false", "true"), ElarionConfigPermission.OPERATOR);

        assertEquals(ElarionConfigChangeError.Code.APPLY_FAILED, result.errors().getFirst().code());
        assertTrue(result.errors().getFirst().message().contains("commit failed"));
        assertTrue(result.errors().getFirst().message().contains("rollback failed"));
        assertEquals(1, failedAudits.get());
    }

    @Test
    void auditPreparationFailureRollsBackPreparedResourcesBeforeCommit() {
        Fixture fixture = fixture();
        AtomicInteger rollbacks = new AtomicInteger();
        registerStateChange(fixture, new AtomicInteger(), rollbacks);

        ElarionConfigChangeResult result = coordinator(fixture, record -> {
            throw new IllegalStateException("audit prepare failed");
        }).apply(request("false", "true"), ElarionConfigPermission.OPERATOR);

        assertEquals(ElarionConfigChangeError.Code.APPLY_FAILED, result.errors().getFirst().code());
        assertTrue(result.errors().getFirst().message().contains("audit prepare failed"));
        assertTrue(fixture.current().get());
        assertEquals(1, rollbacks.get());
    }

    @Test
    void invalidOwnerResultAndAuditFailureRollBackCommittedState() {
        Fixture invalid = fixture();
        AtomicInteger invalidRollbacks = new AtomicInteger();
        invalid.appliers().register(invalid.target(), invalid.capability(), context -> {
            boolean old = invalid.current().get();
            return ElarionConfigPreparedChange.of(() -> {
                invalid.current().set(false);
                return ElarionConfigChangeResult.applied(
                        context.request(), "true", "wrong", true, false,
                        invalid.capability().auditEventType());
            }, () -> {
                invalid.current().set(old);
                invalidRollbacks.incrementAndGet();
            });
        });

        ElarionConfigChangeResult invalidResult = coordinator(invalid, noOpAuditSink())
                .apply(request("false", "true"), ElarionConfigPermission.OPERATOR);
        assertEquals(ElarionConfigChangeError.Code.APPLY_FAILED, invalidResult.errors().getFirst().code());
        assertTrue(invalid.current().get());
        assertEquals(1, invalidRollbacks.get());

        Fixture auditFailure = fixture();
        AtomicInteger auditRollbacks = new AtomicInteger();
        List<String> auditPhases = new CopyOnWriteArrayList<>();
        registerStateChange(auditFailure, new AtomicInteger(), auditRollbacks);
        ElarionConfigChangeResult auditResult = coordinator(auditFailure, record -> {
            auditPhases.add("prepared");
            return new ElarionConfigApplyAuditSession() {
                @Override
                public void committed() {
                    throw new IllegalStateException("audit committed failed");
                }

                @Override
                public void rolledBack(String failure) {
                    auditPhases.add("rolledBack");
                }

                @Override
                public void failed(String failure) {
                    auditPhases.add("failed");
                }
            };
        }).apply(request("false", "true"), ElarionConfigPermission.OPERATOR);
        assertEquals(ElarionConfigChangeError.Code.APPLY_FAILED, auditResult.errors().getFirst().code());
        assertTrue(auditFailure.current().get());
        assertEquals(1, auditRollbacks.get());
        assertEquals(List.of("prepared", "rolledBack"), auditPhases);
    }

    @Test
    void serializesConcurrentApplyRequests() throws Exception {
        Fixture fixture = fixture();
        CountDownLatch firstCommitEntered = new CountDownLatch(1);
        CountDownLatch releaseFirstCommit = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        AtomicInteger prepares = new AtomicInteger();
        AtomicInteger commits = new AtomicInteger();
        fixture.appliers().register(fixture.target(), fixture.capability(), context -> {
            prepares.incrementAndGet();
            boolean old = fixture.current().get();
            boolean next = Boolean.parseBoolean(context.request().proposedValue());
            return ElarionConfigPreparedChange.of(() -> {
                if (commits.incrementAndGet() == 1) {
                    firstCommitEntered.countDown();
                    await(releaseFirstCommit);
                }
                fixture.current().set(next);
                return applied(context, old, next, fixture.capability());
            }, () -> fixture.current().set(old));
        });
        ElarionConfigApplyCoordinator coordinator = coordinator(fixture, noOpAuditSink());
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> coordinator.apply(request("false", ""),
                    ElarionConfigPermission.OPERATOR));
            assertTrue(firstCommitEntered.await(5, TimeUnit.SECONDS));
            var second = executor.submit(() -> {
                secondStarted.countDown();
                return coordinator.apply(request("true", ""), ElarionConfigPermission.OPERATOR);
            });
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
            assertEquals(1, prepares.get());
            assertFalse(second.isDone());

            releaseFirstCommit.countDown();
            assertEquals(ElarionConfigChangeResult.Status.APPLIED, first.get(5, TimeUnit.SECONDS).status());
            assertEquals(ElarionConfigChangeResult.Status.APPLIED, second.get(5, TimeUnit.SECONDS).status());
            assertEquals(2, prepares.get());
            assertTrue(fixture.current().get());
        } finally {
            releaseFirstCommit.countDown();
            executor.shutdownNow();
        }
    }

    private static Fixture fixture() {
        AtomicReference<Boolean> current = new AtomicReference<>(true);
        ElarionConfigRegistry descriptors = new ElarionConfigRegistry();
        descriptors.registerDomain(new ElarionConfigDomain(
                "core",
                "platform:core",
                "Core",
                "Core config",
                List.of("config/elarion/core/test.yml"),
                "/e reload",
                List.of(new ElarionConfigCategory(
                        "general",
                        "General",
                        "General settings",
                        List.of(new ElarionConfigEntry<>(
                                "enabled",
                                "Enabled",
                                "Test setting",
                                "test.yml.enabled",
                                ElarionConfigCodec.BOOLEAN,
                                true,
                                current::get,
                                ElarionConfigValidator.pass(),
                                List.of("true", "false"),
                                "",
                                "",
                                true,
                                false,
                                ElarionConfigPermission.OPERATOR,
                                ElarionConfigPermission.OPERATOR))))));
        return new Fixture(
                descriptors,
                new ElarionConfigApplyRegistry(),
                current,
                new ElarionConfigEditTarget("core", "general", "enabled"),
                ElarionConfigApplyCapability.runtimeReload(
                        "admin-config-applied", List.of("config/elarion/core/test.yml")));
    }

    private static void registerStateChange(Fixture fixture, AtomicInteger prepares) {
        registerStateChange(fixture, prepares, new AtomicInteger());
    }

    private static void registerStateChange(
            Fixture fixture,
            AtomicInteger prepares,
            AtomicInteger rollbacks
    ) {
        fixture.appliers().register(fixture.target(), fixture.capability(), context -> {
            prepares.incrementAndGet();
            boolean old = fixture.current().get();
            boolean next = Boolean.parseBoolean(context.request().proposedValue());
            return ElarionConfigPreparedChange.of(() -> {
                fixture.current().set(next);
                return applied(context, old, next, fixture.capability());
            }, () -> {
                fixture.current().set(old);
                rollbacks.incrementAndGet();
            });
        });
    }

    private static ElarionConfigChangeResult applied(
            ElarionConfigApplyContext context,
            boolean old,
            boolean next,
            ElarionConfigApplyCapability capability
    ) {
        return ElarionConfigChangeResult.applied(
                context.request(),
                Boolean.toString(old),
                Boolean.toString(next),
                true,
                false,
                capability.auditEventType());
    }

    private static ElarionConfigApplyCoordinator coordinator(
            Fixture fixture,
            ElarionConfigApplyAuditSink auditSink
    ) {
        return new ElarionConfigApplyCoordinator(fixture.descriptors(), fixture.appliers(), auditSink);
    }

    private static ElarionConfigApplyAuditSink noOpAuditSink() {
        return trackingAuditSink(ignored -> { }, new CopyOnWriteArrayList<>());
    }

    private static ElarionConfigApplyAuditSink trackingAuditSink(
            Consumer<ElarionConfigApplyAuditRecord> prepared,
            List<String> phases
    ) {
        return record -> {
            prepared.accept(record);
            phases.add("prepared");
            return new ElarionConfigApplyAuditSession() {
                @Override
                public void committed() {
                    phases.add("committed");
                }

                @Override
                public void rolledBack(String failure) {
                    phases.add("rolledBack");
                }

                @Override
                public void failed(String failure) {
                    phases.add("failed");
                }
            };
        };
    }

    private static ElarionConfigChangeRequest request(String proposed, String expected) {
        return new ElarionConfigChangeRequest(
                "core", "general", "enabled", proposed, expected, ACTOR, "test apply");
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for test release");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for test release", exception);
        }
    }

    private record Fixture(
            ElarionConfigRegistry descriptors,
            ElarionConfigApplyRegistry appliers,
            AtomicReference<Boolean> current,
            ElarionConfigEditTarget target,
            ElarionConfigApplyCapability capability
    ) {
    }
}
