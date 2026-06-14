package panetina.elarion.core.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
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
                                            CommandOutput.header(context.getSource(), "Ability Check");
                                            CommandOutput.kv(context.getSource(), "Player", player.getGameProfile().getName());
                                            CommandOutput.kv(context.getSource(), "Ability", ability);
                                            CommandOutput.kv(context.getSource(), "Allowed", allowed);
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
                                            CommandOutput.success(context.getSource(), "Ability granted.", true);
                                            CommandOutput.kv(context.getSource(), "Player", player.getGameProfile().getName());
                                            CommandOutput.kv(context.getSource(), "Ability", ability);
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
                                            CommandOutput.success(context.getSource(), "Ability revoked.", true);
                                            CommandOutput.kv(context.getSource(), "Player", player.getGameProfile().getName());
                                            CommandOutput.kv(context.getSource(), "Ability", ability);
                                            return 1;
                                        }))));
    }
}
