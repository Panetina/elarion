package panetina.elarion.addons.realms.config;

import panetina.elarion.core.config.ElarionConfigCategory;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.config.ElarionConfigValidator;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class RealmConfigDescriptors {
    private RealmConfigDescriptors() {
    }

    public static void register(
            ElarionConfigRegistry registry,
            Supplier<RealmProtectionConfig> config
    ) {
        registry.registerDomain(domain(config));
    }

    public static ElarionConfigDomain domain(Supplier<RealmProtectionConfig> config) {
        RealmProtectionConfig defaults = RealmProtectionConfig.defaults();
        return new ElarionConfigDomain(
                "realms",
                "addons:realms",
                "Realm Protection",
                "Realm world protection and visitor interaction settings.",
                List.of("config/elarion/addons/realms/protection.yml"),
                "",
                List.of(
                        new ElarionConfigCategory(
                                "protection",
                                "Protection",
                                "Shared-world, operator, explosion, and feedback rules.",
                                List.of(
                                        stringEntry(
                                                "shared-world-ids",
                                                "Shared World IDs",
                                                "World IDs where shared-world PvP and block protections apply.",
                                                "protection.yml.shared-world-ids",
                                                joined(defaults.sharedWorldIds()),
                                                () -> joined(current(config).sharedWorldIds()),
                                                false),
                                        boolEntry(
                                                "operator-bypass",
                                                "Operator Bypass",
                                                "Allows permission-level 4 operators to bypass protection rules.",
                                                "protection.yml.operator-bypass",
                                                defaults.operatorBypass(),
                                                () -> current(config).operatorBypass()),
                                        boolEntry(
                                                "protect-explosion-blocks",
                                                "Protect Explosion Blocks",
                                                "Prevents explosion block damage in Realm-owned and shared worlds.",
                                                "protection.yml.protect-explosion-blocks",
                                                defaults.protectExplosionBlocks(),
                                                () -> current(config).protectExplosionBlocks()),
                                        longEntry(
                                                "feedback-cooldown-millis",
                                                "Feedback Cooldown",
                                                "Minimum milliseconds between protection denial messages per player.",
                                                "protection.yml.feedback-cooldown-millis",
                                                defaults.feedbackCooldownMillis(),
                                                () -> current(config).feedbackCooldownMillis(),
                                                0L))),
                        new ElarionConfigCategory(
                                "blocks",
                                "Block Overrides",
                                "Additional block IDs recognized by visitor interaction policy.",
                                List.of(
                                        stringEntry(
                                                "extra-ally-interactable-blocks",
                                                "Extra Ally Interactable Blocks",
                                                "Additional block IDs allies may use as mechanisms.",
                                                "protection.yml.extra-ally-interactable-blocks",
                                                joined(defaults.extraAllyInteractableBlocks()),
                                                () -> joined(current(config).extraAllyInteractableBlocks()),
                                                false),
                                        stringEntry(
                                                "extra-container-blocks",
                                                "Extra Container Blocks",
                                                "Additional block IDs treated as protected containers.",
                                                "protection.yml.extra-container-blocks",
                                                joined(defaults.extraContainerBlocks()),
                                                () -> joined(current(config).extraContainerBlocks()),
                                                false)))));
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
                false, true, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
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
                ElarionConfigValidator.longMinimum(path, minimum), List.of(), Long.toString(minimum), "",
                false, true, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
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
                nonBlank ? ElarionConfigValidator.nonBlank(path) : ElarionConfigValidator.pass(),
                List.of(), "", "", false, true,
                ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static RealmProtectionConfig current(Supplier<RealmProtectionConfig> config) {
        RealmProtectionConfig value = config == null ? null : config.get();
        return value == null ? RealmProtectionConfig.defaults() : value;
    }

    private static String joined(Set<String> values) {
        return values.stream().sorted().reduce((left, right) -> left + ", " + right).orElse("");
    }
}
