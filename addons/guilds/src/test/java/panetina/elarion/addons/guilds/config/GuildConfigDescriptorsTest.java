package panetina.elarion.addons.guilds.config;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.guilds.model.GuildConfig;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigRegistry;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GuildConfigDescriptorsTest {
    @Test
    void registersGuildsDomainWithDefaultMetadata() {
        AtomicReference<GuildConfig> current = new AtomicReference<>(GuildConfig.defaults());
        ElarionConfigRegistry registry = new ElarionConfigRegistry();

        GuildConfigDescriptors.register(registry, current::get);

        ElarionConfigDomain domain = registry.domain("guilds").orElseThrow();
        assertEquals("addons:guilds", domain.ownerModule());
        assertEquals("/e guild reload", domain.reloadCommand());
        assertTrue(domain.files().contains("config/elarion/addons/guilds/guilds.yml"));
        assertTrue(domain.category("general").isPresent());
        assertTrue(domain.category("identity").isPresent());
        assertTrue(domain.category("tags").isPresent());
        assertTrue(domain.category("invitations").isPresent());
    }

    @Test
    void reflectsCurrentRuntimeConfigAfterReload() {
        AtomicReference<GuildConfig> current = new AtomicReference<>(GuildConfig.defaults());
        ElarionConfigDomain domain = GuildConfigDescriptors.domain(current::get);

        ElarionConfigEntry<?> creationFee = domain.entry("general", "creation.fee").orElseThrow();
        ElarionConfigEntry<?> maxName = domain.entry("identity", "identity.max-name-length").orElseThrow();
        ElarionConfigEntry<?> blocked = domain.entry("tags", "tags.blocked").orElseThrow();
        ElarionConfigEntry<?> inviteDays =
                domain.entry("invitations", "invitations.lifetime-days").orElseThrow();

        assertEquals("25", creationFee.currentDisplayValue());
        assertEquals("48", maxName.currentDisplayValue());
        assertEquals("7", inviteDays.currentDisplayValue());

        current.set(new GuildConfig(
                true,
                80L,
                3,
                8,
                64,
                "[a-z][a-z0-9_-]{2,31}",
                "[A-Z0-9]{3,8}",
                Set.of("ADMIN", "KING"),
                Duration.ofDays(14).toMillis()));

        assertEquals("80", creationFee.currentDisplayValue());
        assertEquals("64", maxName.currentDisplayValue());
        assertEquals("14", inviteDays.currentDisplayValue());
        assertTrue(blocked.currentDisplayValue().contains("ADMIN"));
        assertTrue(blocked.currentDisplayValue().contains("KING"));
        assertTrue(creationFee.validateCurrent().isEmpty());
    }
}
