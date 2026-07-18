package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.model.ElarionDomainEvent;
import panetina.elarion.core.model.WorldheartAuthorityType;
import panetina.elarion.core.model.WorldheartGovernanceRole;
import panetina.elarion.core.storage.WorldheartAuthorityStorage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WorldheartGovernanceServiceTest {
    @TempDir
    Path root;

    @Test
    void playerAndSystemAuthorityPersistAndEmitDomainEvents() {
        UUID ruler = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        List<ElarionDomainEvent> events = new ArrayList<>();
        WorldheartGovernanceService service = service(Set.of(ruler), events);

        assertTrue(service.isSystemGoverned());
        assertEquals("Hollow Emperor", service.authorityDisplayName());
        service.setPlayerAuthority(ruler, other);

        assertEquals(WorldheartAuthorityType.PLAYER, service.authority().type());
        assertEquals(Optional.of(ruler), service.rulerId());
        assertTrue(service.isRuler(ruler));
        assertFalse(service.isRuler(other));
        assertEquals(WorldheartGovernanceRole.ADMINISTRATOR, service.role(other, true));
        assertEquals(WorldheartGovernanceRole.RULER, service.role(ruler, false));
        assertEquals(WorldheartGovernanceRole.NONE, service.role(other, false));
        assertEquals("Ruler Name", service.authorityDisplayName());

        WorldheartGovernanceService restarted = service(Set.of(ruler), new ArrayList<>());
        assertEquals(Optional.of(ruler), restarted.rulerId());
        restarted.setSystemAuthority(other);
        assertTrue(restarted.isSystemGoverned());

        assertEquals(1, events.size());
        assertEquals("worldheart-authority-changed", events.getFirst().eventType());
        assertEquals("SYSTEM", events.getFirst().metadata().get("previousType"));
        assertEquals("PLAYER", events.getFirst().metadata().get("newType"));
    }

    @Test
    void rejectsUnknownCitizenAsPlayerAuthority() {
        WorldheartGovernanceService service = service(Set.of(), new ArrayList<>());
        assertThrows(IllegalArgumentException.class,
                () -> service.setPlayerAuthority(UUID.randomUUID(), null));
    }

    private WorldheartGovernanceService service(Set<UUID> citizens, List<ElarionDomainEvent> captured) {
        ElarionEventBus bus = new ElarionEventBus();
        bus.onDomainEvent(captured::add);
        WorldheartGovernanceService service = new WorldheartGovernanceService(
                new WorldheartAuthorityStorage(LoggerFactory.getLogger("worldheart-test"), root),
                citizens::contains,
                id -> citizens.contains(id) ? Optional.of("Ruler Name") : Optional.empty(),
                bus);
        service.bind(null);
        return service;
    }
}
