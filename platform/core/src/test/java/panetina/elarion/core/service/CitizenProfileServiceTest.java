package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.profile.CitizenProfileField;
import panetina.elarion.core.model.profile.CitizenProfileRequestContext;
import panetina.elarion.core.model.profile.CitizenProfileSection;
import panetina.elarion.core.model.profile.CitizenProfileSnapshot;
import panetina.elarion.core.model.profile.ProfileVisibility;
import panetina.elarion.core.storage.CitizenStorage;
import panetina.elarion.core.storage.TitleClaimStorage;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CitizenProfileServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void publicSnapshotShowsOnlyPublicCoreFields() {
        CitizenProfileService service = service();
        CitizenRecord target = citizen();
        UUID viewer = UUID.fromString("22222222-2222-2222-2222-222222222222");

        CitizenProfileSnapshot snapshot = service.snapshot(
                CitizenProfileRequestContext.publicView(viewer, target.uuid()),
                target);

        assertEquals("Mara", snapshot.title());
        CitizenProfileSection identity = snapshot.section("core.identity").orElseThrow();
        assertVisible(identity, "display-name");
        assertVisible(identity, "status");
        assertEquals("Active Ember", value(identity, "citizenship"));
        assertEquals("Ember", value(identity, "civic-standing"));
        assertHidden(identity, "citizen-id");
        assertHidden(identity, "username");
        assertHidden(identity, "abilities-granted");
        CitizenProfileSection realm = snapshot.section("core.realm").orElseThrow();
        assertEquals("Wilderness I", value(realm, "realm-name"));
        assertHidden(realm, "realm-id");
    }

    @Test
    void selfSnapshotIncludesPrivateCoreFields() {
        CitizenProfileService service = service();
        CitizenRecord target = citizen();

        CitizenProfileSnapshot snapshot = service.snapshot(CitizenProfileRequestContext.self(target.uuid()), target);

        CitizenProfileSection identity = snapshot.section("core.identity").orElseThrow();
        assertEquals(target.uuid().toString(), value(identity, "citizen-id"));
        assertEquals("MaraAccount", value(identity, "username"));
        assertEquals("2026-01-01T00:00:00Z", value(identity, "joined-at"));
        assertEquals("2026-01-02T00:00:00Z", value(identity, "last-seen-at"));
        assertEquals("0", value(identity, "abilities-granted"));
        CitizenProfileSection title = snapshot.section("core.title").orElseThrow();
        assertEquals("Ember", value(title, "active-title"));
        assertEquals("citizen", value(title, "title-id"));
        assertEquals("1", value(title, "titles-unlocked"));
    }

    @Test
    void adminSnapshotIncludesPrivateFieldsForOtherCitizen() {
        CitizenProfileService service = service();
        CitizenRecord target = citizen();
        UUID admin = UUID.fromString("33333333-3333-3333-3333-333333333333");

        CitizenProfileSnapshot snapshot = service.snapshot(
                CitizenProfileRequestContext.admin(admin, target.uuid()),
                target);

        assertEquals(target.uuid().toString(), value(snapshot.section("core.identity").orElseThrow(), "citizen-id"));
    }

    @Test
    void contributorSectionsAreFilteredAndBounded() {
        CitizenProfileService service = service();
        service.registerContributor(new TestContributor());
        CitizenRecord target = citizen();

        CitizenProfileSnapshot snapshot = service.snapshot(CitizenProfileRequestContext.self(target.uuid()), target);

        assertTrue(snapshot.sections().size() <= CitizenProfileService.MAX_SECTIONS);
        CitizenProfileSection addon = snapshot.section("addon.section-0").orElseThrow();
        assertEquals(CitizenProfileService.MAX_FIELDS_PER_SECTION, addon.fields().size());
        assertHidden(addon, "admin-only");
    }

    @Test
    void duplicateContributorIdsAreRejected() {
        CitizenProfileService service = service();
        service.registerContributor(new TestContributor());

        assertThrows(IllegalArgumentException.class, () -> service.registerContributor(new TestContributor()));
    }

    @Test
    void contributorFailureDiagnosticIsBoundedAndClearsAfterRecovery() {
        CitizenProfileService service = service();
        AtomicBoolean failing = new AtomicBoolean(true);
        service.registerContributor(new panetina.elarion.core.model.profile.CitizenProfileContributor() {
            @Override
            public String id() {
                return "unstable-addon";
            }

            @Override
            public List<CitizenProfileSection> sections(
                    CitizenProfileRequestContext context,
                    CitizenRecord target
            ) {
                if (failing.get()) throw new IllegalStateException("test failure");
                return List.of();
            }
        });
        CitizenRecord target = citizen();
        CitizenProfileRequestContext context = CitizenProfileRequestContext.self(target.uuid());

        service.snapshot(context, target);
        service.snapshot(context, target);
        assertEquals(1, service.failedContributorCount());

        failing.set(false);
        service.snapshot(context, target);
        assertEquals(0, service.failedContributorCount());
    }

    private CitizenProfileService service() {
        CoreConfigManager config = new CoreConfigManager(LoggerFactory.getLogger("profile-test"), tempDir);
        config.load();
        CitizenService citizens = new CitizenService(
                new CitizenStorage(LoggerFactory.getLogger("profile-test")),
                config,
                new ElarionEventBus());
        RealmService realms = new RealmService(config, citizens);
        TitleService titles = new TitleService(
                config,
                citizens,
                new TitleClaimStorage(LoggerFactory.getLogger("profile-test")),
                null,
                new ElarionEventBus());
        return new CitizenProfileService(citizens, realms, titles);
    }

    private static CitizenRecord citizen() {
        CitizenRecord citizen = new CitizenRecord(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "MaraAccount");
        citizen.setNickname("Mara");
        citizen.setRealmId("realm1");
        citizen.setActiveTitleId("citizen");
        citizen.setJoinedAt(Instant.parse("2026-01-01T00:00:00Z").toEpochMilli());
        citizen.setLastSeenAt(Instant.parse("2026-01-02T00:00:00Z").toEpochMilli());
        return citizen;
    }

    private static String value(CitizenProfileSection section, String id) {
        return section.fields().stream()
                .filter(field -> field.id().equals(id))
                .findFirst()
                .map(CitizenProfileField::value)
                .orElseThrow();
    }

    private static void assertVisible(CitizenProfileSection section, String id) {
        assertTrue(section.fields().stream().anyMatch(field -> field.id().equals(id)));
    }

    private static void assertHidden(CitizenProfileSection section, String id) {
        assertFalse(section.fields().stream().anyMatch(field -> field.id().equals(id)));
    }

    private static final class TestContributor implements panetina.elarion.core.model.profile.CitizenProfileContributor {
        @Override
        public String id() {
            return "addon";
        }

        @Override
        public List<CitizenProfileSection> sections(CitizenProfileRequestContext context, CitizenRecord target) {
            List<CitizenProfileField> fields = IntStream.range(0, CitizenProfileService.MAX_FIELDS_PER_SECTION + 8)
                    .mapToObj(index -> new CitizenProfileField(
                            "field-" + index,
                            "Field " + index,
                            "Value " + index,
                            ProfileVisibility.PUBLIC))
                    .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
            fields.add(new CitizenProfileField("admin-only", "Admin", "Hidden", ProfileVisibility.ADMIN));
            return List.of(new CitizenProfileSection(
                    "addon.section-0",
                    "Addon",
                    "addon",
                    ProfileVisibility.PUBLIC,
                    fields));
        }
    }
}
