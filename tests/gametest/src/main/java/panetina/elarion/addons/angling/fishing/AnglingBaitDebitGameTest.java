package panetina.elarion.addons.angling.fishing;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import panetina.elarion.addons.angling.component.AnglingAttachments;
import panetina.elarion.addons.angling.component.AnglingBaitDebitCursor;
import panetina.elarion.addons.angling.component.AnglingDataComponents;
import panetina.elarion.addons.angling.component.AnglingSingleStackComponent;
import panetina.elarion.addons.angling.registry.AnglingItems;

/** Exercises the player inventory/cursor half of the append-first bait debit path. */
public final class AnglingBaitDebitGameTest implements FabricGameTest {
    @SuppressWarnings("removal") // The 1.21.1 GameTest API exposes ServerPlayerEntity only through this helper.
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void debitIsAtomicIdempotentAndFailsWhenBaitIsMissing(TestContext context) {
        ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
        ItemStack rod = new ItemStack(AnglingItems.require("elarion_angling_rod"));
        ItemStack bait = new ItemStack(AnglingItems.require("worm"), 1);
        Identifier baitId = Registries.ITEM.getId(bait.getItem());
        rod.set(AnglingDataComponents.BAIT, new AnglingSingleStackComponent(bait));
        player.getInventory().main.set(0, rod);

        context.assertTrue(AnglingCatchDeliveryService.applyDebit(player, baitId, 1),
                "The first durable bait debit should consume the available bait");
        assertBaitCount(context, player, 0);
        AnglingBaitDebitCursor cursor = player.getAttachedOrCreate(AnglingAttachments.BAIT_DEBIT_CURSOR);
        context.assertEquals(cursor.applied(baitId), 1L,
                "The player-file cursor should advance with the inventory debit");

        context.assertTrue(AnglingCatchDeliveryService.applyDebit(player, baitId, 1),
                "An exact replay must observe the cursor and avoid a second debit");
        assertBaitCount(context, player, 0);
        context.assertFalse(AnglingCatchDeliveryService.applyDebit(player, baitId, 2),
                "A later debit must fail without enough bait");
        context.assertEquals(player.getAttachedOrCreate(AnglingAttachments.BAIT_DEBIT_CURSOR).applied(baitId), 1L,
                "A failed debit must not advance the player-file cursor");
        assertBaitCount(context, player, 0);
        context.complete();
    }

    private static void assertBaitCount(TestContext context, ServerPlayerEntity player, int expected) {
        ItemStack rod = player.getInventory().main.getFirst();
        context.assertEquals(rod.getOrDefault(AnglingDataComponents.BAIT,
                        AnglingSingleStackComponent.EMPTY).stack().getCount(), expected,
                "Rod bait count mismatch");
    }
}
