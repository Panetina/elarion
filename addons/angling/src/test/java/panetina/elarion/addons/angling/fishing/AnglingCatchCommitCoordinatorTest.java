package panetina.elarion.addons.angling.fishing;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import panetina.elarion.addons.angling.definition.AnglingCatchDefinition;
import panetina.elarion.addons.angling.definition.AnglingCatchOutput;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshot;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshotRepository;
import panetina.elarion.addons.angling.definition.AnglingCatchType;
import panetina.elarion.addons.angling.definition.AnglingDifficultyDefinition;
import panetina.elarion.addons.angling.definition.AnglingItemReference;
import panetina.elarion.addons.angling.definition.AnglingRarity;
import panetina.elarion.addons.angling.definition.AnglingSizeWeightDefinition;
import panetina.elarion.addons.angling.definition.AnglingTypedNode;
import panetina.elarion.addons.angling.persistence.AnglingCatchTransactionJournal;
import panetina.elarion.core.metric.MetricUpdateBatch;
import panetina.elarion.core.model.CatchTelemetryEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingCatchCommitCoordinatorTest {
    private static final Identifier FISH = Identifier.of("elarion_angling", "transaction_fish");

    @BeforeAll
    static void bootstrapRegistries() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void commitsInDurableRequestProjectionDeliveryOrder(@TempDir Path root) {
        Harness harness = new Harness();
        AnglingCatchCommitCoordinator coordinator = harness.coordinator();
        coordinator.bind(root);

        AnglingCatchCommit commit = coordinator.submit(outcome(), facts(UUID.randomUUID(), UUID.randomUUID())).join();

        assertEquals(List.of("request", "catch", "metric", "projected", "delivery", "delivered"), harness.order);
        assertEquals(1, commit.metrics().sequence());
        assertEquals(0, coordinator.snapshot().pendingTransactions());
        assertEquals(1, coordinator.snapshot().completedCatches());
        coordinator.shutdown();
    }

    @Test
    void projectionFailureClosesAdmissionAndRestartReplaysSameSequence(@TempDir Path root) {
        Harness harness = new Harness();
        harness.metrics.failNext = true;
        UUID actor = UUID.randomUUID();
        UUID event = UUID.randomUUID();
        AnglingCatchCommitCoordinator first = harness.coordinator();
        first.bind(root);

        assertThrows(RuntimeException.class, () -> first.submit(outcome(), facts(event, actor)).join());
        assertFalse(first.snapshot().accepting());
        assertEquals(1, first.snapshot().pendingTransactions());
        assertThrows(RuntimeException.class,
                () -> first.submit(outcome(), facts(UUID.randomUUID(), actor)).join());
        first.shutdown();

        AnglingCatchCommitCoordinator recovered = harness.coordinator();
        recovered.bind(root);

        assertEquals(2, harness.catches.calls);
        assertEquals(2, harness.metrics.calls);
        assertEquals(1, harness.delivery.calls);
        assertEquals(1, harness.metrics.lastSequence);
        assertEquals(1, recovered.snapshot().recoveredCatches());
        assertTrue(recovered.snapshot().accepting());
        recovered.shutdown();
    }

    @Test
    void deliveryFailureRecoversFromProjectedStageWithoutReapplyingCore(@TempDir Path root) {
        Harness harness = new Harness();
        harness.delivery.failNext = true;
        AnglingCatchCommitCoordinator first = harness.coordinator();
        first.bind(root);

        assertThrows(RuntimeException.class,
                () -> first.submit(outcome(), facts(UUID.randomUUID(), UUID.randomUUID())).join());
        assertEquals(AnglingCatchTransactionJournal.Stage.PROJECTED,
                harness.store.pending.values().iterator().next().stage());
        first.shutdown();

        AnglingCatchCommitCoordinator recovered = harness.coordinator();
        recovered.bind(root);

        assertEquals(1, harness.catches.calls);
        assertEquals(1, harness.metrics.calls);
        assertEquals(2, harness.delivery.calls);
        assertTrue(harness.store.pending.isEmpty());
        recovered.shutdown();
    }

    private static AnglingCatchOutcome outcome() {
        return new AnglingCatchOutcome(definition(), new ItemStack(Items.COD), Optional.empty(),
                420, 1_250, 125, true, true, true, 60, 8);
    }

    private static AnglingCatchCommitFactory.Facts facts(UUID event, UUID actor) {
        return new AnglingCatchCommitFactory.Facts(
                event, 1_780_000_000_000L, actor,
                Identifier.ofVanilla("overworld"), Identifier.ofVanilla("overworld"),
                Identifier.ofVanilla("plains"), Identifier.of("elarion_angling", "worm"),
                Identifier.of("elarion_angling", "elarion_angling_rod"),
                Identifier.of("elarion_angling", "bobber"), Identifier.of("elarion_angling", "hook"),
                Identifier.ofVanilla("water"), null, null,
                new AnglingCatchReward.RewardPosition(10.5, 64, -2.5));
    }

    private static AnglingCatchSnapshot.NativeCatch definition() {
        AnglingCatchDefinition source = new AnglingCatchDefinition(
                1,
                new AnglingCatchOutput(new AnglingItemReference(Identifier.ofVanilla("cod"), 1),
                        Optional.empty(), Optional.empty(), false, Optional.empty(), AnglingCatchType.FISH),
                1, new AnglingSizeWeightDefinition(1, 0, 1, 0, 0), AnglingRarity.COMMON,
                List.of(new AnglingTypedNode(Identifier.of("elarion_angling", "empty"),
                        "{\"type\":\"elarion_angling:empty\"}")),
                new AnglingDifficultyDefinition(1, 1, 0, 0, List.of(), List.of()),
                false, true, Identifier.of("elarion_angling", "texture"));
        return new AnglingCatchSnapshotRepository().compileAndPublish(Map.of(FISH, source))
                .find(FISH).orElseThrow();
    }

    private static final class Harness {
        private final List<String> order = new ArrayList<>();
        private final FakeStore store = new FakeStore(order);
        private final FakeCatches catches = new FakeCatches(order);
        private final FakeMetrics metrics = new FakeMetrics(order);
        private final FakeDelivery delivery = new FakeDelivery(order);

        private AnglingCatchCommitCoordinator coordinator() {
            return new AnglingCatchCommitCoordinator(new AnglingCatchCommitFactory(), catches, metrics, delivery,
                    store, 8, 8, Duration.ofSeconds(5));
        }
    }

    private static final class FakeCatches implements AnglingCatchCommitCoordinator.CatchProjection {
        private final List<String> order;
        private final Set<UUID> applied = new java.util.HashSet<>();
        private int calls;

        private FakeCatches(List<String> order) {
            this.order = order;
        }

        @Override
        public CompletableFuture<?> submit(CatchTelemetryEvent event) {
            calls++;
            order.add("catch");
            applied.add(event.eventId());
            return CompletableFuture.completedFuture(event);
        }
    }

    private static final class FakeMetrics implements AnglingCatchCommitCoordinator.MetricProjection {
        private final List<String> order;
        private final Map<String, Long> sequences = new LinkedHashMap<>();
        private boolean failNext;
        private int calls;
        private long lastSequence;

        private FakeMetrics(List<String> order) {
            this.order = order;
        }

        @Override
        public long nextSourceSequence(Identifier sourceSystem, String sourcePartition) {
            return sequences.getOrDefault(sourcePartition, 0L) + 1L;
        }

        @Override
        public CompletableFuture<?> submit(MetricUpdateBatch batch) {
            calls++;
            order.add("metric");
            if (failNext) {
                failNext = false;
                return CompletableFuture.failedFuture(new IOException("injected metric failure"));
            }
            long prior = sequences.getOrDefault(batch.sourcePartition(), 0L);
            if (batch.sequence() > prior) sequences.put(batch.sourcePartition(), batch.sequence());
            lastSequence = batch.sequence();
            return CompletableFuture.completedFuture(batch);
        }
    }

    private static final class FakeDelivery implements AnglingCatchCommitCoordinator.Delivery {
        private final List<String> order;
        private final Set<UUID> delivered = new java.util.HashSet<>();
        private boolean failNext;
        private int calls;

        private FakeDelivery(List<String> order) {
            this.order = order;
        }

        @Override
        public CompletableFuture<Void> deliver(AnglingCatchCommit commit) {
            calls++;
            order.add("delivery");
            if (failNext) {
                failNext = false;
                return CompletableFuture.failedFuture(new IOException("injected delivery failure"));
            }
            delivered.add(commit.telemetry().eventId());
            return CompletableFuture.completedFuture(null);
        }
    }

    private static final class FakeStore implements AnglingCatchCommitCoordinator.TransactionStore {
        private final List<String> order;
        private final Map<UUID, AnglingCatchTransactionJournal.Pending> pending = new LinkedHashMap<>();

        private FakeStore(List<String> order) {
            this.order = order;
        }

        @Override
        public void appendRequest(Path root, AnglingCatchCommit commit) {
            order.add("request");
            pending.put(commit.telemetry().eventId(), new AnglingCatchTransactionJournal.Pending(
                    commit, AnglingCatchTransactionJournal.Stage.REQUESTED));
        }

        @Override
        public void appendStage(Path root, UUID eventId, AnglingCatchTransactionJournal.Stage stage) {
            order.add(stage == AnglingCatchTransactionJournal.Stage.PROJECTED ? "projected" : "delivered");
            if (stage == AnglingCatchTransactionJournal.Stage.DELIVERED) {
                pending.remove(eventId);
            } else {
                AnglingCatchTransactionJournal.Pending current = pending.get(eventId);
                pending.put(eventId, new AnglingCatchTransactionJournal.Pending(current.commit(), stage));
            }
        }

        @Override
        public Map<UUID, AnglingCatchTransactionJournal.Pending> loadPending(Path root) {
            return new LinkedHashMap<>(pending);
        }

        @Override
        public void compact(Path root, Map<UUID, AnglingCatchTransactionJournal.Pending> current) {
            pending.clear();
            pending.putAll(current);
        }
    }
}
