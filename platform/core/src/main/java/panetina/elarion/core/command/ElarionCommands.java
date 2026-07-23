package panetina.elarion.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.api.ElarionCommandRegistry;
import panetina.elarion.core.config.ConfigValidationException;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.service.ElarionPerformanceMonitor;

import java.util.function.Supplier;

import static net.minecraft.server.command.CommandManager.literal;

public final class ElarionCommands {
    private ElarionCommands() {
    }

    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess,
            ElarionApi api,
            CoreConfigManager config,
            ElarionCommandRegistry extensions
    ) {
        CommandPolicy.applyVanillaPolicy(dispatcher);
        PlayerCommandRegistrar.register(dispatcher, api, config, extensions);

        for (Supplier<LiteralArgumentBuilder<ServerCommandSource>> extension : extensions.rootCommands()) {
            dispatcher.register(extension.get());
        }

        LiteralArgumentBuilder<ServerCommandSource> root = literal("e")
                .requires(source -> source.hasPermissionLevel(4))
                .then(RealmCommandRegistrar.register(api, registryAccess))
                .then(CitizenCommandRegistrar.register(api))
                .then(TitleCommandRegistrar.register(api))
                .then(AbilityCommandRegistrar.register(api))
                .then(AdminPanelCommandRegistrar.register(api))
                .then(RewardCommandRegistrar.register(api))
                .then(ProgressionCommandRegistrar.register(api))
                .then(HistoryCommandRegistrar.register(api))
                .then(literal("reload").executes(context -> {
                    long started = System.nanoTime();
                    try {
                        try {
                            config.load();
                        } catch (ConfigValidationException exception) {
                            exception.errors().forEach(error ->
                                    context.getSource().sendError(Text.literal(error)));
                            context.getSource().sendError(Text.literal(
                                    "Reload rejected; the previous valid configuration remains active."));
                            return 0;
                        }
                        api.titles().all().forEach(title -> title.abilities().forEach(api.abilities()::register));
                        api.progression().reloadRules();
                        api.realms().initializeScoreboardTeams(context.getSource().getServer());
                        for (ServerPlayerEntity player :
                                context.getSource().getServer().getPlayerManager().getPlayerList()) {
                            api.realms().applyCurrentScoreboardTeam(player);
                            api.progression().reconcileMetricRules(player);
                        }
                        api.identitySync().syncAll(context.getSource().getServer());
                        api.uiThemes().syncAll(context.getSource().getServer());
                        context.getSource().sendFeedback(() -> Text.literal("Elarion configuration reloaded."), true);
                        return 1;
                    } finally {
                        ElarionPerformanceMonitor.record("core-reload", System.nanoTime() - started);
                    }
                }));

        LiteralArgumentBuilder<ServerCommandSource> testRoot = literal("test")
                .requires(source -> source.hasPermissionLevel(4));
        for (Supplier<LiteralArgumentBuilder<ServerCommandSource>> extension : extensions.testCommands()) {
            testRoot.then(extension.get());
        }
        root.then(testRoot);

        for (Supplier<LiteralArgumentBuilder<ServerCommandSource>> extension : extensions.adminCommands()) {
            root.then(extension.get());
        }
        dispatcher.register(root);
    }
}
