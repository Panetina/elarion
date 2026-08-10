package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.data.BackpackDataLoaderClient;
import com.yyz.yyzsbackpack.item.ModItems;
import com.yyz.yyzsbackpack.network.handler.ClientPacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.component.DyedItemColor;

public class BackpackClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPacketHandler.register();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new BackpackDataLoaderClient.ReloadListener(ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "client_backpack_data")));
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                    if (tintIndex == 0) {
                        return DyedItemColor.getOrDefault(stack, -6265536);
                    }
                    return -1;
                }, ModItems.IRON_BACKPACK, ModItems.GOLD_BACKPACK,
                ModItems.DIAMOND_BACKPACK, ModItems.NETHERITE_BACKPACK);
    }
}
