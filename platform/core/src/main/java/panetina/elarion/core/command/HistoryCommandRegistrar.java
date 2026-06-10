package panetina.elarion.core.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.ChronicleArchive;
import panetina.elarion.core.model.ChronicleEntry;
import panetina.elarion.core.model.HistoryEvent;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

final class HistoryCommandRegistrar {
    private HistoryCommandRegistrar() {
    }

    static LiteralArgumentBuilder<ServerCommandSource> register(ElarionApi api) {
        int maxLimit = api.history().commandLimitMax();
        return literal("history")
                .then(literal("recent")
                        .executes(context -> sendHistory(
                                context.getSource(), api.history().recent(10)))
                        .then(argument("limit", IntegerArgumentType.integer(1, maxLimit))
                                .executes(context -> sendHistory(context.getSource(), api.history().recent(
                                        IntegerArgumentType.getInteger(context, "limit"))))))
                .then(literal("player")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> sendHistory(
                                        context.getSource(),
                                        api.history().forPlayer(
                                                EntityArgumentType.getPlayer(context, "player").getUuid(), 10)))
                                .then(argument("limit", IntegerArgumentType.integer(1, maxLimit))
                                        .executes(context -> sendHistory(
                                                context.getSource(),
                                                api.history().forPlayer(
                                                        EntityArgumentType.getPlayer(context, "player").getUuid(),
                                                        IntegerArgumentType.getInteger(context, "limit")))))))
                .then(literal("realm")
                        .then(argument("realm", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    api.realms().all().forEach(value -> builder.suggest(value.id()));
                                    return builder.buildFuture();
                                })
                                .executes(context -> sendHistory(
                                        context.getSource(),
                                        api.history().forRealm(
                                                StringArgumentType.getString(context, "realm"), 10)))
                                .then(argument("limit", IntegerArgumentType.integer(1, maxLimit))
                                        .executes(context -> sendHistory(
                                                context.getSource(),
                                                api.history().forRealm(
                                                        StringArgumentType.getString(context, "realm"),
                                                        IntegerArgumentType.getInteger(context, "limit")))))))
                .then(literal("category")
                        .then(argument("category", StringArgumentType.word())
                                .executes(context -> sendHistory(
                                        context.getSource(),
                                        api.history().forCategory(
                                                StringArgumentType.getString(context, "category"), 10)))
                                .then(argument("limit", IntegerArgumentType.integer(1, maxLimit))
                                        .executes(context -> sendHistory(
                                                context.getSource(),
                                                api.history().forCategory(
                                                        StringArgumentType.getString(context, "category"),
                                                        IntegerArgumentType.getInteger(context, "limit")))))))
                .then(literal("chronicle")
                        .then(literal("list")
                                .executes(context -> sendChronicleList(context.getSource(), api, 8))
                                .then(argument("weeks", IntegerArgumentType.integer(1, 52))
                                        .executes(context -> sendChronicleList(
                                                context.getSource(),
                                                api,
                                                IntegerArgumentType.getInteger(context, "weeks")))))
                        .then(literal("inspect")
                                .then(argument("week", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            api.publicHistory().recentChronicles(52)
                                                    .forEach(archive -> builder.suggest(archive.weekStart()));
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> sendChronicleInspect(
                                                context.getSource(),
                                                api,
                                                StringArgumentType.getString(context, "week"),
                                                20))
                                        .then(argument("limit", IntegerArgumentType.integer(1, maxLimit))
                                                .executes(context -> sendChronicleInspect(
                                                        context.getSource(),
                                                        api,
                                                        StringArgumentType.getString(context, "week"),
                                                        IntegerArgumentType.getInteger(context, "limit")))))));
    }

    private static int sendHistory(ServerCommandSource source, List<HistoryEvent> events) {
        if (events.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No matching Elarion history events."), false);
            return 0;
        }
        source.sendFeedback(() -> Text.literal("Elarion history (" + events.size() + "):"), false);
        for (HistoryEvent event : events) {
            source.sendFeedback(() -> Text.literal(formatHistory(event)), false);
        }
        return events.size();
    }

    private static int sendChronicleList(ServerCommandSource source, ElarionApi api, int weeks) {
        List<ChronicleArchive> archives = api.publicHistory().recentChronicles(weeks);
        if (archives.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No Chronicle archives found."), false);
            return 0;
        }
        source.sendFeedback(() -> Text.literal("Chronicle archives (" + archives.size() + "):"), false);
        for (ChronicleArchive archive : archives) {
            source.sendFeedback(() -> Text.literal(archive.weekStart() + " to " + archive.weekEnd()
                    + " events=" + archive.totalEvents()
                    + " categories=" + summarize(archive.categoryCounts())), false);
        }
        return archives.size();
    }

    private static int sendChronicleInspect(ServerCommandSource source, ElarionApi api, String week, int limit) {
        ChronicleArchive archive = api.publicHistory().recentChronicles(52).stream()
                .filter(value -> value.weekStart().equalsIgnoreCase(week))
                .findFirst()
                .orElse(null);
        if (archive == null) {
            source.sendError(Text.literal("Unknown Chronicle archive week: " + week));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("Chronicle " + archive.weekStart() + " to " + archive.weekEnd()
                + " events=" + archive.totalEvents()
                + " realms=" + summarize(archive.realmCounts())), false);
        int count = 0;
        for (ChronicleEntry entry : archive.entries()) {
            if (count >= limit) break;
            source.sendFeedback(() -> Text.literal(formatChronicleEntry(entry)), false);
            count++;
        }
        return count;
    }

    private static String formatHistory(HistoryEvent event) {
        String time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(event.timestamp()));
        String actor = event.actorId() == null ? "-" : event.actorId().toString();
        String subject = event.subjectType().isBlank()
                ? "-"
                : event.subjectType() + ":" + event.subjectId();
        String realm = event.realmId().isBlank() ? "-" : event.realmId();
        String metadata = event.metadata().isEmpty() ? "" : " " + event.metadata();
        return "[" + time + "] " + event.category() + "/" + event.type()
                + " actor=" + actor + " subject=" + subject
                + " realm=" + realm + metadata;
    }

    private static String formatChronicleEntry(ChronicleEntry entry) {
        String time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(entry.timestamp()));
        String realm = entry.realmId().isBlank() ? "-" : entry.realmId();
        return "[" + time + "] " + entry.category() + "/" + entry.type()
                + " realm=" + realm + " " + entry.text();
    }

    private static String summarize(Map<String, Integer> counts) {
        if (counts.isEmpty()) return "{}";
        return counts.entrySet().stream()
                .limit(5)
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((first, second) -> first + ", " + second)
                .map(value -> "{" + value + (counts.size() > 5 ? ", ..." : "") + "}")
                .orElse("{}");
    }
}
