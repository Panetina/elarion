package panetina.elarion.core.service;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.config.ElarionConfigApplyExecutor;
import panetina.elarion.core.config.ElarionConfigCategory;
import panetina.elarion.core.config.ElarionConfigApplyReadiness;
import panetina.elarion.core.config.ElarionConfigApplyReadinessProvider;
import panetina.elarion.core.config.ElarionConfigChangeError;
import panetina.elarion.core.config.ElarionConfigChangeRequest;
import panetina.elarion.core.config.ElarionConfigChangeResult;
import panetina.elarion.core.config.ElarionConfigChangeValidator;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEditControl;
import panetina.elarion.core.config.ElarionConfigEditTarget;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.ElarionAdminPanelAction;
import panetina.elarion.core.model.ElarionAdminPanelRow;
import panetina.elarion.core.model.ElarionAdminPanelSnapshot;
import panetina.elarion.core.model.ElarionAdminPanelTab;
import panetina.elarion.core.model.ElarionDomainEvent;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.network.AdminPanelOpenPayload;
import panetina.elarion.core.network.ElarionConfigEditOpenPayload;
import panetina.elarion.core.network.ElarionConfigEditRequestPayload;
import panetina.elarion.core.network.ElarionConfigEditResultPayload;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public final class ElarionAdminPanelService {
    public static final String CORE_PROVIDER = "core";
    static final String OPEN_CONFIG_EDITOR_ACTION = "open_config_editor";
    static final String VALIDATE_CONFIG_VALUE_ACTION = "validate_config_value";
    private static final String CONFIG_EDIT_DISABLED_REASON = "Config editing is not enabled yet.";
    private static final String CONFIG_EDIT_EXECUTION_DISABLED_REASON =
            "Config apply execution is not enabled yet.";
    private static final List<String> TAB_ORDER = List.of("overview", "players", "systems", "configs", "realms", "danger");

    private final List<ElarionAdminPanelProvider> providers = new CopyOnWriteArrayList<>();
    private ElarionApi api;
    private ElarionConfigApplyExecutor configApplyExecutor;

    public void bindApi(ElarionApi api) {
        this.api = api;
    }

    public void bindConfigApplyExecutor(ElarionConfigApplyExecutor executor) {
        this.configApplyExecutor = java.util.Objects.requireNonNull(
                executor, "Config apply executor is required");
    }

    public void bindConfigApplyReadiness(ElarionConfigApplyReadinessProvider provider) {
        java.util.Objects.requireNonNull(provider, "Config apply readiness provider is required");
        this.configApplyExecutor = new ElarionConfigApplyExecutor() {
            @Override
            public ElarionConfigApplyReadiness readiness(ElarionConfigEditTarget target) {
                return provider.readiness(target);
            }

            @Override
            public ElarionConfigChangeResult apply(
                    ElarionConfigChangeRequest request,
                    ElarionConfigPermission actorPermission
            ) {
                return ElarionConfigChangeResult.rejected(request, List.of(
                        ElarionConfigChangeError.of(
                                ElarionConfigChangeError.Code.UNSUPPORTED,
                                request.domainId() + ":" + request.categoryId() + ":" + request.entryId(),
                                CONFIG_EDIT_EXECUTION_DISABLED_REASON)));
            }
        };
    }

    public void registerProvider(ElarionAdminPanelProvider provider) {
        if (provider == null || clean(provider.id()).isBlank()) {
            throw new IllegalArgumentException("Admin panel provider id is required.");
        }
        providers.removeIf(existing -> clean(existing.id()).equals(clean(provider.id())));
        providers.add(provider);
        providers.sort(Comparator.comparing(ElarionAdminPanelProvider::id));
    }

    public List<ElarionAdminPanelProvider> providers() {
        return List.copyOf(providers);
    }

    static List<String> tabOrder() {
        return TAB_ORDER;
    }

    public void open(ServerPlayerEntity admin) {
        open(admin, "overview", "", "");
    }

    public void open(ServerPlayerEntity admin, String selectedTabId, String selectedRowId, String message) {
        requireApi();
        ServerPlayNetworking.send(admin, new AdminPanelOpenPayload(
                snapshot(new Context(api, admin), selectedTabId, selectedRowId, message)));
    }

    public ActionResult act(
            ServerPlayerEntity admin,
            String providerId,
            String actionId,
            String targetId,
            Map<String, String> parameters,
            boolean confirmed
    ) {
        requireApi();
        if (!admin.hasPermissionLevel(4)) return ActionResult.failure("Only OP level 4 admins can use the panel.");
        Context context = new Context(api, admin);
        String provider = clean(providerId);
        String action = clean(actionId);
        ActionResult result;
        try {
            if (CORE_PROVIDER.equals(provider)) {
                result = coreAction(context, action, clean(targetId), safeParameters(parameters), confirmed);
            } else {
                ElarionAdminPanelProvider panelProvider = providers.stream()
                        .filter(candidate -> clean(candidate.id()).equals(provider))
                        .findFirst()
                        .orElse(null);
                if (panelProvider == null) {
                    result = ActionResult.failure("Unknown admin panel provider: " + provider);
                } else {
                    result = panelProvider.act(context, action, clean(targetId), safeParameters(parameters), confirmed);
                }
            }
        } catch (IllegalArgumentException exception) {
            result = ActionResult.failure(exception.getMessage());
        } catch (Exception exception) {
            result = ActionResult.failure("Admin action failed: " + exception.getMessage());
        }
        open(admin, tabForAction(action), targetId, result.message());
        return result;
    }

    public ElarionConfigEditResultPayload validateConfigEdit(
            ServerPlayerEntity admin,
            ElarionConfigEditRequestPayload payload
    ) {
        requireApi();
        return configEditResult(
                api.configs(), configApplyExecutor, admin.getUuid(), admin.hasPermissionLevel(4), payload);
    }

    public ElarionAdminPanelSnapshot snapshot(
            Context context,
            String selectedTabId,
            String selectedRowId,
            String message
    ) {
        String selected = clean(selectedTabId).isBlank() ? "overview" : clean(selectedTabId);
        if (!TAB_ORDER.contains(selected)) selected = "overview";
        List<ElarionAdminPanelTab> tabs = List.of(
                selectedTab(selected, "overview", "Overview", "Server status and quick links.",
                        () -> overviewRows(context)),
                selectedTab(selected, "players", "Players", "Inspect, move, and edit online players.",
                        () -> playerRows(context)),
                selectedTab(selected, "systems", "Systems", "Provider-owned testing and repair actions.",
                        () -> systemRows(context)),
                selectedTab(selected, "configs", "Config", "Read-only config domain discovery.",
                        () -> configRows(context.api().configs(), selectedRowId)),
                selectedTab(selected, "realms", "Realms", "Realm-scoped state and reset actions.",
                        () -> realmRows(context)),
                selectedTab(selected, "danger", "Danger Zone", "Runtime-wide reset tools.",
                        () -> dangerRows(context))
        );
        return new ElarionAdminPanelSnapshot(
                "Admin Panel",
                "Validated Elarion testing and repair controls.",
                selected,
                clean(selectedRowId),
                clean(message),
                tabs);
    }

    static ElarionAdminPanelTab selectedTab(
            String selectedTabId,
            String id,
            String title,
            String subtitle,
            Supplier<List<ElarionAdminPanelRow>> rows
    ) {
        return new ElarionAdminPanelTab(
                id,
                title,
                subtitle,
                id.equals(selectedTabId) ? List.copyOf(rows.get()) : List.of());
    }

    private List<ElarionAdminPanelRow> overviewRows(Context context) {
        int players = context.admin().getServer().getPlayerManager().getCurrentPlayerCount();
        int realms = context.api().realms().all().size();
        int resettable = (int) providers.stream().filter(ElarionAdminPanelProvider::supportsRuntimeReset).count();
        List<ElarionAdminPanelRow> rows = new ArrayList<>();
        rows.add(ElarionAdminPanelRow.card("overview_players", "Online Players",
                players + " connected", "Open Players to inspect, move, or repair online players.",
                "Players", "item:minecraft:player_head", List.of()));
        rows.add(ElarionAdminPanelRow.card("overview_realms", "Realms",
                realms + " configured", "Open Realms for Realm-scoped reset and progression actions.",
                "Realms", "item:minecraft:banner", List.of()));
        rows.add(ElarionAdminPanelRow.card("overview_providers", "Loaded Providers",
                providers.size() + " registered", providerList(),
                resettable + " resettable", "item:minecraft:comparator", List.of()));
        return rows;
    }

    private List<ElarionAdminPanelRow> playerRows(Context context) {
        List<ElarionAdminPanelRow> rows = new ArrayList<>();
        List<ServerPlayerEntity> players = new ArrayList<>(context.admin().getServer().getPlayerManager().getPlayerList());
        players.sort(Comparator.comparing(player -> player.getGameProfile().getName().toLowerCase(Locale.ROOT)));
        for (ServerPlayerEntity player : players) {
            CitizenRecord citizen = context.api().citizens().getOrCreate(player);
            String realm = citizen.realmId().isBlank() ? "No Realm" : citizen.realmId();
            String title = citizen.activeTitleId().isBlank() ? "No active title" : citizen.activeTitleId();
            List<ElarionAdminPanelAction> actions = new ArrayList<>();
            actions.add(ElarionAdminPanelAction.normal(CORE_PROVIDER, "teleport_admin_to_player", "Go to Player"));
            actions.add(ElarionAdminPanelAction.normal(CORE_PROVIDER, "teleport_player_to_admin", "Bring Here"));
            actions.add(ElarionAdminPanelAction.normal(CORE_PROVIDER, "teleport_player_realm_spawn", "Realm Spawn"));
            actions.add(ElarionAdminPanelAction.input(CORE_PROVIDER, "set_realm", "Set Realm",
                    "value", "Realm id", realmPlaceholder(context), realmSuggestions(context)));
            actions.add(ElarionAdminPanelAction.input(CORE_PROVIDER, "set_nickname", "Set Nickname",
                    "value", "Nickname", "Nickname"));
            actions.add(ElarionAdminPanelAction.normal(CORE_PROVIDER, "clear_nickname", "Clear Nickname"));
            actions.add(ElarionAdminPanelAction.input(CORE_PROVIDER, "grant_title", "Grant Title",
                    "value", "Title id", titlePlaceholder(context), titleSuggestions(context)));
            actions.add(ElarionAdminPanelAction.input(CORE_PROVIDER, "revoke_title", "Revoke Title",
                    "value", "Title id", titlePlaceholder(context), titleSuggestions(context)));
            actions.add(ElarionAdminPanelAction.input(CORE_PROVIDER, "set_active_title", "Set Active Title",
                    "value", "Title id", titlePlaceholder(context), titleSuggestions(context)));
            actions.add(ElarionAdminPanelAction.normal(CORE_PROVIDER, "clear_active_title", "Clear Active Title"));
            actions.add(ElarionAdminPanelAction.input(CORE_PROVIDER, "grant_ability", "Grant Ability",
                    "value", "Ability id", abilityPlaceholder(context), abilitySuggestions(context)));
            actions.add(ElarionAdminPanelAction.input(CORE_PROVIDER, "revoke_ability", "Revoke Ability",
                    "value", "Ability id", abilityPlaceholder(context), abilitySuggestions(context)));
            actions.add(ElarionAdminPanelAction.normal(CORE_PROVIDER, "finish_cooldown", "Finish Cooldown"));
            actions.add(ElarionAdminPanelAction.normal(CORE_PROVIDER, "reset_character", "Reset Character"));
            actions.add(ElarionAdminPanelAction.normal(CORE_PROVIDER, "force_active_character", "Force Active"));
            for (ElarionAdminPanelProvider provider : providers) {
                actions.addAll(provider.playerActions(context, player));
            }
            rows.add(ElarionAdminPanelRow.card(player.getUuidAsString(),
                    player.getGameProfile().getName(),
                    realm + " - " + title,
                    "Nickname: " + (citizen.nickname() == null || citizen.nickname().isBlank()
                            ? "-" : citizen.nickname())
                            + "\nAbilities: " + (citizen.grantedAbilities().isEmpty()
                            ? "-" : String.join(", ", citizen.grantedAbilities()))
                            + "\nUnlocked titles: " + citizen.unlockedTitleIds().size(),
                    player.getWorld().getRegistryKey().getValue().toString(),
                    "item:minecraft:player_head",
                    actions));
        }
        if (rows.isEmpty()) {
            rows.add(ElarionAdminPanelRow.card("no_players", "No Online Players",
                    "Only online players are editable in V1.", "Use commands for offline records.",
                    "Empty", "item:minecraft:barrier", List.of()));
        }
        return rows;
    }

    private List<ElarionAdminPanelRow> systemRows(Context context) {
        List<ElarionAdminPanelRow> rows = new ArrayList<>();
        for (ElarionAdminPanelProvider provider : providers) {
            rows.addAll(provider.systemRows(context));
        }
        if (rows.isEmpty()) {
            rows.add(ElarionAdminPanelRow.card("no_systems", "No Providers",
                    "No addon panel providers are registered.", "Addons can register providers through Core.",
                    "Empty", "item:minecraft:barrier", List.of()));
        }
        return rows;
    }

    static List<ElarionAdminPanelRow> configRows(ElarionConfigRegistry registry) {
        return configRows(registry, "");
    }

    static List<ElarionAdminPanelRow> configRows(ElarionConfigRegistry registry, String selectedRowId) {
        if (registry == null || registry.domains().isEmpty()) return List.of();
        ConfigCategoryTarget focus = ConfigCategoryTarget.from(selectedRowId);
        List<ElarionAdminPanelRow> rows = new ArrayList<>();
        for (ElarionConfigDomain domain : registry.domains()) {
            rows.add(ElarionAdminPanelRow.card(
                    "config:" + domain.id(),
                    "Config: " + domain.label(),
                    domain.ownerModule(),
                    configDomainBody(domain),
                    domain.categories().size() + " categories",
                    "item:minecraft:writable_book",
                    List.of()));
            for (ElarionConfigCategory category : domain.categories()) {
                rows.add(ElarionAdminPanelRow.card(
                        "config:" + domain.id() + ":category:" + category.id(),
                        domain.label() + ": " + category.label(),
                        domain.ownerModule(),
                        configCategoryBody(domain, category, focus.matches(domain.id(), category.id())),
                        category.entries().size() + " entries",
                        "item:minecraft:paper",
                        List.of()));
                if (!focus.matches(domain.id(), category.id())) continue;
                for (ElarionConfigEntry<?> entry : category.entries()) {
                    rows.add(ElarionAdminPanelRow.card(
                            configEntryRowId(domain.id(), category.id(), entry.id()),
                            entry.label(),
                            domain.label() + " / " + category.label(),
                            configEntryBody(domain, category, entry),
                            configEntryState(entry),
                            "item:minecraft:name_tag",
                            List.of(ElarionAdminPanelAction.normal(CORE_PROVIDER, OPEN_CONFIG_EDITOR_ACTION,
                                            "Open Editor"),
                                    ElarionAdminPanelAction.input(CORE_PROVIDER, VALIDATE_CONFIG_VALUE_ACTION,
                                            "Validate Value", "value", "Proposed value",
                                            entry.currentDisplayValue()))));
                }
            }
        }
        return rows;
    }

    private static String configDomainBody(ElarionConfigDomain domain) {
        StringBuilder body = new StringBuilder();
        appendLine(body, "Owner: " + domain.ownerModule());
        appendLine(body, "Files: " + (domain.files().isEmpty() ? "-" : String.join(", ", domain.files())));
        appendLine(body, "Reload: " + (domain.reloadCommand().isBlank() ? "Not declared" : domain.reloadCommand()));
        appendLine(body, "Mode: Read-only discovery. Edits are not enabled here.");
        appendLine(body, "Categories: " + domain.categories().size());
        appendLine(body, "Entries: " + entryCount(domain));
        appendLine(body, "Reloadable entries: " + reloadableCount(domain));
        appendLine(body, "Restart-required entries: " + restartRequiredCount(domain));
        appendLine(body, "Invalid entries: " + invalidCount(domain));
        appendLine(body, "");
        appendLine(body, "Open a category row below for entry details.");
        for (ElarionConfigCategory category : domain.categories()) {
            appendLine(body, "- " + category.label() + ": " + category.entries().size() + " entries");
        }
        return body.toString().trim();
    }

    private static String configCategoryBody(
            ElarionConfigDomain domain,
            ElarionConfigCategory category,
            boolean expanded
    ) {
        StringBuilder body = new StringBuilder();
        appendLine(body, "Domain: " + domain.label() + " (" + domain.id() + ")");
        appendLine(body, "Owner: " + domain.ownerModule());
        appendLine(body, "Category: " + category.label() + " (" + category.id() + ")");
        appendLine(body, "Description: " + category.description());
        appendLine(body, "Mode: Read-only discovery. Edits are not enabled here.");
        appendLine(body, "Entries: " + category.entries().size());
        appendLine(body, expanded ? "Showing entries below." : "Select this category to load entry rows.");
        if (!expanded) return body.toString().trim();
        appendLine(body, "");
        for (ElarionConfigEntry<?> entry : category.entries()) {
            appendEntry(body, entry);
        }
        return body.toString().trim();
    }

    private static void appendEntry(StringBuilder body, ElarionConfigEntry<?> entry) {
        String line = "- " + entry.label() + " (" + entry.path() + "): current="
                + value(entry.currentDisplayValue()) + ", default=" + value(entry.defaultDisplayValue())
                + ", type=" + entry.codec().valueType().name().toLowerCase(Locale.ROOT);
        if (!entry.minimum().isBlank() || !entry.maximum().isBlank()) {
            line += ", bounds=" + (entry.minimum().isBlank() ? "*" : entry.minimum())
                    + ".." + (entry.maximum().isBlank() ? "*" : entry.maximum());
        }
        if (!entry.choices().isEmpty()) {
            line += ", choices=" + String.join("/", entry.choices());
        }
        line += entry.restartRequired() ? ", restart required"
                : entry.runtimeReloadable() ? ", reloadable" : ", static";
        List<String> errors = entry.validateCurrent();
        if (!errors.isEmpty()) line += ", invalid=" + String.join("; ", errors);
        appendLine(body, line);
    }

    private static String configEntryBody(
            ElarionConfigDomain domain,
            ElarionConfigCategory category,
            ElarionConfigEntry<?> entry
    ) {
        StringBuilder body = new StringBuilder();
        appendLine(body, "Domain: " + domain.label() + " (" + domain.id() + ")");
        appendLine(body, "Owner: " + domain.ownerModule());
        appendLine(body, "Category: " + category.label() + " (" + category.id() + ")");
        appendLine(body, "Path: " + entry.path());
        appendLine(body, "Description: " + entry.description());
        appendLine(body, "Current: " + value(entry.currentDisplayValue()));
        appendLine(body, "Default: " + value(entry.defaultDisplayValue()));
        appendLine(body, "Type: " + entry.codec().valueType().name().toLowerCase(Locale.ROOT));
        if (!entry.minimum().isBlank() || !entry.maximum().isBlank()) {
            appendLine(body, "Bounds: " + (entry.minimum().isBlank() ? "*" : entry.minimum())
                    + ".." + (entry.maximum().isBlank() ? "*" : entry.maximum()));
        }
        if (!entry.choices().isEmpty()) {
            appendLine(body, "Choices: " + String.join(", ", entry.choices()));
        }
        appendLine(body, "Runtime: " + configEntryState(entry));
        appendLine(body, "Read Permission: " + entry.readPermission().label());
        appendLine(body, "Write Permission: " + entry.writePermission().label());
        List<String> errors = entry.validateCurrent();
        appendLine(body, "Current Validation: " + (errors.isEmpty() ? "Valid" : String.join("; ", errors)));
        appendLine(body, "");
        appendLine(body, "Validate Value previews parsing and validation only. It does not write files, reload config, or change runtime state.");
        return body.toString().trim();
    }

    private static String configEntryState(ElarionConfigEntry<?> entry) {
        List<String> errors = entry.validateCurrent();
        if (!errors.isEmpty()) return "Invalid";
        if (entry.restartRequired()) return "Restart required";
        if (entry.runtimeReloadable()) return "Reloadable";
        return "Static";
    }

    private static String configEntryRowId(String domainId, String categoryId, String entryId) {
        return "config-entry|" + clean(domainId) + "|" + clean(categoryId) + "|" + clean(entryId);
    }

    private static String value(String value) {
        String clean = clean(value);
        if (clean.isBlank()) return "-";
        return clean.length() > 80 ? clean.substring(0, 77) + "..." : clean;
    }

    private static int entryCount(ElarionConfigDomain domain) {
        int count = 0;
        for (ElarionConfigCategory category : domain.categories()) count += category.entries().size();
        return count;
    }

    private static int reloadableCount(ElarionConfigDomain domain) {
        int count = 0;
        for (ElarionConfigCategory category : domain.categories()) {
            for (ElarionConfigEntry<?> entry : category.entries()) {
                if (entry.runtimeReloadable()) count++;
            }
        }
        return count;
    }

    private static int restartRequiredCount(ElarionConfigDomain domain) {
        int count = 0;
        for (ElarionConfigCategory category : domain.categories()) {
            for (ElarionConfigEntry<?> entry : category.entries()) {
                if (entry.restartRequired()) count++;
            }
        }
        return count;
    }

    private static int invalidCount(ElarionConfigDomain domain) {
        int count = 0;
        for (ElarionConfigCategory category : domain.categories()) {
            for (ElarionConfigEntry<?> entry : category.entries()) {
                if (!entry.validateCurrent().isEmpty()) count++;
            }
        }
        return count;
    }

    private static void appendLine(StringBuilder body, String line) {
        if (body.length() >= 1900) return;
        String clean = line == null ? "" : line;
        int remaining = 1900 - body.length();
        if (clean.length() + 1 > remaining) {
            body.append(clean, 0, Math.max(0, remaining - 4)).append("...");
            return;
        }
        body.append(clean).append('\n');
    }

    private List<ElarionAdminPanelRow> realmRows(Context context) {
        List<ElarionAdminPanelRow> rows = new ArrayList<>();
        for (RealmDefinition realm : context.api().realms().all()) {
            List<ElarionAdminPanelAction> actions = new ArrayList<>();
            for (ElarionAdminPanelProvider provider : providers) {
                for (ElarionAdminPanelRow providerRow : provider.realmRows(context, realm.id())) {
                    actions.addAll(providerRow.actions());
                }
            }
            rows.add(ElarionAdminPanelRow.card(realm.id(),
                    context.api().realms().officialName(realm),
                    realm.id() + " - " + realm.shortName(),
                    "Realm-scoped actions are provided by owning addons. Core keeps Realm identity and membership canonical.",
                    actions.isEmpty() ? "No actions" : actions.size() + " actions",
                    "item:minecraft:filled_map",
                    actions));
        }
        return rows;
    }

    private List<ElarionAdminPanelRow> dangerRows(Context context) {
        List<String> resetters = providers.stream()
                .filter(ElarionAdminPanelProvider::supportsRuntimeReset)
                .map(ElarionAdminPanelProvider::runtimeResetDescription)
                .toList();
        String body = resetters.isEmpty()
                ? "No runtime reset providers are registered."
                : "This resets Elarion runtime/progression state only:\n- " + String.join("\n- ", resetters)
                + "\n\nIt does not delete configs, world files, placed blocks, NPC placements, portal endpoints, or inventories.";
        return List.of(ElarionAdminPanelRow.danger("runtime_reset_all",
                "Reset Everything",
                "Runtime state only",
                body,
                resetters.isEmpty() ? "Unavailable" : resetters.size() + " systems",
                "item:minecraft:tnt",
                List.of(ElarionAdminPanelAction.danger(CORE_PROVIDER, "runtime_reset_all",
                        "Reset Everything", "Confirm Runtime Reset", body))));
    }

    private ActionResult coreAction(
            Context context,
            String action,
            String targetId,
            Map<String, String> parameters,
            boolean confirmed
    ) {
        return switch (action) {
            case "teleport_admin_to_player" -> teleportAdminToPlayer(context, targetId);
            case "teleport_player_to_admin" -> teleportPlayerToAdmin(context, targetId);
            case "teleport_player_realm_spawn" -> teleportPlayerRealmSpawn(context, targetId);
            case "set_realm" -> setRealm(context, targetId, parameters.getOrDefault("value", ""));
            case "set_nickname" -> setNickname(context, targetId, parameters.getOrDefault("value", ""));
            case "clear_nickname" -> clearNickname(context, targetId);
            case "grant_title" -> title(context, targetId, parameters.getOrDefault("value", ""), "grant");
            case "revoke_title" -> title(context, targetId, parameters.getOrDefault("value", ""), "revoke");
            case "set_active_title" -> title(context, targetId, parameters.getOrDefault("value", ""), "active");
            case "clear_active_title" -> title(context, targetId, "", "clear");
            case "grant_ability" -> ability(context, targetId, parameters.getOrDefault("value", ""), true);
            case "revoke_ability" -> ability(context, targetId, parameters.getOrDefault("value", ""), false);
            case "finish_cooldown" -> lifecycle(context, targetId, "finish");
            case "reset_character" -> lifecycle(context, targetId, "reset");
            case "force_active_character" -> lifecycle(context, targetId, "active");
            case OPEN_CONFIG_EDITOR_ACTION -> openConfigEditor(context, targetId);
            case VALIDATE_CONFIG_VALUE_ACTION -> configValidationPreview(
                    context.api().configs(), context.admin().getUuid(), targetId,
                    parameters.getOrDefault("value", ""));
            case "runtime_reset_all" -> runtimeResetAll(context, confirmed);
            default -> ActionResult.failure("Unknown Core admin action: " + action);
        };
    }

    private ActionResult openConfigEditor(Context context, String targetId) {
        ConfigEditOpenResult result = configEditOpenControl(
                context.api().configs(), configApplyExecutor, targetId);
        if (!result.success()) return ActionResult.failure(result.message());
        ServerPlayNetworking.send(context.admin(), new ElarionConfigEditOpenPayload(result.control(), ""));
        return ActionResult.success(result.message());
    }

    static ConfigEditOpenResult configEditOpenControl(ElarionConfigRegistry registry, String targetId) {
        return configEditOpenControl(registry, null, targetId);
    }

    static ConfigEditOpenResult configEditOpenControl(
            ElarionConfigRegistry registry,
            ElarionConfigApplyReadinessProvider readinessProvider,
            String targetId
    ) {
        ConfigEntryTarget parsed = ConfigEntryTarget.parse(targetId);
        if (parsed == null) return ConfigEditOpenResult.failure("Invalid config entry target.");
        if (registry == null) return ConfigEditOpenResult.failure("Config registry is unavailable.");

        ElarionConfigDomain domain = registry.domain(parsed.domainId()).orElse(null);
        if (domain == null) return ConfigEditOpenResult.failure("Unknown config domain.");
        ElarionConfigCategory category = domain.category(parsed.categoryId()).orElse(null);
        if (category == null) return ConfigEditOpenResult.failure("Unknown config category.");
        ElarionConfigEntry<?> entry = category.entry(parsed.entryId()).orElse(null);
        if (entry == null) return ConfigEditOpenResult.failure("Unknown config entry.");

        ElarionConfigEditTarget target = new ElarionConfigEditTarget(
                parsed.domainId(), parsed.categoryId(), parsed.entryId());
        ElarionConfigApplyReadiness readiness = configApplyReadiness(readinessProvider, target);
        boolean ready = readiness != null && readiness.ready();
        String disabledReason = ready ? "" : configApplyDisabledReason(readiness);
        ElarionConfigEditControl control = new ElarionConfigEditControl(
                target,
                entry.label(),
                entry.description(),
                entry.path(),
                entry.codec().valueType(),
                entry.currentDisplayValue(),
                entry.defaultDisplayValue(),
                entry.choices(),
                entry.minimum(),
                entry.maximum(),
                entry.runtimeReloadable(),
                entry.restartRequired(),
                entry.readPermission(),
                entry.writePermission(),
                ready,
                ready,
                disabledReason,
                disabledReason);
        return ConfigEditOpenResult.success("Opened config editor for " + entry.label() + ".", control);
    }

    static ActionResult configValidationPreview(
            ElarionConfigRegistry registry,
            UUID actorId,
            String targetId,
            String proposedValue
    ) {
        ConfigEntryTarget target = ConfigEntryTarget.parse(targetId);
        if (target == null) return ActionResult.failure("Invalid config entry target.");

        ElarionConfigChangeRequest request = new ElarionConfigChangeRequest(
                target.domainId(),
                target.categoryId(),
                target.entryId(),
                proposedValue,
                "",
                actorId,
                "admin-panel-validation-preview");
        ElarionConfigChangeResult result = ElarionConfigChangeValidator.validate(
                registry, request, ElarionConfigPermission.OPERATOR);
        if (result.success()) {
            String runtime = result.restartRequired()
                    ? "Restart required"
                    : result.reloadRequired() ? "Reload required" : "Static";
            return ActionResult.success("Valid: " + value(result.oldDisplayValue())
                    + " -> " + value(result.newDisplayValue()) + ". " + runtime + ".");
        }
        String message = result.errors().stream()
                .map(ElarionConfigChangeError::message)
                .filter(error -> !error.isBlank())
                .findFirst()
                .orElse("Value did not pass validation.");
        return ActionResult.failure("Invalid: " + message);
    }

    static ElarionConfigEditResultPayload configEditResult(
            ElarionConfigRegistry registry,
            UUID actorId,
            boolean hasOperatorPermission,
            ElarionConfigEditRequestPayload payload
    ) {
        return configEditResult(registry, (ElarionConfigApplyExecutor) null,
                actorId, hasOperatorPermission, payload);
    }

    static ElarionConfigEditResultPayload configEditResult(
            ElarionConfigRegistry registry,
            ElarionConfigApplyReadinessProvider readinessProvider,
            UUID actorId,
            boolean hasOperatorPermission,
            ElarionConfigEditRequestPayload payload
    ) {
        return configEditResult(registry, readinessOnlyExecutor(readinessProvider),
                actorId, hasOperatorPermission, payload);
    }

    static ElarionConfigEditResultPayload configEditResult(
            ElarionConfigRegistry registry,
            ElarionConfigApplyExecutor applyExecutor,
            UUID actorId,
            boolean hasOperatorPermission,
            ElarionConfigEditRequestPayload payload
    ) {
        ElarionConfigEditTarget target = payload.target();
        if (!hasOperatorPermission) {
            return rejectedConfigEdit(target, ElarionConfigChangeError.Code.PERMISSION_DENIED,
                    "", "Only OP level 4 admins can validate or apply config edits.");
        }

        ElarionConfigChangeRequest request = configEditChangeRequest(target, actorId, payload);
        if (payload.intent() == ElarionConfigEditRequestPayload.Intent.APPLY) {
            if (applyExecutor == null) {
                return rejectedConfigEdit(target, ElarionConfigChangeError.Code.UNSUPPORTED,
                        target.targetKey(), "Config apply is not enabled yet.");
            }
            return configEditApplyResult(target, applyExecutor.apply(request, ElarionConfigPermission.OPERATOR));
        }

        ElarionConfigChangeResult result = ElarionConfigChangeValidator.validate(
                registry, request, ElarionConfigPermission.OPERATOR);
        if (!result.success()) {
            String message = result.errors().stream()
                    .map(ElarionConfigChangeError::message)
                    .filter(error -> !error.isBlank())
                    .findFirst()
                    .orElse("Invalid config edit request.");
            return new ElarionConfigEditResultPayload(target, result.status(), "", "",
                    false, false, false, "", result.errors(), "Invalid: " + message);
        }

        ElarionConfigApplyReadiness readiness = configApplyReadiness(applyExecutor, target);
        boolean canApply = readiness != null && readiness.ready();
        String auditPreview = "Would change " + target.targetKey()
                + " from " + value(result.oldDisplayValue())
                + " to " + value(result.newDisplayValue()) + ".";
        String message = "Valid: " + value(result.oldDisplayValue())
                + " -> " + value(result.newDisplayValue()) + ". " + configEditRuntime(result)
                + (canApply ? ". Apply available."
                : ". Apply unavailable: " + configApplyDisabledReason(readiness));
        return new ElarionConfigEditResultPayload(target, result.status(),
                result.oldDisplayValue(), result.newDisplayValue(),
                result.reloadRequired(), result.restartRequired(),
                canApply, auditPreview, List.of(), message);
    }

    private static ElarionConfigChangeRequest configEditChangeRequest(
            ElarionConfigEditTarget target,
            UUID actorId,
            ElarionConfigEditRequestPayload payload
    ) {
        String defaultReason = payload.intent() == ElarionConfigEditRequestPayload.Intent.APPLY
                ? "admin-panel-config-edit-apply"
                : "admin-panel-config-edit-preview";
        return new ElarionConfigChangeRequest(
                target.domainId(),
                target.categoryId(),
                target.entryId(),
                payload.proposedRawValue(),
                payload.expectedCurrentDisplayValue(),
                actorId,
                clean(payload.reason()).isBlank() ? defaultReason : clean(payload.reason()));
    }

    private static ElarionConfigEditResultPayload configEditApplyResult(
            ElarionConfigEditTarget target,
            ElarionConfigChangeResult result
    ) {
        if (!result.success()) {
            String message = result.errors().stream()
                    .map(ElarionConfigChangeError::message)
                    .filter(error -> !error.isBlank())
                    .findFirst()
                    .orElse("Config apply request was rejected.");
            return new ElarionConfigEditResultPayload(target, result.status(), "", "",
                    false, false, false, "", result.errors(), "Apply failed: " + message);
        }

        String auditPreview = "Changed " + target.targetKey()
                + " from " + value(result.oldDisplayValue())
                + " to " + value(result.newDisplayValue()) + ".";
        String prefix = result.status() == ElarionConfigChangeResult.Status.APPLIED ? "Applied: " : "Valid: ";
        String message = prefix + value(result.oldDisplayValue())
                + " -> " + value(result.newDisplayValue()) + ". " + configEditRuntime(result) + ".";
        return new ElarionConfigEditResultPayload(target, result.status(),
                result.oldDisplayValue(), result.newDisplayValue(),
                result.reloadRequired(), result.restartRequired(),
                false, auditPreview, List.of(), message);
    }

    private static ElarionConfigApplyExecutor readinessOnlyExecutor(
            ElarionConfigApplyReadinessProvider readinessProvider
    ) {
        if (readinessProvider == null) return null;
        return new ElarionConfigApplyExecutor() {
            @Override
            public ElarionConfigApplyReadiness readiness(ElarionConfigEditTarget target) {
                return readinessProvider.readiness(target);
            }

            @Override
            public ElarionConfigChangeResult apply(
                    ElarionConfigChangeRequest request,
                    ElarionConfigPermission actorPermission
            ) {
                return ElarionConfigChangeResult.rejected(request, List.of(
                        ElarionConfigChangeError.of(
                                ElarionConfigChangeError.Code.UNSUPPORTED,
                                request.domainId() + ":" + request.categoryId() + ":" + request.entryId(),
                                CONFIG_EDIT_EXECUTION_DISABLED_REASON)));
            }
        };
    }

    private static String configApplyDisabledReason(
            ElarionConfigApplyReadinessProvider readinessProvider,
            ElarionConfigEditTarget target
    ) {
        return configApplyDisabledReason(configApplyReadiness(readinessProvider, target));
    }

    private static ElarionConfigApplyReadiness configApplyReadiness(
            ElarionConfigApplyReadinessProvider readinessProvider,
            ElarionConfigEditTarget target
    ) {
        if (readinessProvider == null) return null;
        return readinessProvider.readiness(target);
    }

    private static String configApplyDisabledReason(ElarionConfigApplyReadiness readiness) {
        if (readiness == null) return CONFIG_EDIT_DISABLED_REASON;
        return readiness.ready()
                ? ""
                : clean(readiness.firstErrorMessage()).isBlank()
                ? CONFIG_EDIT_DISABLED_REASON
                : clean(readiness.firstErrorMessage());
    }

    private static ElarionConfigEditResultPayload rejectedConfigEdit(
            ElarionConfigEditTarget target,
            ElarionConfigChangeError.Code code,
            String path,
            String message
    ) {
        return new ElarionConfigEditResultPayload(target, ElarionConfigChangeResult.Status.REJECTED,
                "", "", false, false, false, "",
                List.of(ElarionConfigChangeError.of(code, path, message)), message);
    }

    private static String configEditRuntime(ElarionConfigChangeResult result) {
        if (result.restartRequired()) return "Restart required";
        if (result.reloadRequired()) return "Reload required";
        return "Static";
    }

    private ActionResult teleportAdminToPlayer(Context context, String targetId) {
        ServerPlayerEntity target = requirePlayer(context, targetId);
        teleport(context.admin(), target);
        emit(context, "admin-teleport-to-player", target.getUuid(), Map.of());
        return ActionResult.success("Teleported to " + target.getGameProfile().getName() + ".");
    }

    private ActionResult teleportPlayerToAdmin(Context context, String targetId) {
        ServerPlayerEntity target = requirePlayer(context, targetId);
        teleport(target, context.admin());
        emit(context, "admin-teleport-player-to-admin", target.getUuid(), Map.of());
        return ActionResult.success("Brought " + target.getGameProfile().getName() + " to you.");
    }

    private ActionResult teleportPlayerRealmSpawn(Context context, String targetId) {
        ServerPlayerEntity target = requirePlayer(context, targetId);
        boolean ok = context.api().realmSpawns().teleportToRealmSpawn(target, "admin-panel-realm-spawn");
        if (!ok) return ActionResult.failure("Player has no valid Realm spawn.");
        emit(context, "admin-teleport-player-realm-spawn", target.getUuid(), Map.of());
        return ActionResult.success("Sent " + target.getGameProfile().getName() + " to Realm spawn.");
    }

    private ActionResult setRealm(Context context, String targetId, String realmId) {
        ServerPlayerEntity target = requirePlayer(context, targetId);
        String requested = clean(realmId).toLowerCase(Locale.ROOT);
        if (requested.isBlank()) return ActionResult.failure("Realm id is required.");
        Optional<RealmDefinition> realm = context.api().realms().find(requested);
        if (realm.isEmpty()) return ActionResult.failure("Unknown Realm: " + requested);
        if (!context.api().realms().assign(target, realm.get().id())) {
            return ActionResult.failure("Could not assign player to Realm: " + requested);
        }
        context.api().identitySync().syncSubject(target.getServer(), target);
        emit(context, "admin-player-realm-set", target.getUuid(), Map.of("realm", realm.get().id()));
        return ActionResult.success("Set " + target.getGameProfile().getName() + " to " + realm.get().id() + ".");
    }

    private ActionResult setNickname(Context context, String targetId, String nickname) {
        ServerPlayerEntity target = requirePlayer(context, targetId);
        NicknameService.Validation validation = context.api().nicknames().validate(target.getUuid(), nickname);
        if (!validation.valid()) return ActionResult.failure(validation.error());
        context.api().citizens().update(target, "admin-panel-nickname-set",
                citizen -> citizen.setNickname(validation.nickname()));
        context.api().identitySync().syncSubject(target.getServer(), target);
        emit(context, "admin-nickname-set", target.getUuid(), Map.of("nickname", validation.nickname()));
        return ActionResult.success("Nickname set to " + validation.nickname() + ".");
    }

    private ActionResult clearNickname(Context context, String targetId) {
        ServerPlayerEntity target = requirePlayer(context, targetId);
        context.api().citizens().update(target, "admin-panel-nickname-cleared", citizen -> citizen.setNickname(""));
        context.api().identitySync().syncSubject(target.getServer(), target);
        emit(context, "admin-nickname-cleared", target.getUuid(), Map.of());
        return ActionResult.success("Nickname cleared.");
    }

    private ActionResult title(Context context, String targetId, String titleId, String mode) {
        ServerPlayerEntity target = requirePlayer(context, targetId);
        TitleService.TitleOperation result = switch (mode) {
            case "grant" -> context.api().titles().grant(target, titleId, context.admin().getUuid(), "admin-panel");
            case "revoke" -> context.api().titles().revoke(target, titleId, context.admin().getUuid(), "admin-panel");
            case "active" -> context.api().titles().setActive(target, titleId, context.admin().getUuid(), "admin-panel");
            case "clear" -> context.api().titles().clearActive(target, context.admin().getUuid(), "admin-panel");
            default -> new TitleService.TitleOperation(false, "Unknown title action.");
        };
        context.api().identitySync().syncSubject(target.getServer(), target);
        emit(context, "admin-title-" + mode, target.getUuid(), Map.of("title", clean(titleId)));
        return new ActionResult(result.success(), result.message());
    }

    private ActionResult ability(Context context, String targetId, String abilityId, boolean grant) {
        ServerPlayerEntity target = requirePlayer(context, targetId);
        context.api().citizens().update(target, grant ? "admin-panel-ability-granted" : "admin-panel-ability-revoked",
                citizen -> {
                    if (grant) context.api().abilities().grant(citizen, abilityId);
                    else context.api().abilities().revoke(citizen, abilityId);
                });
        context.api().identitySync().syncSubject(target.getServer(), target);
        emit(context, grant ? "admin-ability-granted" : "admin-ability-revoked",
                target.getUuid(), Map.of("ability", clean(abilityId)));
        return ActionResult.success((grant ? "Granted " : "Revoked ") + abilityId + ".");
    }

    private ActionResult lifecycle(Context context, String targetId, String mode) {
        ServerPlayerEntity target = requirePlayer(context, targetId);
        switch (mode) {
            case "finish" -> context.api().characters().finishCooldown(target.getUuid());
            case "reset" -> context.api().characters().resetForTesting(target);
            case "active" -> context.api().characters().forceActiveForTesting(target);
            default -> throw new IllegalArgumentException("Unknown lifecycle action.");
        }
        emit(context, "admin-character-" + mode, target.getUuid(), Map.of());
        return ActionResult.success("Character lifecycle updated.");
    }

    private ActionResult runtimeResetAll(Context context, boolean confirmed) {
        if (!confirmed) return ActionResult.failure("Confirmation required.");
        List<String> results = new ArrayList<>();
        for (ElarionAdminPanelProvider provider : providers) {
            if (!provider.supportsRuntimeReset()) continue;
            ActionResult result = provider.runtimeReset(context);
            results.add(provider.title() + ": " + result.message());
        }
        emit(context, "admin-runtime-reset-all", context.admin().getUuid(), Map.of(
                "providers", Integer.toString(results.size())));
        return ActionResult.success(results.isEmpty()
                ? "No runtime reset providers were registered."
                : String.join(" | ", results));
    }

    private static void teleport(ServerPlayerEntity subject, ServerPlayerEntity destination) {
        subject.teleportTo(new TeleportTarget(
                (ServerWorld) destination.getWorld(),
                destination.getPos(),
                Vec3d.ZERO,
                destination.getYaw(),
                destination.getPitch(),
                TeleportTarget.NO_OP));
    }

    private ServerPlayerEntity requirePlayer(Context context, String targetId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(clean(targetId));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid player target.");
        }
        ServerPlayerEntity player = context.admin().getServer().getPlayerManager().getPlayer(uuid);
        if (player == null) throw new IllegalArgumentException("Target player is not online.");
        return player;
    }

    private String providerList() {
        if (providers.isEmpty()) return "No addon providers registered.";
        List<String> titles = providers.stream().map(ElarionAdminPanelProvider::title).toList();
        return String.join(", ", titles);
    }

    private static String tabForAction(String action) {
        if (OPEN_CONFIG_EDITOR_ACTION.equals(action) || VALIDATE_CONFIG_VALUE_ACTION.equals(action)) return "configs";
        if ("runtime_reset_all".equals(action)) return "danger";
        if (action.startsWith("teleport") || action.contains("nickname") || "set_realm".equals(action)
                || action.contains("title") || action.contains("ability")
                || action.contains("character") || action.contains("mount")) {
            return "players";
        }
        return "systems";
    }

    private void emit(Context context, String eventType, UUID subjectId, Map<String, String> metadata) {
        context.api().events().emitDomainEvent(ElarionDomainEvent.of(
                "elarion_core", eventType, context.admin().getUuid(), "",
                "admin-panel", subjectId == null ? "" : subjectId.toString(), metadata));
        context.api().history().record("administration", eventType, context.admin().getUuid(),
                "admin-panel", subjectId == null ? "" : subjectId.toString(), "",
                metadata == null ? Map.of() : metadata);
    }

    private static Map<String, String> safeParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) return Map.of();
        Map<String, String> safe = new LinkedHashMap<>();
        parameters.forEach((key, value) -> safe.put(clean(key), clean(value)));
        return Map.copyOf(safe);
    }

    private void requireApi() {
        if (api == null) throw new IllegalStateException("Admin panel service is not bound to Core API.");
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static List<String> realmSuggestions(Context context) {
        return context.api().realms().all().stream()
                .map(RealmDefinition::id)
                .sorted()
                .toList();
    }

    private static String realmPlaceholder(Context context) {
        return realmSuggestions(context).stream().findFirst().orElse("realm1");
    }

    private static List<String> titleSuggestions(Context context) {
        return context.api().titles().all().stream()
                .map(panetina.elarion.core.model.TitleDefinition::id)
                .sorted()
                .toList();
    }

    private static String titlePlaceholder(Context context) {
        return titleSuggestions(context).stream().findFirst().orElse("citizen");
    }

    private static List<String> abilitySuggestions(Context context) {
        return context.api().abilities().registeredAbilities().stream()
                .sorted()
                .toList();
    }

    private static String abilityPlaceholder(Context context) {
        return abilitySuggestions(context).stream().findFirst().orElse("elarion.example.ability");
    }

    public record Context(ElarionApi api, ServerPlayerEntity admin) {
    }

    public record ActionResult(boolean success, String message) {
        public static ActionResult success(String message) {
            return new ActionResult(true, clean(message).isBlank() ? "Done." : clean(message));
        }

        public static ActionResult failure(String message) {
            return new ActionResult(false, clean(message).isBlank() ? "Action failed." : clean(message));
        }
    }

    record ConfigEditOpenResult(boolean success, String message, ElarionConfigEditControl control) {
        static ConfigEditOpenResult success(String message, ElarionConfigEditControl control) {
            return new ConfigEditOpenResult(true, clean(message).isBlank() ? "Done." : clean(message), control);
        }

        static ConfigEditOpenResult failure(String message) {
            return new ConfigEditOpenResult(false, clean(message).isBlank() ? "Action failed." : clean(message), null);
        }
    }

    private record ConfigEntryTarget(String domainId, String categoryId, String entryId) {
        private static ConfigEntryTarget parse(String targetId) {
            String[] parts = clean(targetId).split("\\|", -1);
            if (parts.length != 4 || !"config-entry".equals(parts[0])) return null;
            return new ConfigEntryTarget(parts[1], parts[2], parts[3]);
        }
    }

    private record ConfigCategoryTarget(String domainId, String categoryId) {
        private static ConfigCategoryTarget from(String targetId) {
            ConfigEntryTarget entry = ConfigEntryTarget.parse(targetId);
            if (entry != null) return new ConfigCategoryTarget(entry.domainId(), entry.categoryId());
            String[] parts = clean(targetId).split(":", -1);
            if (parts.length == 4 && "config".equals(parts[0]) && "category".equals(parts[2])) {
                return new ConfigCategoryTarget(parts[1], parts[3]);
            }
            return new ConfigCategoryTarget("", "");
        }

        private boolean matches(String domainId, String categoryId) {
            return !this.domainId.isBlank()
                    && this.domainId.equals(clean(domainId))
                    && this.categoryId.equals(clean(categoryId));
        }
    }
}
