package panetina.elarion.core.service;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;

/** Registers Core-owned gates before addon interaction callbacks are installed. */
public final class PlayerInteractionRestrictionRegistrar {
    private PlayerInteractionRestrictionRegistrar() {
    }

    public static void register(PlayerRestrictionService restrictions) {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) ->
                !(player instanceof ServerPlayerEntity serverPlayer)
                        || !restrictions.isRestricted(serverPlayer, PlayerRestrictionService.BREAK_BLOCK));
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
                denied(player, restrictions, PlayerRestrictionService.ATTACK_BLOCK));
        AttackEntityCallback.EVENT.register((player, world, hand, target, hit) ->
                denied(player, restrictions, PlayerRestrictionService.ATTACK_ENTITY));
        UseBlockCallback.EVENT.register((player, world, hand, hit) ->
                denied(player, restrictions, PlayerRestrictionService.INTERACT_BLOCK));
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) ->
                denied(player, restrictions, PlayerRestrictionService.INTERACT_ENTITY));
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (player instanceof ServerPlayerEntity serverPlayer
                    && restrictions.isRestricted(serverPlayer, PlayerRestrictionService.USE_ITEM)) {
                return TypedActionResult.fail(player.getStackInHand(hand));
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });
    }

    private static ActionResult denied(
            net.minecraft.entity.player.PlayerEntity player,
            PlayerRestrictionService restrictions,
            String action
    ) {
        return player instanceof ServerPlayerEntity serverPlayer
                && restrictions.isRestricted(serverPlayer, action)
                ? ActionResult.FAIL
                : ActionResult.PASS;
    }
}
