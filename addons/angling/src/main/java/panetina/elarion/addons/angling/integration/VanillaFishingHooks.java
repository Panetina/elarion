package panetina.elarion.addons.angling.integration;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.angling.condition.AnglingConditionContext;
import panetina.elarion.addons.angling.service.AnglingFeedbackService;
import panetina.elarion.addons.angling.service.AnglingFishingTriggerService;

import java.util.Objects;

public final class VanillaFishingHooks {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_angling");
    private static AnglingFishingTriggerService triggers;
    private static AnglingFeedbackService feedback;

    private VanillaFishingHooks() {
    }

    public static void initialize(
            AnglingFishingTriggerService triggerService,
            AnglingFeedbackService feedbackService
    ) {
        if (triggers != null || feedback != null) {
            throw new IllegalStateException("Vanilla fishing hooks are already initialized");
        }
        triggers = Objects.requireNonNull(triggerService, "triggerService");
        feedback = Objects.requireNonNull(feedbackService, "feedbackService");
    }

    public static void onFishingTick(
            FishingBobberEntity bobber,
            ServerWorld world,
            BlockPos pos
    ) {
        if (!(bobber.getPlayerOwner() instanceof ServerPlayerEntity player)) return;
        Identifier worldId = world.getRegistryKey().getValue();
        Identifier dimensionId = world.getDimensionEntry().getKey()
                .orElseThrow(() -> new IllegalStateException("Fishing world has no dimension registry key"))
                .getValue();
        Identifier biomeId = world.getBiome(pos).getKey()
                .orElseThrow(() -> new IllegalStateException("Fishing position has no biome registry key"))
                .getValue();
        Identifier fluidId = Registries.FLUID.getId(world.getFluidState(pos).getFluid());
        long timeOfDay = Math.floorMod(
                world.getTimeOfDay(),
                AnglingConditionContext.TICKS_PER_DAY);

        if (requireTriggers().beginIfAbsent(
                new AnglingConditionContext(
                        player.getUuid(),
                        worldId,
                        dimensionId,
                        biomeId,
                        fluidId,
                        null,
                        pos.getY(),
                        timeOfDay,
                        world.isRaining(),
                        world.isThundering()),
                world.getRandom().nextLong()).isEmpty()) {
            sendUnavailableFeedback(player);
        }
    }

    public static void onBobberUnloaded(FishingBobberEntity bobber) {
        if (bobber.getPlayerOwner() instanceof ServerPlayerEntity player) {
            requireTriggers().cancel(player.getUuid());
        }
    }

    public static boolean beforeVanillaFishingLoot(FishingBobberEntity bobber) {
        if (bobber.getPlayerOwner() instanceof ServerPlayerEntity player) {
            boolean accepted;
            try {
                accepted = requireTriggers().complete(player.getUuid()).isPresent();
            } catch (RuntimeException exception) {
                LOGGER.error(
                        "Blocked vanilla fishing loot because catch telemetry was not accepted for {}",
                        player.getUuid(),
                        exception);
                return false;
            }
            if (accepted) sendAcceptedFeedback(player);
        }
        return true;
    }

    private static void sendAcceptedFeedback(ServerPlayerEntity player) {
        try {
            requireFeedback().accepted(player);
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Unable to send accepted-catch feedback to {}",
                    player.getUuid(),
                    exception);
        }
    }

    private static void sendUnavailableFeedback(ServerPlayerEntity player) {
        try {
            requireFeedback().unavailable(player);
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Unable to send unavailable-catch feedback to {}",
                    player.getUuid(),
                    exception);
        }
    }

    private static AnglingFishingTriggerService requireTriggers() {
        if (triggers == null) {
            throw new IllegalStateException("Vanilla fishing hooks are not initialized");
        }
        return triggers;
    }

    private static AnglingFeedbackService requireFeedback() {
        if (feedback == null) {
            throw new IllegalStateException("Vanilla fishing feedback is not initialized");
        }
        return feedback;
    }
}
