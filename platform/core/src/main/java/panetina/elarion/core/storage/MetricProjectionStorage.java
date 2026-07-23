package panetina.elarion.core.storage;

import panetina.elarion.core.metric.MetricProjectionState;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/** Strict fail-closed atomic storage for current metric projections. */
public final class MetricProjectionStorage {
    public MetricProjectionState load(Path elarionRoot) throws IOException {
        Path file = statePath(elarionRoot);
        if (Files.notExists(file)) return MetricProjectionState.empty();
        return MetricPersistenceCodec.decodeState(file.toString(), Files.readString(file, StandardCharsets.UTF_8));
    }

    public void save(Path elarionRoot, MetricProjectionState state) throws IOException {
        Path file = statePath(elarionRoot);
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.createDirectories(file.getParent());
        try {
            Files.writeString(temporary, MetricPersistenceCodec.encodeState(state), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                channel.force(false);
            }
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFailure) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException cleanup) {
                exception.addSuppressed(cleanup);
            }
            throw exception;
        }
    }

    static Path statePath(Path elarionRoot) {
        return elarionRoot.resolve("metrics").resolve("current.json");
    }
}
