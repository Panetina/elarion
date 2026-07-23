package panetina.elarion.addons.angling.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import panetina.elarion.addons.angling.client.particle.AnglingBitingParticle;
import panetina.elarion.addons.angling.client.particle.AnglingNotificationParticle;
import panetina.elarion.addons.angling.registry.AnglingParticles;

/** Client-only registrations; never loaded by a dedicated server. */
public final class ElarionAnglingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry factories = ParticleFactoryRegistry.getInstance();
        factories.register(AnglingParticles.FISHING_BITING, AnglingBitingParticle::factory);
        factories.register(AnglingParticles.FISHING_BITING_LAVA, AnglingBitingParticle::factory);
        factories.register(AnglingParticles.VALLEY_NOTIFICATION, AnglingNotificationParticle::factory);
        AnglingClientNetworking.initialize();
    }
}
