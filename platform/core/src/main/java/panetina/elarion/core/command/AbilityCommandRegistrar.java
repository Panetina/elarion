package panetina.elarion.core.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.core.api.ElarionApi;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

final class AbilityCommandRegistrar {
    private AbilityCommandRegistrar() {
    }

    static LiteralArgumentBuilder<ServerCommandSource> register(ElarionApi api) {
        return literal("ability")
                .then(literal("check")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("ability", StringArgumentType.word())
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String ability = StringArgumentType.getString(context, "ability");
                                            boolean allowed = api.abilities().has(api.citizens().getOrCreate(player), ability);
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal(player.getGameProfile().getName()
                                                            + " ability " + ability + ": " + allowed), false);
                                            return allowed ? 1 : 0;
                                        }))))
                .then(literal("grant")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("ability", StringArgumentType.word())
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String ability = StringArgumentType.getString(context, "ability");
                                            api.citizens().update(player, "ability-granted",
                                                    citizen -> api.abilities().grant(citizen, ability));
                                            context.getSource().sendFeedback(() -> Text.literal("Granted " + ability), true);
                                            return 1;
                                        }))))
                .then(literal("revoke")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("ability", StringArgumentType.word())
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String ability = StringArgumentType.getString(context, "ability");
                                            api.citizens().update(player, "ability-revoked",
                                                    citizen -> api.abilities().revoke(citizen, ability));
                                            context.getSource().sendFeedback(() -> Text.literal("Revoked " + ability), true);
                                            return 1;
                                        }))));
    }
}
