package panetina.elarion.addons.security;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.AddonConfigFiles;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.command.CommandOutput;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

public final class ElarionSecurityAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_security");

    @Override
    public void initialize(ElarionApi api) {
        AddonConfigFiles.writeDefault("security", "security.yml", """
                config-version: 1

                # Security starts evidence-first. Do not automatically punish until rules are tested.
                evidence:
                  enabled: true
                  retain-days: 30

                anti-cheat:
                  enabled: false
                  movement-checks: true
                  interaction-rate-checks: true
                  reach-checks: true
                  packet-spam-checks: true

                anti-afk-farms:
                  enabled: false
                  ender-pearl-chunk-loaders: true
                  unattended-mob-farms: true
                  redstone-overload-zones: true
                  repeated-action-loops: true

                actions:
                  admin-warn-threshold: 10
                  auto-kick-threshold: 0
                """);
        api.system().abilities().register("elarion.security.admin");
        SecurityEvidenceService evidence = new SecurityEvidenceService(LOGGER);
        evidence.register();
        api.system().commands().registerAdminSubcommand(() -> securityCommand(evidence));
        api.system().commands().registerHelpDescription("/e security status", "Show Elarion security evidence status.");
        LOGGER.info("Elarion Security addon shell initialized");
    }

    private static LiteralArgumentBuilder<ServerCommandSource> securityCommand(SecurityEvidenceService evidence) {
        return literal("security")
                .requires(source -> source.hasPermissionLevel(4))
                .then(literal("status").executes(context -> sendStatus(context.getSource(), evidence)));
    }

    private static int sendStatus(ServerCommandSource source, SecurityEvidenceService evidence) {
        Map<String, String> diagnostics = evidence.diagnostics();
        CommandOutput.header(source, "Security Evidence");
        CommandOutput.kv(source, "State", diagnostics.getOrDefault("state", "unknown"));
        CommandOutput.kv(source, "Total evidence", diagnostics.getOrDefault("totalEvidence", "0"));
        CommandOutput.kv(source, "Dirty", diagnostics.getOrDefault("dirty", "false"));
        CommandOutput.kv(source, "Evidence types", diagnostics.getOrDefault("types", "(none)"));
        CommandOutput.kv(source, "Last evidence", diagnostics.getOrDefault("lastEvidenceAt", "never"));
        return 1;
    }
}
