package panetina.elarion.addons.guilds.config;

import panetina.elarion.addons.guilds.model.GuildConfig;
import panetina.elarion.core.config.ElarionConfigCategory;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.config.ElarionConfigValidator;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

public final class GuildConfigDescriptors {
    private GuildConfigDescriptors() {
    }

    public static void register(ElarionConfigRegistry registry, Supplier<GuildConfig> config) {
        registry.registerDomain(domain(config));
    }

    public static ElarionConfigDomain domain(Supplier<GuildConfig> config) {
        GuildConfig defaults = GuildConfig.defaults();
        return new ElarionConfigDomain(
                "guilds",
                "addons:guilds",
                "Guilds",
                "Public player guild configuration.",
                List.of("config/elarion/addons/guilds/guilds.yml"),
                "/e guild reload",
                List.of(
                        new ElarionConfigCategory(
                                "general",
                                "General",
                                "Guild system enablement and creation costs.",
                                List.of(
                                        boolEntry("enabled", "Enabled",
                                                "Allows players to create and use public guilds.",
                                                "guilds.yml.enabled",
                                                defaults.enabled(), () -> config.get().enabled()),
                                        longEntry("creation.fee", "Creation Fee",
                                                "Physical carried currency charged when a player creates a guild.",
                                                "guilds.yml.creation.fee",
                                                defaults.creationFee(), () -> config.get().creationFee(), 0L))),
                        new ElarionConfigCategory(
                                "identity",
                                "Identity",
                                "Stable guild IDs and display-name rules.",
                                List.of(
                                        stringEntry("identity.id-pattern", "ID Pattern",
                                                "Regular expression for stable guild IDs.",
                                                "guilds.yml.identity.id-pattern",
                                                defaults.idPattern(), () -> config.get().idPattern()),
                                        intMinimumEntry("identity.max-name-length", "Max Name Length",
                                                "Maximum public guild display-name length.",
                                                "guilds.yml.identity.max-name-length",
                                                defaults.maxNameLength(), () -> config.get().maxNameLength(), 3))),
                        new ElarionConfigCategory(
                                "tags",
                                "Tags",
                                "Public guild tag rules.",
                                List.of(
                                        intMinimumEntry("tags.min-length", "Minimum Tag Length",
                                                "Minimum public guild tag length.",
                                                "guilds.yml.tags.min-length",
                                                defaults.minTagLength(), () -> config.get().minTagLength(), 1),
                                        intMinimumEntry("tags.max-length", "Maximum Tag Length",
                                                "Maximum public guild tag length.",
                                                "guilds.yml.tags.max-length",
                                                defaults.maxTagLength(), () -> config.get().maxTagLength(), 1),
                                        stringEntry("tags.pattern", "Tag Pattern",
                                                "Regular expression for public guild tags.",
                                                "guilds.yml.tags.pattern",
                                                defaults.tagPattern(), () -> config.get().tagPattern()),
                                        stringEntry("tags.blocked", "Blocked Tags",
                                                "Comma-separated display of reserved public tags.",
                                                "guilds.yml.tags.blocked",
                                                String.join(", ", defaults.blockedTags()),
                                                () -> String.join(", ", config.get().blockedTags()), false))),
                        new ElarionConfigCategory(
                                "invitations",
                                "Invitations",
                                "Guild invitation timing.",
                                List.of(longEntry("invitations.lifetime-days", "Invite Lifetime Days",
                                        "How many real-world days a guild invitation remains valid.",
                                        "guilds.yml.invitations.lifetime-days",
                                        Duration.ofMillis(defaults.inviteLifetimeMillis()).toDays(),
                                        () -> Duration.ofMillis(config.get().inviteLifetimeMillis()).toDays(),
                                        1L)))));
    }

    private static ElarionConfigEntry<Boolean> boolEntry(
            String id,
            String label,
            String description,
            String path,
            boolean defaultValue,
            Supplier<Boolean> currentValue
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.BOOLEAN, defaultValue, currentValue,
                ElarionConfigValidator.pass(), List.of("true", "false"), "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<Integer> intMinimumEntry(
            String id,
            String label,
            String description,
            String path,
            int defaultValue,
            Supplier<Integer> currentValue,
            int minimum
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.INTEGER, defaultValue, currentValue,
                ElarionConfigValidator.integerMinimum(path, minimum), List.of(),
                Integer.toString(minimum), "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<Long> longEntry(
            String id,
            String label,
            String description,
            String path,
            long defaultValue,
            Supplier<Long> currentValue,
            long minimum
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.LONG, defaultValue, currentValue,
                ElarionConfigValidator.longMinimum(path, minimum), List.of(),
                Long.toString(minimum), "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue
    ) {
        return stringEntry(id, label, description, path, defaultValue, currentValue, true);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue,
            boolean nonBlank
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.STRING, defaultValue, currentValue,
                nonBlank ? ElarionConfigValidator.nonBlank(path) : ElarionConfigValidator.pass(), List.of(), "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }
}
