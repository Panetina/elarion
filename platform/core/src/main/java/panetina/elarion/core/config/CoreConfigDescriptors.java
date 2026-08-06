package panetina.elarion.core.config;

import panetina.elarion.core.model.ElarionUiTheme;
import panetina.elarion.core.model.HistoryRecordingPolicy;
import panetina.elarion.core.model.ProgressionRegion;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.RewardAction;
import panetina.elarion.core.model.ServerIdentityConfig;
import panetina.elarion.core.model.TitleAcquisitionMode;
import panetina.elarion.core.model.TitleActiveEffect;
import panetina.elarion.core.model.TitleDefinition;
import panetina.elarion.core.model.TitleOwnershipMode;
import panetina.elarion.core.model.TitleUnlockRule;
import panetina.elarion.core.model.VisibilityScope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class CoreConfigDescriptors {
    private CoreConfigDescriptors() {
    }

    public static void register(ElarionConfigRegistry registry, CoreConfigManager config) {
        registry.registerDomain(domain(config));
    }

    public static ElarionConfigDomain domain(CoreConfigManager config) {
        ElarionUiTheme uiDefaults = ElarionUiTheme.defaults();
        ServerIdentityConfig identityDefaults = ServerIdentityConfig.defaults();
        return new ElarionConfigDomain(
                "core",
                "platform:core",
                "Core",
                "Canonical Elarion platform configuration.",
                List.of(
                        "config/elarion/core/ui_theme.yml",
                        "config/elarion/core/server_identity.yml",
                        "config/elarion/core/realms.yml",
                        "config/elarion/core/titles.yml",
                        "config/elarion/core/title-progression.yml",
                        "config/elarion/core/rewards.yml",
                        "config/elarion/core/citizens-defaults.yml",
                        "config/elarion/core/activity.yml",
                        "config/elarion/core/chat.yml",
                        "config/elarion/core/identity.yml",
                        "config/elarion/core/history.yml"),
                "/e reload",
                List.of(
                        new ElarionConfigCategory(
                                "ui_theme",
                                "UI Theme",
                                "Shared custom Elarion UI sizing and typography values.",
                                List.of(
                                        intEntry("defaults.logical-width", "Logical Width",
                                                "Base custom UI layout width before Minecraft GUI scaling.",
                                                "ui_theme.yml.defaults.logical-width",
                                                uiDefaults.logicalWidth(), () -> config.uiTheme().logicalWidth(), 240, 960),
                                        intEntry("defaults.logical-height", "Logical Height",
                                                "Base custom UI layout height before Minecraft GUI scaling.",
                                                "ui_theme.yml.defaults.logical-height",
                                                uiDefaults.logicalHeight(), () -> config.uiTheme().logicalHeight(), 180, 720),
                                        intEntry("defaults.minimum-scale-percent", "Minimum UI Scale",
                                                "Smallest allowed custom UI scale before a screen shows its fallback.",
                                                "ui_theme.yml.defaults.minimum-scale-percent",
                                                uiDefaults.minimumScalePercent(),
                                                () -> config.uiTheme().minimumScalePercent(), 25, 100),
                                        intEntry("defaults.font-scale-percent", "Font Scale",
                                                "Server-wide Elarion custom UI text scale.",
                                                "ui_theme.yml.defaults.font-scale-percent",
                                                uiDefaults.fontScalePercent(),
                                                () -> config.uiTheme().fontScalePercent(), 100, 150),
                                        intEntry("defaults.row-height", "Row Height",
                                                "Default text-bearing row height for custom Elarion screens.",
                                                "ui_theme.yml.defaults.row-height",
                                                uiDefaults.rowHeight(), () -> config.uiTheme().rowHeight(), 10, 64),
                                        intEntry("defaults.button-height", "Button Height",
                                                "Default text-bearing button height for custom Elarion screens.",
                                                "ui_theme.yml.defaults.button-height",
                                                uiDefaults.buttonHeight(), () -> config.uiTheme().buttonHeight(), 10, 64))),
                        new ElarionConfigCategory(
                                "server_identity",
                                "Server Identity",
                                "Global player-facing names and terms.",
                                List.of(
                                        stringEntry("identity.server-name", "Server Name",
                                                "Public server name used in authored text.",
                                                "server_identity.yml.identity.server-name",
                                                identityDefaults.serverName(),
                                                () -> config.serverIdentity().serverName()),
                                        stringEntry("identity.capital-name", "Capital Name",
                                                "Public capital or hub name.",
                                                "server_identity.yml.identity.capital-name",
                                                identityDefaults.capitalName(),
                                                () -> config.serverIdentity().capitalName()),
                                        stringEntry("terms.realm-singular", "Realm Term",
                                                "Singular player-facing Realm term.",
                                                "server_identity.yml.terms.realm-singular",
                                                identityDefaults.realmSingular(),
                                                () -> config.serverIdentity().realmSingular()),
                                        stringEntry("terms.currency-singular", "Currency Singular",
                                                "Singular player-facing currency term.",
                                                "server_identity.yml.terms.currency-singular",
                                                identityDefaults.currencySingular(),
                                                () -> config.serverIdentity().currencySingular()),
                                        stringEntry("terms.currency-plural", "Currency Plural",
                                                "Plural player-facing currency term.",
                                                "server_identity.yml.terms.currency-plural",
                                                identityDefaults.currencyPlural(),
                                                () -> config.serverIdentity().currencyPlural()))),
                        realmsCategory(config),
                        titlesCategory(config),
                        titleProgressionCategory(config),
                        rewardsCategory(config),
                        citizenCategory(config),
                        chatCategory(config),
                        identityCategory(config),
                        historyCategory(config)));
    }

    private static ElarionConfigCategory realmsCategory(CoreConfigManager config) {
        Map<String, RealmDefinition> snapshot = config.realms();
        List<ElarionConfigEntry<?>> entries = new ArrayList<>();
        entries.add(intEntry("realms.count", "Realm Count",
                "Number of currently loaded Core Realm definitions.",
                "realms.yml.realms",
                snapshot.size(), () -> config.realms().size(), 0, Integer.MAX_VALUE));
        entries.add(stringEntry("realms.ids", "Realm IDs",
                "Comma-separated Realm IDs currently known to Core.",
                "realms.yml.realms",
                keys(snapshot), () -> keys(config.realms()), false));

        for (RealmDefinition realm : sortedRealms(snapshot)) {
            entries.add(realmStringEntry(realm, "display-name", "Display Name",
                    "Player-facing Realm name.",
                    config, RealmDefinition::displayName));
            entries.add(realmStringEntry(realm, "short-name", "Short Name",
                    "Short Realm label used in compact UI and chat contexts.",
                    config, RealmDefinition::shortName));
            entries.add(realmStringEntry(realm, "prefix", "Prefix",
                    "Realm prefix used by identity and chat presentation.",
                    config, RealmDefinition::prefix, false));
            entries.add(realmStringEntry(realm, "color", "Color",
                    "Configured Minecraft formatting color name for this Realm.",
                    config, RealmDefinition::color));
            entries.add(realmStringEntry(realm, "visibility-scope", "Visibility Scope",
                    "Default visibility scope for Realm presentation.",
                    config, value -> value.visibilityScope().name(), visibilityChoices()));
            entries.add(realmStringEntry(realm, "spawn.world", "Spawn World",
                    "Minecraft world registry ID used for this Realm's spawn.",
                    config, value -> value.spawn().worldId()));
            entries.add(realmStringEntry(realm, "spawn.x", "Spawn X",
                    "Realm spawn X coordinate.",
                    config, value -> decimal(value.spawn().x())));
            entries.add(realmStringEntry(realm, "spawn.y", "Spawn Y",
                    "Realm spawn Y coordinate.",
                    config, value -> decimal(value.spawn().y())));
            entries.add(realmStringEntry(realm, "spawn.z", "Spawn Z",
                    "Realm spawn Z coordinate.",
                    config, value -> decimal(value.spawn().z())));
            entries.add(realmStringEntry(realm, "spawn.yaw", "Spawn Yaw",
                    "Realm spawn yaw rotation.",
                    config, value -> decimal(value.spawn().yaw())));
            entries.add(realmStringEntry(realm, "spawn.pitch", "Spawn Pitch",
                    "Realm spawn pitch rotation.",
                    config, value -> decimal(value.spawn().pitch())));
            entries.add(realmStringEntry(realm, "flags", "Flags",
                    "Comma-separated optional Realm definition flags.",
                    config, value -> joined(value.flags()), false));
        }

        return new ElarionConfigCategory(
                "realms",
                "Realm Definitions",
                "Core-owned Realm presentation, visibility, spawn, and flag definitions.",
                entries);
    }

    private static ElarionConfigCategory titlesCategory(CoreConfigManager config) {
        Map<String, TitleDefinition> snapshot = config.titles();
        List<ElarionConfigEntry<?>> entries = new ArrayList<>();
        entries.add(intEntry("titles.count", "Title Count",
                "Number of currently loaded Core title definitions.",
                "titles.yml.titles",
                snapshot.size(), () -> config.titles().size(), 0, Integer.MAX_VALUE));
        entries.add(stringEntry("titles.ids", "Title IDs",
                "Comma-separated title IDs currently known to Core.",
                "titles.yml.titles",
                keys(snapshot), () -> keys(config.titles()), false));

        for (TitleDefinition title : sortedTitles(snapshot)) {
            entries.add(titleStringEntry(title, "display-name", "Display Name",
                    "Player-facing title name.",
                    config, TitleDefinition::displayName));
            entries.add(titleStringEntry(title, "description", "Description",
                    "Player-facing title description.",
                    config, TitleDefinition::description, false));
            entries.add(titleStringEntry(title, "prefix", "Prefix",
                    "Prefix added by this title when configured.",
                    config, TitleDefinition::prefix, false));
            entries.add(titleStringEntry(title, "suffix", "Suffix",
                    "Suffix added by this title when configured.",
                    config, TitleDefinition::suffix, false));
            entries.add(titleStringEntry(title, "color", "Title Color",
                    "Hex #RRGGBB color used for this title in identity and Ember Ledger presentation.",
                    config, value -> String.format("#%06X", value.colorArgb() & 0x00FFFFFF)));
            entries.add(titleIntEntry(title, "priority", "Priority",
                    "Display priority used when several titles are available.",
                    config, TitleDefinition::priority, Integer.MIN_VALUE, Integer.MAX_VALUE));
            entries.add(titleBoolEntry(title, "visible-under-username", "Visible Under Username",
                    "Whether this title may be rendered under the player's name.",
                    config, TitleDefinition::visibleUnderUsername));
            entries.add(titleStringEntry(title, "acquisition-mode", "Acquisition Mode",
                    "How this title is acquired.",
                    config, value -> value.acquisitionMode().name(), acquisitionModeChoices()));
            entries.add(titleStringEntry(title, "ownership-mode", "Ownership Mode",
                    "How ownership of this title is constrained.",
                    config, value -> value.ownershipMode().name(), ownershipModeChoices()));
            entries.add(titleBoolEntry(title, "hidden-from-discovery", "Hidden From Discovery",
                    "Whether discovery views should hide this title until earned.",
                    config, TitleDefinition::hiddenFromDiscovery));
            entries.add(titleStringEntry(title, "abilities", "Abilities",
                    "Comma-separated ability IDs granted by this title.",
                    config, value -> joined(value.abilities()), false));
            entries.add(titleIntEntry(title, "active-effects.count", "Active Effect Count",
                    "Number of active effect definitions attached to this title.",
                    config, value -> value.activeEffects().size(), 0, Integer.MAX_VALUE));
            entries.add(titleStringEntry(title, "active-effects.summary", "Active Effects",
                    "Summary of active effect types and parameters.",
                    config, CoreConfigDescriptors::activeEffectsSummary, false));
        }

        return new ElarionConfigCategory(
                "titles",
                "Title Definitions",
                "Core-owned title presentation, acquisition, ownership, abilities, and effect summaries.",
                entries);
    }

    private static ElarionConfigCategory titleProgressionCategory(CoreConfigManager config) {
        Map<String, ProgressionRegion> regionSnapshot = config.progressionRegions();
        Map<String, TitleUnlockRule> ruleSnapshot = config.titleUnlockRules();
        List<ElarionConfigEntry<?>> entries = new ArrayList<>();
        entries.add(intEntry("regions.count", "Region Count",
                "Number of currently loaded title progression regions.",
                "title-progression.yml.regions",
                regionSnapshot.size(), () -> config.progressionRegions().size(), 0, Integer.MAX_VALUE));
        entries.add(stringEntry("regions.ids", "Region IDs",
                "Comma-separated title progression region IDs currently known to Core.",
                "title-progression.yml.regions",
                keys(regionSnapshot), () -> keys(config.progressionRegions()), false));
        for (ProgressionRegion region : sortedRegions(regionSnapshot)) {
            entries.add(regionStringEntry(region, "world", "World",
                    "World registry ID for this progression region.",
                    config, ProgressionRegion::world));
            entries.add(regionStringEntry(region, "min-x", "Minimum X",
                    "Minimum X coordinate for this progression region.",
                    config, value -> decimal(value.minX())));
            entries.add(regionStringEntry(region, "min-y", "Minimum Y",
                    "Minimum Y coordinate for this progression region.",
                    config, value -> decimal(value.minY())));
            entries.add(regionStringEntry(region, "min-z", "Minimum Z",
                    "Minimum Z coordinate for this progression region.",
                    config, value -> decimal(value.minZ())));
            entries.add(regionStringEntry(region, "max-x", "Maximum X",
                    "Maximum X coordinate for this progression region.",
                    config, value -> decimal(value.maxX())));
            entries.add(regionStringEntry(region, "max-y", "Maximum Y",
                    "Maximum Y coordinate for this progression region.",
                    config, value -> decimal(value.maxY())));
            entries.add(regionStringEntry(region, "max-z", "Maximum Z",
                    "Maximum Z coordinate for this progression region.",
                    config, value -> decimal(value.maxZ())));
        }

        entries.add(intEntry("rules.count", "Rule Count",
                "Number of currently loaded title unlock rules.",
                "title-progression.yml.rules",
                ruleSnapshot.size(), () -> config.titleUnlockRules().size(), 0, Integer.MAX_VALUE));
        entries.add(stringEntry("rules.ids", "Rule IDs",
                "Comma-separated title unlock rule IDs currently known to Core.",
                "title-progression.yml.rules",
                keys(ruleSnapshot), () -> keys(config.titleUnlockRules()), false));
        for (TitleUnlockRule rule : sortedRules(ruleSnapshot)) {
            entries.add(ruleStringEntry(rule, "title", "Title",
                    "Title ID granted by this unlock rule.",
                    config, TitleUnlockRule::titleId));
            entries.add(ruleStringEntry(rule, "trigger", "Trigger",
                    "Progression event trigger type.",
                    config, TitleUnlockRule::trigger));
            entries.add(ruleStringEntry(rule, "stat-key", "Stat Key",
                    "Stat key used by stat-threshold rules.",
                    config, TitleUnlockRule::statKey, false));
            entries.add(ruleLongEntry(rule, "threshold", "Threshold",
                    "Required threshold for this unlock rule.",
                    config, TitleUnlockRule::threshold, 0));
            entries.add(ruleLongEntry(rule, "amount", "Amount",
                    "Amount credited per matching event.",
                    config, TitleUnlockRule::amount, 1));
            if (rule.metric() != null) {
                entries.add(ruleStringEntry(rule, "metric.id", "Metric ID",
                        "Core metric ID evaluated by this title rule.", config,
                        value -> value.metric() == null ? "" : value.metric().metricId().toString(), false));
                entries.add(ruleStringEntry(rule, "metric.scope", "Metric Scope",
                        "Metric scope: global, realm, realm:<id>, or event:<id>.", config,
                        value -> value.metric() == null ? "" : metricScope(value.metric()), false));
                entries.add(ruleStringEntry(rule, "metric.comparator", "Metric Comparator",
                        "Metric comparison operator: gte, lte, or eq.", config,
                        value -> value.metric() == null ? "" : value.metric().comparator().name()
                                .toLowerCase(java.util.Locale.ROOT), false));
                entries.add(ruleLongEntry(rule, "metric.threshold", "Metric Threshold",
                        "Fixed-point metric threshold required by this title rule.", config,
                        value -> value.metric() == null ? 0L : value.metric().threshold(), Long.MIN_VALUE));
                entries.add(ruleStringEntry(rule, "metric.dimensions", "Metric Dimensions",
                        "Bounded materialized dimension filters as key=identifier pairs.", config,
                        value -> value.metric() == null ? "" : value.metric().dimensions().entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .map(entry -> entry.getKey() + "=" + entry.getValue())
                                .collect(java.util.stream.Collectors.joining(",")), false));
            }
            entries.add(ruleStringEntry(rule, "entities", "Entities",
                    "Entity registry IDs or tags matched by this rule.",
                    config, value -> matchers(value.entities()), false));
            entries.add(ruleStringEntry(rule, "blocks", "Blocks",
                    "Block registry IDs or tags matched by this rule.",
                    config, value -> matchers(value.blocks()), false));
            entries.add(ruleStringEntry(rule, "items", "Items",
                    "Item registry IDs or tags matched by this rule.",
                    config, value -> matchers(value.items()), false));
            entries.add(ruleStringEntry(rule, "recipes", "Recipes",
                    "Recipe registry IDs or tags matched by this rule.",
                    config, value -> matchers(value.recipes()), false));
            entries.add(ruleStringEntry(rule, "worlds", "Worlds",
                    "World IDs matched by this rule.",
                    config, value -> joined(value.worlds()), false));
            entries.add(ruleStringEntry(rule, "dimensions", "Dimensions",
                    "Dimension IDs matched by this rule.",
                    config, value -> joined(value.dimensions()), false));
            entries.add(ruleStringEntry(rule, "biomes", "Biomes",
                    "Biome IDs matched by this rule.",
                    config, value -> joined(value.biomes()), false));
            entries.add(ruleStringEntry(rule, "regions", "Regions",
                    "Progression region IDs matched by this rule.",
                    config, value -> joined(value.regions()), false));
            entries.add(ruleStringEntry(rule, "metadata", "Metadata",
                    "Required metadata key/value pairs.",
                    config, value -> keyValues(value.metadata()), false));
            entries.add(ruleStringEntry(rule, "continuous", "Continuous Rule",
                    "Continuous-rule sampling and requirement summary.",
                    config, CoreConfigDescriptors::continuousSummary, false));
        }

        return new ElarionConfigCategory(
                "title_progression",
                "Title Progression",
                "Core-owned title progression regions and unlock-rule summaries.",
                entries);
    }

    private static ElarionConfigCategory rewardsCategory(CoreConfigManager config) {
        Map<String, List<RewardAction>> snapshot = config.rewards();
        List<ElarionConfigEntry<?>> entries = new ArrayList<>();
        entries.add(intEntry("rewards.count", "Reward Count",
                "Number of currently loaded Core reward definitions.",
                "rewards.yml.rewards",
                snapshot.size(), () -> config.rewards().size(), 0, Integer.MAX_VALUE));
        entries.add(stringEntry("rewards.ids", "Reward IDs",
                "Comma-separated reward IDs currently known to Core.",
                "rewards.yml.rewards",
                keys(snapshot), () -> keys(config.rewards()), false));

        for (Map.Entry<String, List<RewardAction>> entry : sortedRewardEntries(snapshot)) {
            String rewardId = entry.getKey();
            List<RewardAction> actions = entry.getValue();
            entries.add(rewardIntEntry(rewardId, "actions.count", "Action Count",
                    "Number of actions in this reward definition.",
                    config, value -> value.size(), actions.size(), 0, Integer.MAX_VALUE));
            entries.add(rewardStringEntry(rewardId, "actions.types", "Action Types",
                    "Ordered action types in this reward definition.",
                    config, CoreConfigDescriptors::rewardActionTypes, rewardActionTypes(actions), false));
            for (int index = 0; index < actions.size(); index++) {
                RewardAction action = actions.get(index);
                String field = "actions." + index;
                int actionIndex = index;
                entries.add(rewardActionStringEntry(rewardId, field + ".type", "Action " + index + " Type",
                        "Reward action type.",
                        config, actionIndex, RewardAction::type, action.type()));
                entries.add(rewardActionStringEntry(rewardId, field + ".parameters",
                        "Action " + index + " Parameters",
                        "Reward action parameters.",
                        config, actionIndex, value -> keyValues(value.parameters()),
                        keyValues(action.parameters()), false));
            }
        }

        return new ElarionConfigCategory(
                "rewards",
                "Reward Definitions",
                "Core-owned reward action definitions.",
                entries);
    }

    private static ElarionConfigCategory citizenCategory(CoreConfigManager config) {
        return new ElarionConfigCategory(
                "citizens",
                "Embers",
                "Ember defaults and activity-window settings.",
                List.of(
                        stringEntry("defaults.title", "Default Title",
                                "Title ID assigned by default to Embers.",
                                "citizens-defaults.yml.defaults.title",
                                "citizen", config::defaultTitleId),
                        intEntry("activity.inactivity-days", "Inactivity Days",
                                "Days an offline Ember remains active for population and eligibility checks.",
                                "activity.yml.citizens.inactivity-days",
                                14, config::citizenInactivityDays, 1, Integer.MAX_VALUE)));
    }

    private static ElarionConfigCategory chatCategory(CoreConfigManager config) {
        return new ElarionConfigCategory(
                "chat",
                "Chat",
                "Local, whisper, yell, Realm, alliance, and notice presentation settings.",
                List.of(
                        boolEntry("local.enabled", "Local Chat Enabled",
                                "Enables local-radius chat.",
                                "chat.yml.local-chat.enabled", true, config::localChatEnabled),
                        intEntry("local.radius", "Local Chat Radius",
                                "Local chat radius in blocks.",
                                "chat.yml.local-chat.radius", 64, config::localChatRadius, 1, Integer.MAX_VALUE),
                        boolEntry("local.same-world-only", "Local Same World Only",
                                "Restricts local chat recipients to the sender's world.",
                                "chat.yml.local-chat.same-world-only", true, config::localChatSameWorldOnly),
                        boolEntry("local.admin-spy", "Local Admin Spy",
                                "Allows the OP-only local chat spy toggle.",
                                "chat.yml.local-chat.admin-spy", true, config::localChatAdminSpy),
                        stringEntry("local.format", "Local Chat Format",
                                "Formatting template for local chat messages.",
                                "chat.yml.local-chat.format", "[%local_chat%] %player% » %message%",
                                config::localChatFormat),
                        intEntry("whisper.radius", "Whisper Radius",
                                "Whisper chat radius in blocks.",
                                "chat.yml.whisper-chat.radius", 4, config::whisperChatRadius, 1, Integer.MAX_VALUE),
                        stringEntry("whisper.format", "Whisper Format",
                                "Formatting template for whisper chat messages.",
                                "chat.yml.whisper-chat.format",
                                "[%local_chat%] %player% whispers: %message%", config::whisperChatFormat),
                        intEntry("yell.radius", "Yell Radius",
                                "Yell chat radius in blocks.",
                                "chat.yml.yell-chat.radius", 128, config::yellChatRadius, 1, Integer.MAX_VALUE),
                        intEntry("yell.cooldown-seconds", "Yell Cooldown",
                                "Cooldown between yell messages in seconds.",
                                "chat.yml.yell-chat.cooldown-seconds", 300,
                                config::yellChatCooldownSeconds, 0, Integer.MAX_VALUE),
                        stringEntry("yell.format", "Yell Format",
                                "Formatting template for yell chat messages.",
                                "chat.yml.yell-chat.format",
                                "[%local_chat%] %player% yells: %message%", config::yellChatFormat),
                        stringEntry("realm.format", "Realm Chat Format",
                                "Formatting template for Realm chat messages.",
                                "chat.yml.realm-chat.format",
                                "[%realm_chat%] %player% » %message%", config::realmChatFormat),
                        stringEntry("alliance.format", "Alliance Chat Format",
                                "Formatting template for alliance chat messages.",
                                "chat.yml.alliance-chat.format",
                                "[%alliance_chat%:%realm_short%] %player% » %message%",
                                config::allianceChatFormat),
                        boolEntry("notices.scoped-join-leave", "Scoped Join/Leave Notices",
                                "Scopes Ember join/leave notices instead of broadcasting globally.",
                                "chat.yml.notices.scoped-join-leave", true, config::scopedJoinLeaveNotices),
                        stringEntry("notices.realm-format", "Realm Notice Format",
                                "Formatting template for Realm-scoped notices.",
                                "chat.yml.notices.realm-format",
                                "%player% joined your %realm_term%.", config::realmNoticeFormat),
                        stringEntry("notices.admin-format", "Admin Notice Format",
                                "Formatting template for administrator notices.",
                                "chat.yml.notices.admin-format",
                                "%player% joined %realm_term_lower% %realm%.", config::adminNoticeFormat)));
    }

    private static ElarionConfigCategory identityCategory(CoreConfigManager config) {
        return new ElarionConfigCategory(
                "identity",
                "Identity",
                "Nickname enablement, uniqueness, reservations, and protected-name policy.",
                List.of(
                        boolEntry("nickname.enabled", "Nicknames Enabled",
                                "Allows Embers to use Core nicknames.",
                                "identity.yml.nickname.enabled", true, config::nicknamesEnabled),
                        intEntry("nickname.max-length", "Nickname Maximum Length",
                                "Maximum nickname length.",
                                "identity.yml.nickname.max-length", 32,
                                config::nicknameMaxLength, 1, Integer.MAX_VALUE),
                        boolEntry("policy.unique", "Unique Nicknames",
                                "Requires normalized nicknames to be unique.",
                                "identity.yml.nickname-policy.unique", true, config::nicknameUnique),
                        boolEntry("policy.reserve-player-usernames", "Reserve Player Usernames",
                                "Prevents nicknames from claiming known player usernames.",
                                "identity.yml.nickname-policy.reserve-player-usernames", true,
                                config::nicknameReservePlayerUsernames),
                        stringEntry("policy.reserved-names", "Reserved Names",
                                "Comma-separated protected nickname values.",
                                "identity.yml.nickname-policy.reserved-names",
                                "admin, administrator, console, moderator, operator, server, system, %server_lower%",
                                () -> joined(config.nicknameReservedNames()), false),
                        boolEntry("protection.enabled", "Nickname Protection",
                                "Enables protected-name rejection.",
                                "identity.yml.nickname-protection.enabled", true,
                                config::nicknameProtectionEnabled),
                        boolEntry("protection.protect-realm-presentation", "Protect Realm Presentation",
                                "Protects configured Realm names and presentation terms from nickname reuse.",
                                "identity.yml.nickname-protection.protect-realm-presentation", true,
                                config::nicknameProtectRealmPresentation),
                        boolEntry("protection.protect-title-presentation", "Protect Title Presentation",
                                "Protects configured title names and presentation terms from nickname reuse.",
                                "identity.yml.nickname-protection.protect-title-presentation", true,
                                config::nicknameProtectTitlePresentation),
                        boolEntry("protection.reject-containing-protected-name", "Reject Containing Name",
                                "Rejects nicknames containing a protected name as a substring.",
                                "identity.yml.nickname-protection.reject-containing-protected-name", false,
                                config::nicknameRejectContainingProtectedName)));
    }

    private static ElarionConfigCategory historyCategory(CoreConfigManager config) {
        HistoryRecordingPolicy defaults = new HistoryRecordingPolicy(
                true, true, java.util.Set.of(), java.util.Set.of("chat"),
                true, java.util.Set.of(), java.util.Set.of());
        return new ElarionConfigCategory(
                "history",
                "History",
                "Recording filters, bounded live queries, archives, and public query limits.",
                List.of(
                        boolEntry("recording.enabled", "Recording Enabled",
                                "Enables structured Core history recording.",
                                "history.yml.recording.enabled", defaults.enabled(),
                                () -> config.historyRecordingPolicy().enabled()),
                        boolEntry("recording.default-category-enabled", "Default Category Enabled",
                                "Records categories unless explicitly disabled.",
                                "history.yml.recording.default-category-enabled",
                                defaults.defaultCategoryEnabled(),
                                () -> config.historyRecordingPolicy().defaultCategoryEnabled()),
                        stringEntry("recording.enabled-categories", "Enabled Categories",
                                "Categories explicitly enabled for recording.",
                                "history.yml.recording.enabled-categories", "",
                                () -> joined(config.historyRecordingPolicy().enabledCategories()), false),
                        stringEntry("recording.disabled-categories", "Disabled Categories",
                                "Categories explicitly excluded from recording.",
                                "history.yml.recording.disabled-categories", "chat",
                                () -> joined(config.historyRecordingPolicy().disabledCategories()), false),
                        boolEntry("recording.default-type-enabled", "Default Type Enabled",
                                "Records event types unless explicitly disabled.",
                                "history.yml.recording.default-type-enabled", defaults.defaultTypeEnabled(),
                                () -> config.historyRecordingPolicy().defaultTypeEnabled()),
                        stringEntry("recording.enabled-types", "Enabled Types",
                                "Event types explicitly enabled for recording.",
                                "history.yml.recording.enabled-types", "",
                                () -> joined(config.historyRecordingPolicy().enabledTypes()), false),
                        stringEntry("recording.disabled-types", "Disabled Types",
                                "Event types explicitly excluded from recording.",
                                "history.yml.recording.disabled-types", "",
                                () -> joined(config.historyRecordingPolicy().disabledTypes()), false),
                        intEntry("query.max-months-scanned", "Maximum Months Scanned",
                                "Maximum newest monthly history files scanned by live queries.",
                                "history.yml.query.max-months-scanned", 3,
                                config::historyQueryMaxMonths, 1, Integer.MAX_VALUE),
                        intEntry("query.command-limit-max", "Command Limit Maximum",
                                "Maximum history rows accepted by command queries.",
                                "history.yml.query.command-limit-max", 100,
                                config::historyCommandLimitMax, 1, 500),
                        boolEntry("archive.enabled", "Archive Enabled",
                                "Enables completed-week history archive summaries.",
                                "history.yml.archive.enabled", true, config::historyArchiveEnabled),
                        intEntry("archive.max-completed-weeks-per-generation", "Archive Weeks",
                                "Maximum completed archive weeks retained per generation.",
                                "history.yml.archive.max-completed-weeks-per-generation", 8,
                                config::historyArchiveMaxCompletedWeeks, 1, Integer.MAX_VALUE),
                        stringEntry("archive.chronicle-categories", "Chronicle Categories",
                                "Categories eligible for Chronicle books, library reads, and website projections.",
                                "history.yml.archive.chronicle-categories",
                                "administration, diplomacy, leadership, npc, realm, realm-decision, reward, security, title, world",
                                () -> joined(config.historyChronicleCategories()), false),
                        boolEntry("archive.default-chronicle-type-enabled", "Default Chronicle Type Enabled",
                                "Allows eligible event types unless they are excluded below.",
                                "history.yml.archive.default-chronicle-type-enabled", true,
                                () -> config.historyChroniclePolicy().defaultTypeEnabled()),
                        stringEntry("archive.enabled-chronicle-types", "Enabled Chronicle Types",
                                "Allow-list types when the default is disabled; use type or category:type.",
                                "history.yml.archive.enabled-chronicle-types", "",
                                () -> joined(config.historyChroniclePolicy().enabledTypes()), false),
                        stringEntry("archive.disabled-chronicle-types", "Disabled Chronicle Types",
                                "Deny-list noisy Chronicle types; audit recording remains unchanged.",
                                "history.yml.archive.disabled-chronicle-types", "",
                                () -> joined(config.historyChroniclePolicy().disabledTypes()), false),
                        intEntry("public-query.default-weeks", "Public Default Weeks",
                                "Default public-history window in weeks.",
                                "history.yml.public-query.default-weeks", 8,
                                config::publicHistoryDefaultWeeks, 1, 52),
                        intEntry("public-query.max-weeks", "Public Maximum Weeks",
                                "Hard maximum public-history window accepted by a query.",
                                "history.yml.public-query.max-weeks", 52,
                                config::publicHistoryMaxWeeks, 1, 52),
                        intEntry("public-query.default-limit", "Public Default Limit",
                                "Default maximum public-history rows returned.",
                                "history.yml.public-query.default-limit", 50,
                                config::publicHistoryDefaultLimit, 1, 1000),
                        intEntry("public-query.max-limit", "Public Maximum Limit",
                                "Maximum public-history rows accepted by a query.",
                                "history.yml.public-query.max-limit", 200,
                                config::publicHistoryMaxLimit, 1, 1000)));
    }

    private static ElarionConfigEntry<Integer> intEntry(
            String id,
            String label,
            String description,
            String path,
            int defaultValue,
            Supplier<Integer> currentValue,
            int minimum,
            int maximum
    ) {
        return new ElarionConfigEntry<>(
                id,
                label,
                description,
                path,
                ElarionConfigCodec.INTEGER,
                defaultValue,
                currentValue,
                ElarionConfigValidator.integerRange(path, minimum, maximum),
                List.of(),
                Integer.toString(minimum),
                Integer.toString(maximum),
                true,
                false,
                ElarionConfigPermission.OPERATOR,
                ElarionConfigPermission.OPERATOR);
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
                id,
                label,
                description,
                path,
                ElarionConfigCodec.STRING,
                defaultValue,
                currentValue,
                nonBlank ? ElarionConfigValidator.nonBlank(path) : ElarionConfigValidator.pass(),
                List.of(),
                "",
                "",
                true,
                false,
                ElarionConfigPermission.OPERATOR,
                ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> realmStringEntry(
            RealmDefinition realm,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<RealmDefinition, String> value
    ) {
        return realmStringEntry(realm, field, label, description, config, value, List.of(), true);
    }

    private static ElarionConfigEntry<String> realmStringEntry(
            RealmDefinition realm,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<RealmDefinition, String> value,
            boolean nonBlank
    ) {
        return realmStringEntry(realm, field, label, description, config, value, List.of(), nonBlank);
    }

    private static ElarionConfigEntry<String> realmStringEntry(
            RealmDefinition realm,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<RealmDefinition, String> value,
            List<String> choices
    ) {
        return realmStringEntry(realm, field, label, description, config, value, choices, true);
    }

    private static ElarionConfigEntry<String> realmStringEntry(
            RealmDefinition realm,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<RealmDefinition, String> value,
            List<String> choices,
            boolean nonBlank
    ) {
        return new ElarionConfigEntry<>(
                realmId(realm, field),
                realmLabel(realm, label),
                description,
                realmPath(realm, field),
                ElarionConfigCodec.STRING,
                value.apply(realm),
                () -> value.apply(currentRealm(config, realm)),
                nonBlank ? ElarionConfigValidator.nonBlank(realmPath(realm, field)) : ElarionConfigValidator.pass(),
                choices,
                "",
                "",
                true,
                false,
                ElarionConfigPermission.OPERATOR,
                ElarionConfigPermission.OPERATOR);
    }

    private static RealmDefinition currentRealm(CoreConfigManager config, RealmDefinition fallback) {
        RealmDefinition current = config.realms().get(fallback.id());
        return current == null ? fallback : current;
    }

    private static List<RealmDefinition> sortedRealms(Map<String, RealmDefinition> realms) {
        return realms.values().stream()
                .sorted(Comparator.comparing(RealmDefinition::id))
                .toList();
    }

    private static String realmId(RealmDefinition realm, String field) {
        return "realms." + realm.id() + "." + field;
    }

    private static String realmPath(RealmDefinition realm, String field) {
        return "realms.yml.realms." + realm.id() + "." + field;
    }

    private static String realmLabel(RealmDefinition realm, String fieldLabel) {
        return realm.id() + " " + fieldLabel;
    }

    private static List<String> visibilityChoices() {
        List<String> choices = new ArrayList<>();
        for (VisibilityScope scope : VisibilityScope.values()) {
            choices.add(scope.name());
        }
        return choices;
    }

    private static ElarionConfigEntry<Boolean> titleBoolEntry(
            TitleDefinition title,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<TitleDefinition, Boolean> value
    ) {
        return boolEntry(titleId(title, field), titleLabel(title, label), description,
                titlePath(title, field), value.apply(title), () -> value.apply(currentTitle(config, title)));
    }

    private static ElarionConfigEntry<Integer> titleIntEntry(
            TitleDefinition title,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<TitleDefinition, Integer> value,
            int minimum,
            int maximum
    ) {
        return intEntry(titleId(title, field), titleLabel(title, label), description,
                titlePath(title, field), value.apply(title), () -> value.apply(currentTitle(config, title)),
                minimum, maximum);
    }

    private static ElarionConfigEntry<String> titleStringEntry(
            TitleDefinition title,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<TitleDefinition, String> value
    ) {
        return titleStringEntry(title, field, label, description, config, value, List.of(), true);
    }

    private static ElarionConfigEntry<String> titleStringEntry(
            TitleDefinition title,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<TitleDefinition, String> value,
            boolean nonBlank
    ) {
        return titleStringEntry(title, field, label, description, config, value, List.of(), nonBlank);
    }

    private static ElarionConfigEntry<String> titleStringEntry(
            TitleDefinition title,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<TitleDefinition, String> value,
            List<String> choices
    ) {
        return titleStringEntry(title, field, label, description, config, value, choices, true);
    }

    private static ElarionConfigEntry<String> titleStringEntry(
            TitleDefinition title,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<TitleDefinition, String> value,
            List<String> choices,
            boolean nonBlank
    ) {
        return new ElarionConfigEntry<>(
                titleId(title, field),
                titleLabel(title, label),
                description,
                titlePath(title, field),
                ElarionConfigCodec.STRING,
                value.apply(title),
                () -> value.apply(currentTitle(config, title)),
                nonBlank ? ElarionConfigValidator.nonBlank(titlePath(title, field)) : ElarionConfigValidator.pass(),
                choices,
                "",
                "",
                true,
                false,
                ElarionConfigPermission.OPERATOR,
                ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> regionStringEntry(
            ProgressionRegion region,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<ProgressionRegion, String> value
    ) {
        return new ElarionConfigEntry<>(
                regionId(region, field),
                regionLabel(region, label),
                description,
                regionPath(region, field),
                ElarionConfigCodec.STRING,
                value.apply(region),
                () -> value.apply(currentRegion(config, region)),
                ElarionConfigValidator.nonBlank(regionPath(region, field)),
                List.of(),
                "",
                "",
                true,
                false,
                ElarionConfigPermission.OPERATOR,
                ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<Long> ruleLongEntry(
            TitleUnlockRule rule,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<TitleUnlockRule, Long> value,
            long minimum
    ) {
        return new ElarionConfigEntry<>(
                ruleId(rule, field),
                ruleLabel(rule, label),
                description,
                rulePath(rule, field),
                ElarionConfigCodec.LONG,
                value.apply(rule),
                () -> value.apply(currentRule(config, rule)),
                ElarionConfigValidator.longMinimum(rulePath(rule, field), minimum),
                List.of(),
                Long.toString(minimum),
                "",
                true,
                false,
                ElarionConfigPermission.OPERATOR,
                ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> ruleStringEntry(
            TitleUnlockRule rule,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<TitleUnlockRule, String> value
    ) {
        return ruleStringEntry(rule, field, label, description, config, value, true);
    }

    private static ElarionConfigEntry<String> ruleStringEntry(
            TitleUnlockRule rule,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<TitleUnlockRule, String> value,
            boolean nonBlank
    ) {
        return new ElarionConfigEntry<>(
                ruleId(rule, field),
                ruleLabel(rule, label),
                description,
                rulePath(rule, field),
                ElarionConfigCodec.STRING,
                value.apply(rule),
                () -> value.apply(currentRule(config, rule)),
                nonBlank ? ElarionConfigValidator.nonBlank(rulePath(rule, field)) : ElarionConfigValidator.pass(),
                List.of(),
                "",
                "",
                true,
                false,
                ElarionConfigPermission.OPERATOR,
                ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<Integer> rewardIntEntry(
            String rewardId,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<List<RewardAction>, Integer> value,
            int defaultValue,
            int minimum,
            int maximum
    ) {
        return new ElarionConfigEntry<>(
                rewardId(rewardId, field),
                rewardLabel(rewardId, label),
                description,
                rewardPath(rewardId, field),
                ElarionConfigCodec.INTEGER,
                defaultValue,
                () -> value.apply(currentReward(config, rewardId)),
                ElarionConfigValidator.integerRange(rewardPath(rewardId, field), minimum, maximum),
                List.of(),
                Integer.toString(minimum),
                maximum == Integer.MAX_VALUE ? "" : Integer.toString(maximum),
                true,
                false,
                ElarionConfigPermission.OPERATOR,
                ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> rewardStringEntry(
            String rewardId,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            Function<List<RewardAction>, String> value,
            String defaultValue,
            boolean nonBlank
    ) {
        return new ElarionConfigEntry<>(
                rewardId(rewardId, field),
                rewardLabel(rewardId, label),
                description,
                rewardPath(rewardId, field),
                ElarionConfigCodec.STRING,
                defaultValue,
                () -> value.apply(currentReward(config, rewardId)),
                nonBlank ? ElarionConfigValidator.nonBlank(rewardPath(rewardId, field)) : ElarionConfigValidator.pass(),
                List.of(),
                "",
                "",
                true,
                false,
                ElarionConfigPermission.OPERATOR,
                ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> rewardActionStringEntry(
            String rewardId,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            int index,
            Function<RewardAction, String> value,
            String defaultValue
    ) {
        return rewardActionStringEntry(rewardId, field, label, description, config, index, value, defaultValue, true);
    }

    private static ElarionConfigEntry<String> rewardActionStringEntry(
            String rewardId,
            String field,
            String label,
            String description,
            CoreConfigManager config,
            int index,
            Function<RewardAction, String> value,
            String defaultValue,
            boolean nonBlank
    ) {
        return new ElarionConfigEntry<>(
                rewardId(rewardId, field),
                rewardLabel(rewardId, label),
                description,
                rewardPath(rewardId, field),
                ElarionConfigCodec.STRING,
                defaultValue,
                () -> value.apply(currentRewardAction(config, rewardId, index)),
                nonBlank ? ElarionConfigValidator.nonBlank(rewardPath(rewardId, field))
                        : ElarionConfigValidator.pass(),
                List.of(),
                "",
                "",
                true,
                false,
                ElarionConfigPermission.OPERATOR,
                ElarionConfigPermission.OPERATOR);
    }

    private static TitleDefinition currentTitle(CoreConfigManager config, TitleDefinition fallback) {
        TitleDefinition current = config.titles().get(fallback.id());
        return current == null ? fallback : current;
    }

    private static ProgressionRegion currentRegion(CoreConfigManager config, ProgressionRegion fallback) {
        ProgressionRegion current = config.progressionRegions().get(fallback.id());
        return current == null ? fallback : current;
    }

    private static TitleUnlockRule currentRule(CoreConfigManager config, TitleUnlockRule fallback) {
        TitleUnlockRule current = config.titleUnlockRules().get(fallback.id());
        return current == null ? fallback : current;
    }

    private static List<RewardAction> currentReward(CoreConfigManager config, String rewardId) {
        return config.rewards().getOrDefault(rewardId, List.of());
    }

    private static RewardAction currentRewardAction(CoreConfigManager config, String rewardId, int index) {
        List<RewardAction> actions = currentReward(config, rewardId);
        if (index < 0 || index >= actions.size()) return new RewardAction("", Map.of());
        return actions.get(index);
    }

    private static List<TitleDefinition> sortedTitles(Map<String, TitleDefinition> titles) {
        return titles.values().stream()
                .sorted(Comparator.comparing(TitleDefinition::id))
                .toList();
    }

    private static List<ProgressionRegion> sortedRegions(Map<String, ProgressionRegion> regions) {
        return regions.values().stream()
                .sorted(Comparator.comparing(ProgressionRegion::id))
                .toList();
    }

    private static List<TitleUnlockRule> sortedRules(Map<String, TitleUnlockRule> rules) {
        return rules.values().stream()
                .sorted(Comparator.comparing(TitleUnlockRule::id))
                .toList();
    }

    private static List<Map.Entry<String, List<RewardAction>>> sortedRewardEntries(
            Map<String, List<RewardAction>> rewards
    ) {
        return rewards.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();
    }

    private static String titleId(TitleDefinition title, String field) {
        return "titles." + title.id() + "." + field;
    }

    private static String titlePath(TitleDefinition title, String field) {
        return "titles.yml.titles." + title.id() + "." + field;
    }

    private static String titleLabel(TitleDefinition title, String fieldLabel) {
        return title.id() + " " + fieldLabel;
    }

    private static String regionId(ProgressionRegion region, String field) {
        return "regions." + region.id() + "." + field;
    }

    private static String regionPath(ProgressionRegion region, String field) {
        return "title-progression.yml.regions." + region.id() + "." + field;
    }

    private static String regionLabel(ProgressionRegion region, String fieldLabel) {
        return region.id() + " " + fieldLabel;
    }

    private static String ruleId(TitleUnlockRule rule, String field) {
        return "rules." + rule.id() + "." + field;
    }

    private static String rulePath(TitleUnlockRule rule, String field) {
        return "title-progression.yml.rules." + rule.id() + "." + field;
    }

    private static String ruleLabel(TitleUnlockRule rule, String fieldLabel) {
        return rule.id() + " " + fieldLabel;
    }

    private static String rewardId(String rewardId, String field) {
        return "rewards." + rewardId + "." + field;
    }

    private static String rewardPath(String rewardId, String field) {
        return "rewards.yml.rewards." + rewardId + "." + field;
    }

    private static String rewardLabel(String rewardId, String fieldLabel) {
        return rewardId + " " + fieldLabel;
    }

    private static List<String> acquisitionModeChoices() {
        List<String> choices = new ArrayList<>();
        for (TitleAcquisitionMode mode : TitleAcquisitionMode.values()) {
            choices.add(mode.name());
        }
        return choices;
    }

    private static List<String> ownershipModeChoices() {
        List<String> choices = new ArrayList<>();
        for (TitleOwnershipMode mode : TitleOwnershipMode.values()) {
            choices.add(mode.name());
        }
        return choices;
    }

    private static String activeEffectsSummary(TitleDefinition title) {
        if (title.activeEffects().isEmpty()) return "";
        List<String> summaries = new ArrayList<>();
        for (TitleActiveEffect effect : title.activeEffects()) {
            summaries.add(effect.type() + "(" + keyValues(effect.parameters()) + ")");
        }
        return String.join(", ", summaries);
    }

    private static String continuousSummary(TitleUnlockRule rule) {
        TitleUnlockRule.Continuous continuous = rule.continuous();
        if (continuous == null) return "";
        return "duration=" + continuous.duration()
                + " " + continuous.durationUnit()
                + ", sample=" + continuous.sampleIntervalTicks()
                + ", reset=" + continuous.resetOnFailure()
                + ", required-status=" + joined(continuous.requiredStatusEffects())
                + ", allowed-status=" + joined(continuous.allowedStatusEffects())
                + ", required-metadata=" + joined(continuous.requiredMetadata());
    }

    private static String metricScope(TitleUnlockRule.MetricCondition metric) {
        String type = metric.scopeType().name().toLowerCase(java.util.Locale.ROOT);
        return metric.scopeId() == null ? type : type + ":" + metric.scopeId();
    }

    private static String matchers(Collection<TitleUnlockRule.RegistryMatcher> matchers) {
        if (matchers == null || matchers.isEmpty()) return "";
        return matchers.stream()
                .map(matcher -> (matcher.tag() ? "#" : "") + matcher.id())
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static String keyValues(Map<String, String> values) {
        if (values == null || values.isEmpty()) return "";
        return values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static String rewardActionTypes(List<RewardAction> actions) {
        if (actions == null || actions.isEmpty()) return "";
        List<String> types = new ArrayList<>();
        for (RewardAction action : actions) {
            types.add(action.type());
        }
        return String.join(", ", types);
    }

    private static String keys(Map<String, ?> values) {
        return values.keySet().stream().sorted()
                .reduce((left, right) -> left + ", " + right).orElse("");
    }

    private static String decimal(double value) {
        if (value == Math.rint(value)) return Long.toString((long) value);
        return Double.toString(value);
    }

    private static String decimal(float value) {
        if (value == Math.rint(value)) return Long.toString((long) value);
        return Float.toString(value);
    }

    private static String joined(Collection<String> values) {
        return values == null ? "" : values.stream().sorted()
                .reduce((left, right) -> left + ", " + right).orElse("");
    }
}
