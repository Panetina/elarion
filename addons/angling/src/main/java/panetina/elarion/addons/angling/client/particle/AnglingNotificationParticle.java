package panetina.elarion.addons.angling.client.particle;

import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

/** Exact rising/fading notification particle from the frozen reference. */
public final class AnglingNotificationParticle extends SpriteBillboardParticle {
    private final SpriteProvider sprites;

    private AnglingNotificationParticle(ClientWorld world, double x, double y, double z, SpriteProvider sprites) {
        super(world, x, y, z);
        velocityX = 0.0;
        velocityY = 0.0;
        velocityZ = 0.0;
        scale = 0.3F;
        maxAge = 15;
        this.sprites = sprites;
        setSpriteForAge(sprites);
    }

    @Override
    public void tick() {
        setSpriteForAge(sprites);
        alpha -= 0.06F;
        velocityY = 0.06F;
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
        if (age++ >= maxAge) markDead();
        move(velocityX, velocityY, velocityZ);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static ParticleFactory<SimpleParticleType> factory(SpriteProvider sprites) {
        return (type, world, x, y, z, velocityX, velocityY, velocityZ) ->
                new AnglingNotificationParticle(world, x, y, z, sprites);
    }
}
