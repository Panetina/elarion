package panetina.elarion.addons.offerings.service;

import panetina.elarion.addons.offerings.model.OfferingAnchor;
import panetina.elarion.addons.offerings.model.OfferingInstance;
import panetina.elarion.addons.offerings.model.OfferingProgress;
import panetina.elarion.addons.offerings.model.OfferingProjectDefinition;
import panetina.elarion.addons.offerings.model.OfferingProjectLevel;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionProtocol.Visibility;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionPublisher;
import panetina.elarion.core.integration.minecraft.WebsiteMapMarker;

import java.util.LinkedHashMap;
import java.util.Map;

public final class OfferingWebProjectionPublisher {
    private final OfferingService service;
    private final OfferingDefinitionService definitions;
    private final MinecraftProjectionPublisher projections;

    public OfferingWebProjectionPublisher(
            OfferingService service,
            OfferingDefinitionService definitions,
            MinecraftProjectionPublisher projections
    ) {
        this.service = service;
        this.definitions = definitions;
        this.projections = projections;
    }

    public void publishSnapshot() {
        service.instances().forEach(instance -> publish(new OfferingService.Change(
                OfferingService.ChangeType.UPSERT, instance)));
    }

    public void publish(OfferingService.Change change) {
        OfferingInstance instance = change.instance();
        boolean active = change.type() != OfferingService.ChangeType.DELETE;
        OfferingProjectDefinition project = definitions.find(instance.projectId()).orElse(null);
        if (project == null) return;

        OfferingProjectLevel level = project.level(instance.activeLevelId()).orElse(project.firstLevel());
        String label = instance.displayNameOverride().isBlank() ? level.displayName() : instance.displayNameOverride();
        Map<String, String> payload = active
                ? progressPayload(instance, project, level, label)
                : Map.of(
                        "label", label,
                        "projectId", project.id(),
                        "levelId", level.id(),
                        "value", "0",
                        "required", "0",
                        "percent", "0",
                        "displayValue", "Removed",
                        "description", level.description(),
                        "status", "removed");
        projections.publishState("metric.shrine-contribution", instance.id(), instance.realmId(),
                Visibility.PUBLIC, payload);

        OfferingAnchor anchor = active && !instance.anchorId().isBlank()
                ? service.findAnchor(instance.anchorId()).orElse(null)
                : null;
        if (anchor != null) {
            projections.publishMapMarker(marker(instance, anchor, label, true));
        } else if (change.type() == OfferingService.ChangeType.DELETE || instance.anchorId().isBlank()) {
            String worldId = instance.worldId().isBlank() ? "minecraft:overworld" : instance.worldId();
            projections.publishMapMarker(new WebsiteMapMarker(
                    "shrine", instance.id(), instance.realmId(), label, worldId,
                    instance.x(), instance.y(), instance.z(), Visibility.PUBLIC, false,
                    Map.of("projectId", instance.projectId(), "status", "removed")));
        }
    }

    private Map<String, String> progressPayload(
            OfferingInstance instance,
            OfferingProjectDefinition project,
            OfferingProjectLevel level,
            String label
    ) {
        OfferingProgress progress = service.progress(instance.id());
        long current = progress.rows().stream().mapToLong(row -> Math.min(row.current(), row.required())).sum();
        long required = progress.rows().stream().mapToLong(OfferingProgress.Row::required).sum();
        long percent = required <= 0 ? (progress.complete() ? 100 : 0) : Math.min(100, current * 100 / required);
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("label", label);
        payload.put("projectId", project.id());
        payload.put("levelId", level.id());
        payload.put("value", Long.toString(current));
        payload.put("required", Long.toString(required));
        payload.put("percent", Long.toString(percent));
        payload.put("displayValue", progress.complete() ? "Complete" : percent + "% funded");
        payload.put("description", level.description());
        payload.put("status", progress.complete() ? "complete" : "active");
        return Map.copyOf(payload);
    }

    private static WebsiteMapMarker marker(
            OfferingInstance instance,
            OfferingAnchor anchor,
            String label,
            boolean active
    ) {
        return new WebsiteMapMarker(
                "shrine", instance.id(), instance.realmId(), label, anchor.worldId(),
                anchor.x(), anchor.y(), anchor.z(), Visibility.PUBLIC, active,
                Map.of(
                        "projectId", instance.projectId(),
                        "status", instance.completed() ? "complete" : "active"));
    }
}
