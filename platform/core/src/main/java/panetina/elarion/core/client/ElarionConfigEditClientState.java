package panetina.elarion.core.client;

import panetina.elarion.core.config.ElarionConfigEditControl;
import panetina.elarion.core.config.ElarionConfigChangeResult;
import panetina.elarion.core.network.ElarionConfigEditResultPayload;

import java.util.Optional;

public final class ElarionConfigEditClientState {
    private static ElarionConfigEditControl openControl;
    private static ElarionConfigEditResultPayload lastResult;

    private ElarionConfigEditClientState() {
    }

    public static synchronized void update(ElarionConfigEditResultPayload result) {
        lastResult = result;
        if (result != null && result.status() == ElarionConfigChangeResult.Status.APPLIED) {
            openControl = null;
        }
    }

    public static synchronized void clearLastResult() {
        lastResult = null;
    }

    public static synchronized void open(ElarionConfigEditControl control) {
        openControl = control;
        lastResult = null;
    }

    public static synchronized Optional<ElarionConfigEditControl> openControl() {
        return Optional.ofNullable(openControl);
    }

    public static synchronized void closeOpenControl() {
        openControl = null;
    }

    public static synchronized Optional<ElarionConfigEditResultPayload> lastResult() {
        return Optional.ofNullable(lastResult);
    }

    public static synchronized void clear() {
        openControl = null;
        lastResult = null;
    }
}
