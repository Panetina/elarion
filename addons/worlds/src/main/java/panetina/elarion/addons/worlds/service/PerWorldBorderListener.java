package panetina.elarion.addons.worlds.service;

import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;

import java.util.function.Consumer;

/**
 * Sends border changes only to players in the affected world.
 *
 * Behavior adapted from PotatoPresident/worldborderfixer (MIT).
 */
public final class PerWorldBorderListener implements WorldBorderListener {
    private final ServerWorld world;
    private final Consumer<WorldBorder> onChange;

    public PerWorldBorderListener(ServerWorld world, Consumer<WorldBorder> onChange) {
        this.world = world;
        this.onChange = onChange;
    }

    @Override
    public void onSizeChange(WorldBorder border, double size) {
        world.getPlayers().forEach(player ->
                player.networkHandler.sendPacket(new WorldBorderSizeChangedS2CPacket(border)));
        onChange.accept(border);
    }

    @Override
    public void onInterpolateSize(WorldBorder border, double fromSize, double toSize, long time) {
        world.getPlayers().forEach(player ->
                player.networkHandler.sendPacket(new WorldBorderInitializeS2CPacket(border)));
        onChange.accept(border);
    }

    @Override
    public void onCenterChanged(WorldBorder border, double centerX, double centerZ) {
        world.getPlayers().forEach(player ->
                player.networkHandler.sendPacket(new WorldBorderCenterChangedS2CPacket(border)));
        onChange.accept(border);
    }

    @Override
    public void onWarningTimeChanged(WorldBorder border, int warningTime) {
        world.getPlayers().forEach(player ->
                player.networkHandler.sendPacket(new WorldBorderWarningTimeChangedS2CPacket(border)));
        onChange.accept(border);
    }

    @Override
    public void onWarningBlocksChanged(WorldBorder border, int warningBlockDistance) {
        world.getPlayers().forEach(player ->
                player.networkHandler.sendPacket(new WorldBorderWarningBlocksChangedS2CPacket(border)));
        onChange.accept(border);
    }

    @Override
    public void onDamagePerBlockChanged(WorldBorder border, double damagePerBlock) {
        onChange.accept(border);
    }

    @Override
    public void onSafeZoneChanged(WorldBorder border, double safeZoneRadius) {
        onChange.accept(border);
    }
}
