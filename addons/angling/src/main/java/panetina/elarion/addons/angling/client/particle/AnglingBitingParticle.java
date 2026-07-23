package panetina.elarion.addons.angling.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

/** Shared exact motion/animation used by the reference water and lava bite particles. */
public final class AnglingBitingParticle extends SpriteBillboardParticle {
    private final SpriteProvider sprites;

    private AnglingBitingParticle(ClientWorld world, double x, double y, double z, SpriteProvider sprites) {
        super(world, x, y, z);
        velocityX = random.nextFloat() * 0.2F - 0.1F;
        velocityY = random.nextFloat() * 0.2F + 0.1F;
        velocityZ = random.nextFloat() * 0.2F - 0.1F;
        scale = random.nextFloat() * 0.2F + 0.05F;
        maxAge = 20;
        this.sprites = sprites;
        setSpriteForAge(sprites);
    }

    @Override
    public void tick() {
        setSpriteForAge(sprites);
        velocityY -= 0.01F;
        velocityX *= 0.95F;
        velocityY *= 0.95F;
        velocityZ *= 0.95F;
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
                new AnglingBitingParticle(world, x, y, z, sprites);
    }
}
