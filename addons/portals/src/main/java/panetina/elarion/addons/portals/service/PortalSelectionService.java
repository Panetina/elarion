package panetina.elarion.addons.portals.service;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import panetina.elarion.addons.portals.model.PortalBounds;
import panetina.elarion.addons.portals.model.PortalSelection;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PortalSelectionService {
    private final Map<UUID, PortalSelection> selections = new ConcurrentHashMap<>();

    public void first(ServerPlayerEntity player, BlockPos pos) {
        String world = world(player);
        PortalSelection current = selections.getOrDefault(player.getUuid(), new PortalSelection(world, null, null));
        PortalSelection updated = current.withFirst(world, pos);
        selections.put(player.getUuid(), updated);
        message(player, "First portal corner", pos, updated);
    }

    public void second(ServerPlayerEntity player, BlockPos pos) {
        String world = world(player);
        PortalSelection current = selections.getOrDefault(player.getUuid(), new PortalSelection(world, null, null));
        PortalSelection updated = current.withSecond(world, pos);
        selections.put(player.getUuid(), updated);
        message(player, "Second portal corner", pos, updated);
    }

    public PortalSelection require(ServerPlayerEntity player) {
        PortalSelection selection = selections.get(player.getUuid());
        if (selection == null || !selection.complete()) {
            throw new IllegalArgumentException("Select both portal corners with the Portal Surveyor first.");
        }
        PortalBounds.between(selection.first(), selection.second());
        return selection;
    }

    public void clear(ServerPlayerEntity player) {
        selections.remove(player.getUuid());
    }

    private static void message(
            ServerPlayerEntity player, String label, BlockPos pos, PortalSelection selection
    ) {
        String suffix = "";
        if (selection.complete()) {
            try {
                PortalBounds bounds = PortalBounds.between(selection.first(), selection.second());
                suffix = " | " + (bounds.maxX() - bounds.minX() + 1) + "x"
                        + (bounds.maxY() - bounds.minY() + 1) + "x"
                        + (bounds.maxZ() - bounds.minZ() + 1) + " axis=" + bounds.axis();
            } catch (IllegalArgumentException exception) {
                suffix = " | Invalid portal shape: select one block thick, at least two wide and two tall.";
            }
        }
        player.sendMessage(Text.literal(label + ": " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + suffix),
                true);
    }

    private static String world(ServerPlayerEntity player) {
        return player.getWorld().getRegistryKey().getValue().toString();
    }
}
