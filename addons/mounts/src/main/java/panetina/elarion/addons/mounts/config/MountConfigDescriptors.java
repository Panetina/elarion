package panetina.elarion.addons.mounts.config;

import panetina.elarion.addons.mounts.entity.ElarionMountType;
import panetina.elarion.core.config.ElarionConfigCategory;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.config.ElarionConfigValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class MountConfigDescriptors {
    private MountConfigDescriptors() {
    }

    public static void register(
            ElarionConfigRegistry registry,
            Supplier<MountCollectionTextConfig> config
    ) {
        registry.registerDomain(domain(config));
    }

    public static ElarionConfigDomain domain(Supplier<MountCollectionTextConfig> config) {
        MountCollectionTextConfig defaults = MountCollectionTextConfig.defaults();
        return new ElarionConfigDomain(
                "mounts",
                "addons:mounts",
                "Mounts",
                "Mount Collection locked and unlocked presentation text.",
                List.of("config/elarion/addons/mounts/collection.yml"),
                "",
                List.of(new ElarionConfigCategory(
                        "collection-text",
                        "Collection Text",
                        "Loaded row and detail text for each registered mount type.",
                        collectionEntries(config, defaults))));
    }

    private static List<ElarionConfigEntry<?>> collectionEntries(
            Supplier<MountCollectionTextConfig> config,
            MountCollectionTextConfig defaults
    ) {
        List<ElarionConfigEntry<?>> entries = new ArrayList<>();
        entries.add(intEntry(
                "mounts.count",
                "Mount Count",
                "Number of registered mount types with Collection text.",
                "collection.yml.mounts",
                ElarionMountType.values().length,
                () -> ElarionMountType.values().length));
        entries.add(stringEntry(
                "mounts.ids",
                "Mount IDs",
                "Comma-separated stable mount type IDs.",
                "collection.yml.mounts",
                mountIds(),
                MountConfigDescriptors::mountIds));

        for (ElarionMountType type : ElarionMountType.values()) {
            MountCollectionTextConfig.Entry defaultEntry = defaults.entry(type);
            entries.add(textEntry(type, "locked-row", "Locked Row",
                    "Compact Collection row text while the mount is locked.",
                    defaultEntry.lockedRow(), config, MountCollectionTextConfig.Entry::lockedRow));
            entries.add(textEntry(type, "unlocked-row", "Unlocked Row",
                    "Compact Collection row text after the mount is unlocked.",
                    defaultEntry.unlockedRow(), config, MountCollectionTextConfig.Entry::unlockedRow));
            entries.add(textEntry(type, "locked-detail", "Locked Detail",
                    "Collection detail text while the mount is locked.",
                    defaultEntry.lockedDetail(), config, MountCollectionTextConfig.Entry::lockedDetail));
            entries.add(textEntry(type, "unlocked-detail", "Unlocked Detail",
                    "Collection detail text after the mount is unlocked.",
                    defaultEntry.unlockedDetail(), config, MountCollectionTextConfig.Entry::unlockedDetail));
        }
        return entries;
    }

    private static ElarionConfigEntry<String> textEntry(
            ElarionMountType type,
            String field,
            String label,
            String description,
            String defaultValue,
            Supplier<MountCollectionTextConfig> config,
            Function<MountCollectionTextConfig.Entry, String> value
    ) {
        String path = "collection.yml.mounts." + type.id() + "." + field;
        return stringEntry(
                "mounts." + type.id() + "." + field,
                type.label() + " " + label,
                description,
                path,
                defaultValue,
                () -> value.apply(current(config).entry(type)));
    }

    private static ElarionConfigEntry<Integer> intEntry(
            String id,
            String label,
            String description,
            String path,
            int defaultValue,
            Supplier<Integer> currentValue
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.INTEGER, defaultValue, currentValue,
                ElarionConfigValidator.integerMinimum(path, 0), List.of(), "0", "",
                false, true, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.STRING, defaultValue, currentValue,
                ElarionConfigValidator.nonBlank(path), List.of(), "", "",
                false, true, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static MountCollectionTextConfig current(Supplier<MountCollectionTextConfig> config) {
        MountCollectionTextConfig value = config == null ? null : config.get();
        return value == null ? MountCollectionTextConfig.defaults() : value;
    }

    private static String mountIds() {
        return Arrays.stream(ElarionMountType.values())
                .map(ElarionMountType::id)
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }
}
