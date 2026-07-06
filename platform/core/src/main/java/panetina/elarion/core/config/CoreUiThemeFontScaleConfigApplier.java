package panetina.elarion.core.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CoreUiThemeFontScaleConfigApplier {
    public static final ElarionConfigEditTarget TARGET = new ElarionConfigEditTarget(
            "core", "ui_theme", "defaults.font-scale-percent");
    public static final String AUDIT_EVENT_TYPE = "core.config.ui_theme.font_scale_changed";
    public static final String AFFECTED_FILE = "config/elarion/core/ui_theme.yml";

    private static final Pattern FONT_SCALE_LINE = Pattern.compile(
            "(?m)^([ \\t]*font-scale-percent:[ \\t]*)([0-9]+)([ \\t]*(?:#.*)?)$");
    private static final Pattern MINIMUM_SCALE_LINE = Pattern.compile(
            "(?m)^([ \\t]*)(minimum-scale-percent:[^\\r\\n]*)(\\r?\\n)");
    private static final Pattern DEFAULTS_LINE = Pattern.compile(
            "(?m)^(defaults:[ \\t]*(?:#.*)?)(\\r?\\n)");

    private CoreUiThemeFontScaleConfigApplier() {
    }

    public static void register(
            ElarionConfigApplyRegistrar registrar,
            CoreConfigManager config,
            Runnable syncUiThemes
    ) {
        register(registrar, config, config::load, syncUiThemes);
    }

    static void register(
            ElarionConfigApplyRegistrar registrar,
            CoreConfigManager config,
            Runnable reloadConfig,
            Runnable syncUiThemes
    ) {
        Objects.requireNonNull(registrar, "Config apply registrar is required");
        Objects.requireNonNull(config, "Core config manager is required");
        Objects.requireNonNull(reloadConfig, "Core config reload operation is required");
        Objects.requireNonNull(syncUiThemes, "UI theme sync operation is required");
        registrar.register(TARGET, ElarionConfigApplyCapability.runtimeReload(
                AUDIT_EVENT_TYPE, List.of(AFFECTED_FILE)),
                context -> prepare(config, reloadConfig, syncUiThemes, context));
    }

    private static ElarionConfigPreparedChange prepare(
            CoreConfigManager config,
            Runnable reloadConfig,
            Runnable syncUiThemes,
            ElarionConfigApplyContext context
    ) {
        ElarionConfigEditTarget target = new ElarionConfigEditTarget(
                context.request().domainId(), context.request().categoryId(), context.request().entryId());
        if (!TARGET.equals(target)) {
            throw new IllegalArgumentException("Unsupported Core UI theme config apply target: "
                    + target.targetKey());
        }

        String oldDisplayValue = context.entry().currentDisplayValue();
        String newDisplayValue = context.request().proposedValue().trim();
        Path file = config.coreConfigDir().resolve("ui_theme.yml");
        String oldContents = read(file);
        String newContents = replaceFontScale(oldContents, newDisplayValue);

        return ElarionConfigPreparedChange.of(
                () -> {
                    writeReplacing(file, newContents);
                    reloadAndSync(reloadConfig, syncUiThemes);
                    return ElarionConfigChangeResult.applied(
                            context.request(), oldDisplayValue, newDisplayValue,
                            true, false, AUDIT_EVENT_TYPE);
                },
                () -> {
                    writeReplacing(file, oldContents);
                    reloadAndSync(reloadConfig, syncUiThemes);
                });
    }

    private static String replaceFontScale(String contents, String newValue) {
        Matcher matcher = FONT_SCALE_LINE.matcher(contents);
        StringBuffer rewritten = new StringBuffer();
        int matches = 0;
        while (matcher.find()) {
            matches++;
            matcher.appendReplacement(rewritten,
                    Matcher.quoteReplacement(matcher.group(1) + newValue + matcher.group(3)));
        }
        matcher.appendTail(rewritten);
        if (matches == 0) {
            return insertMissingFontScale(contents, newValue);
        }
        if (matches > 1) {
            throw new IllegalStateException("ui_theme.yml must contain exactly one defaults.font-scale-percent line");
        }
        return rewritten.toString();
    }

    private static String insertMissingFontScale(String contents, String newValue) {
        Matcher minimum = MINIMUM_SCALE_LINE.matcher(contents);
        if (minimum.find()) {
            return minimum.replaceFirst(Matcher.quoteReplacement(
                    minimum.group(1) + minimum.group(2) + minimum.group(3)
                            + minimum.group(1) + "font-scale-percent: " + newValue + minimum.group(3)));
        }

        Matcher defaults = DEFAULTS_LINE.matcher(contents);
        if (defaults.find()) {
            String newline = defaults.group(2).isEmpty() ? System.lineSeparator() : defaults.group(2);
            return defaults.replaceFirst(Matcher.quoteReplacement(
                    defaults.group(1) + newline + "  font-scale-percent: " + newValue + newline));
        }

        throw new IllegalStateException("ui_theme.yml must contain a defaults block for defaults.font-scale-percent");
    }

    private static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to read " + file, exception);
        }
    }

    private static void writeReplacing(Path file, String contents) {
        Path temp = file.resolveSibling(file.getFileName() + ".admin-apply.tmp");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(temp, contents, StandardCharsets.UTF_8);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to write " + file, exception);
        } finally {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
            }
        }
    }

    private static void reloadAndSync(Runnable reloadConfig, Runnable syncUiThemes) {
        reloadConfig.run();
        syncUiThemes.run();
    }
}
