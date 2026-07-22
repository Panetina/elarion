package panetina.elarion.core.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

/** Client-side handoff of an opaque, server-issued passage receipt to the managed launcher instance. */
public final class LauncherPassageTicketStore {
    private static final Pattern TICKET = Pattern.compile("[A-Za-z0-9_-]{16,512}\\.[a-f0-9]{64}");

    private LauncherPassageTicketStore() {
    }

    public static void save(String uuid, String ticket) {
        if (uuid == null || !uuid.matches("[0-9a-fA-F-]{36}") || ticket == null || !TICKET.matcher(ticket).matches()) return;
        Path target = FabricLoader.getInstance().getConfigDir()
                .resolve("elarion").resolve("core").resolve("launcher-passage-ticket");
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            Files.createDirectories(target.getParent());
            Files.writeString(temporary, uuid.toLowerCase(java.util.Locale.ROOT) + "\n" + ticket, StandardCharsets.US_ASCII);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) {
            // Passage is supplemental launcher state; failure must not affect play.
        }
    }
}
