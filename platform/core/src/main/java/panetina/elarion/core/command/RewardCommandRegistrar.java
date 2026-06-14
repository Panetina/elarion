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

final class RewardCommandRegistrar {
    private RewardCommandRegistrar() {
    }

    static LiteralArgumentBuilder<ServerCommandSource> register(ElarionApi api) {
        return literal("reward")
                .then(literal("run")
                        .then(argument("reward", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    api.rewards().rewardIds().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String reward = StringArgumentType.getString(context, "reward");
                                            boolean success = api.rewards().executeReward(reward, player);
                                            if (!success) {
                                                context.getSource().sendError(Text.literal("Unknown or failed reward: " + reward));
                                                return 0;
                                            }
                                            CommandOutput.success(context.getSource(), "Reward executed.", true);
                                            CommandOutput.kv(context.getSource(), "Reward", reward);
                                            CommandOutput.kv(context.getSource(), "Player", player.getGameProfile().getName());
                                            return 1;
                                        }))));
    }
}
