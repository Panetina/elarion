package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import panetina.elarion.core.model.CatchSummary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class CatchSummaryStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public CatchSummary load(Path elarionRoot, UUID actorId) throws IOException {
        Path file = summaryPath(elarionRoot, actorId);
        if (Files.notExists(file)) return CatchSummary.empty(actorId);
        CatchSummary summary = CatchSummaryCodec.decode(
                file.toString(), Files.readString(file, StandardCharsets.UTF_8));
        if (!actorId.equals(summary.actorId())) {
            throw new CatchTelemetryFormatException(file + ": actorId does not match summary path");
        }
        return summary;
    }

    public void save(Path elarionRoot, CatchSummary summary) throws IOException {
        JsonStateStorage.writeAtomicChecked(
                summaryPath(elarionRoot, summary.actorId()),
                GSON,
                CatchSummaryCodec.encode(summary),
                "catch summary");
    }

    public static Path summaryPath(Path elarionRoot, UUID actorId) {
        if (elarionRoot == null) throw new NullPointerException("elarionRoot");
        if (actorId == null) throw new NullPointerException("actorId");
        return elarionRoot.resolve("catch-telemetry")
                .resolve("players")
                .resolve(actorId + ".json");
    }
}
