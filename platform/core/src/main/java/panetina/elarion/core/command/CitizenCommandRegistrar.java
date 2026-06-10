package panetina.elarion.core.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.service.NicknameService;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static panetina.elarion.core.command.CommandSupport.value;

final class CitizenCommandRegistrar {
    private CitizenCommandRegistrar() {
    }

    static LiteralArgumentBuilder<ServerCommandSource> register(ElarionApi api) {
        return literal("citizen")
                .then(literal("info")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    CitizenRecord citizen = api.citizens().getOrCreate(player);
                                    String info = "Citizen " + player.getGameProfile().getName()
                                            + " | realm=" + value(citizen.realmId())
                                            + " | title=" + value(citizen.titleId())
                                            + " | nickname=" + value(citizen.nickname())
                                            + " | status=" + citizen.status();
                                    context.getSource().sendFeedback(() -> Text.literal(info), false);
                                    return 1;
                                })))
                .then(literal("nickname")
                        .then(literal("set")
                                .then(argument("player", EntityArgumentType.player())
                                        .then(argument("nickname", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                    String input = StringArgumentType.getString(context, "nickname");
                                                    NicknameService.Validation validation =
                                                            api.nicknames().validate(player.getUuid(), input);
                                                    if (!validation.valid()) {
                                                        context.getSource().sendError(Text.literal(validation.error()));
                                                        return 0;
                                                    }
                                                    api.citizens().update(player, "nickname-set",
                                                            citizen -> citizen.setNickname(validation.nickname()));
                                                    context.getSource().sendFeedback(
                                                            () -> Text.literal("Set nickname for "
                                                                    + player.getGameProfile().getName()), true);
                                                    return 1;
                                                }))))
                        .then(literal("clear")
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            api.citizens().update(player, "nickname-cleared",
                                                    citizen -> citizen.setNickname(null));
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Cleared nickname for "
                                                            + player.getGameProfile().getName()), true);
                                            return 1;
                                        }))));
    }
}
