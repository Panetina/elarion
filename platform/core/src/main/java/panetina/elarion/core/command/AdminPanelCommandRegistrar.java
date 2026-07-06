package panetina.elarion.core.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.core.api.ElarionApi;

import static net.minecraft.server.command.CommandManager.literal;

public final class AdminPanelCommandRegistrar {
    private AdminPanelCommandRegistrar() {
    }

    public static LiteralArgumentBuilder<ServerCommandSource> register(ElarionApi api) {
        return literal("panel")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) {
                        context.getSource().sendError(Text.literal("/e panel requires an in-game admin player."));
                        return 0;
                    }
                    api.system().adminPanel().open(player);
                    return 1;
                });
    }
}
