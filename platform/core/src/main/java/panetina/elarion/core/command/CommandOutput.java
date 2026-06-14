package panetina.elarion.core.command;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class CommandOutput {
    private CommandOutput() {
    }

    public static void header(ServerCommandSource source, String title) {
        source.sendFeedback(() -> Text.literal("== " + title + " ==")
                .formatted(Formatting.GOLD, Formatting.BOLD), false);
    }

    public static void section(ServerCommandSource source, String title) {
        source.sendFeedback(() -> Text.literal("-- " + title + " --")
                .formatted(Formatting.YELLOW), false);
    }

    public static void line(ServerCommandSource source, String text) {
        source.sendFeedback(() -> Text.literal(text), false);
    }

    public static void bullet(ServerCommandSource source, String text) {
        source.sendFeedback(() -> Text.literal(" - " + text), false);
    }

    public static void kv(ServerCommandSource source, String key, Object value) {
        source.sendFeedback(() -> Text.literal(" - ")
                .append(Text.literal(key + ": ").formatted(Formatting.GRAY))
                .append(Text.literal(String.valueOf(value)).formatted(Formatting.WHITE)), false);
    }

    public static void success(ServerCommandSource source, String text, boolean broadcastToOps) {
        source.sendFeedback(() -> Text.literal(text).formatted(Formatting.GREEN), broadcastToOps);
    }

    public static void empty(ServerCommandSource source, String text) {
        source.sendFeedback(() -> Text.literal(text).formatted(Formatting.GRAY), false);
    }

    public static Text status(String label, boolean active) {
        return Text.literal(label + ": ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(active ? "yes" : "no")
                        .formatted(active ? Formatting.GREEN : Formatting.RED));
    }

    public static MutableText row(String label, Object value) {
        return Text.literal(" - ")
                .append(Text.literal(label + ": ").formatted(Formatting.GRAY))
                .append(Text.literal(String.valueOf(value)));
    }
}
