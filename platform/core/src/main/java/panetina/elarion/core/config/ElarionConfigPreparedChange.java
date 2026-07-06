package panetina.elarion.core.config;

import java.util.Objects;
import java.util.function.Supplier;

public interface ElarionConfigPreparedChange {
    ElarionConfigChangeResult commit();

    void rollback();

    static ElarionConfigPreparedChange of(
            Supplier<ElarionConfigChangeResult> commit,
            Runnable rollback
    ) {
        Objects.requireNonNull(commit, "Config commit operation is required");
        Objects.requireNonNull(rollback, "Config rollback operation is required");
        return new ElarionConfigPreparedChange() {
            private boolean committed;
            private boolean rolledBack;

            @Override
            public synchronized ElarionConfigChangeResult commit() {
                if (committed || rolledBack) {
                    throw new IllegalStateException("Prepared config change is no longer commit-ready");
                }
                ElarionConfigChangeResult result = Objects.requireNonNull(
                        commit.get(), "Config commit result is required");
                committed = true;
                return result;
            }

            @Override
            public synchronized void rollback() {
                if (rolledBack) return;
                rollback.run();
                rolledBack = true;
            }
        };
    }
}
