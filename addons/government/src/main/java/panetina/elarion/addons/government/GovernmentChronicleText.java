package panetina.elarion.addons.government;

import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.ChronicleRenderContext;
import panetina.elarion.core.model.ChronicleRenderer;
import panetina.elarion.core.model.ChronicleTemplate;
import panetina.elarion.core.model.ChronicleTemplateFamily;
import panetina.elarion.core.model.PublicHistoryEntry;
import panetina.elarion.core.service.ChronicleRendererRegistry;
import panetina.elarion.core.service.ChronicleVariantSelector;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class GovernmentChronicleText implements ChronicleRenderer {
    public static final GovernmentChronicleText INSTANCE = new GovernmentChronicleText();
    private static final ChronicleVariantSelector VARIANT_SELECTOR = new ChronicleVariantSelector();
    private static final ChronicleTemplateFamily PROPOSAL_APPROVED_FAMILY = new ChronicleTemplateFamily(
            "government.proposal-approved",
            "government",
            Set.of("proposal-approved"),
            "Proposal Approved",
            "Proposal",
            "Approved by authority",
            "A civic proposal was approved and awaits official wording.",
            List.of(
                    new ChronicleTemplate("government.proposal-approved.01",
                            "The {category} \"{title}\" cleared Seat review and awaits final wording."),
                    new ChronicleTemplate("government.proposal-approved.02",
                            "\"{title}\" was approved by the Seat of Rule as a {category}."),
                    new ChronicleTemplate("government.proposal-approved.03",
                            "Authority holders approved \"{title}\" for the Realm's {category} records."),
                    new ChronicleTemplate("government.proposal-approved.04",
                            "The Seat accepted \"{title}\" and moved the {category} toward publication."),
                    new ChronicleTemplate("government.proposal-approved.05",
                            "\"{title}\" passed authority review and awaits its official text."),
                    new ChronicleTemplate("government.proposal-approved.06",
                            "The Realm's authority approved the {category} proposal \"{title}\"."),
                    new ChronicleTemplate("government.proposal-approved.07",
                            "\"{title}\" earned approval from the Seat of Rule."),
                    new ChronicleTemplate("government.proposal-approved.08",
                            "The {category} proposal \"{title}\" advanced from review to final wording."),
                    new ChronicleTemplate("government.proposal-approved.09",
                            "Authority review ended in favor of \"{title}\"."),
                    new ChronicleTemplate("government.proposal-approved.10",
                            "\"{title}\" was accepted into the civic record queue as a {category}.")
            ),
            Set.of("title"),
            Set.of("category"));
    private static final ChronicleTemplateFamily PROPOSAL_REJECTED_FAMILY = new ChronicleTemplateFamily(
            "government.proposal-rejected",
            "government",
            Set.of("proposal-rejected"),
            "Proposal Rejected",
            "Proposal",
            "Rejected by authority",
            "A civic proposal was rejected by the Seat of Rule.",
            List.of(
                    new ChronicleTemplate("government.proposal-rejected.01",
                            "The Seat of Rule rejected the {category} proposal \"{title}\"."),
                    new ChronicleTemplate("government.proposal-rejected.02",
                            "\"{title}\" failed authority review as a {category}."),
                    new ChronicleTemplate("government.proposal-rejected.03",
                            "Authority holders declined \"{title}\" before it could advance."),
                    new ChronicleTemplate("government.proposal-rejected.04",
                            "The {category} proposal \"{title}\" was turned away by the Seat."),
                    new ChronicleTemplate("government.proposal-rejected.05",
                            "\"{title}\" was rejected and removed from the active review queue."),
                    new ChronicleTemplate("government.proposal-rejected.06",
                            "The Realm's authority refused the {category} proposal \"{title}\"."),
                    new ChronicleTemplate("government.proposal-rejected.07",
                            "\"{title}\" did not survive review by the Seat of Rule."),
                    new ChronicleTemplate("government.proposal-rejected.08",
                            "Review ended against \"{title}\", closing the {category} proposal."),
                    new ChronicleTemplate("government.proposal-rejected.09",
                            "The Seat marked \"{title}\" rejected."),
                    new ChronicleTemplate("government.proposal-rejected.10",
                            "\"{title}\" was denied a place in the civic record queue.")
            ),
            Set.of("title"),
            Set.of("category"));
    private static final ChronicleTemplateFamily CIVIC_RECORD_CREATED_FAMILY = new ChronicleTemplateFamily(
            "government.civic-record-created",
            "government",
            Set.of("civic-record-created"),
            "Civic Record Created",
            "Record",
            "Official record created",
            "A new civic record became active.",
            List.of(
                    new ChronicleTemplate("government.civic-record-created.01",
                            "The {category} \"{title}\" became active in the civic record."),
                    new ChronicleTemplate("government.civic-record-created.02",
                            "\"{title}\" was entered as an active {category}."),
                    new ChronicleTemplate("government.civic-record-created.03",
                            "The Seat published \"{title}\" into the Realm's {category} records."),
                    new ChronicleTemplate("government.civic-record-created.04",
                            "A new {category}, \"{title}\", took effect."),
                    new ChronicleTemplate("government.civic-record-created.05",
                            "\"{title}\" now stands as active civic policy."),
                    new ChronicleTemplate("government.civic-record-created.06",
                            "The Realm added \"{title}\" to its active {category} ledger."),
                    new ChronicleTemplate("government.civic-record-created.07",
                            "\"{title}\" was made official by the Seat of Rule."),
                    new ChronicleTemplate("government.civic-record-created.08",
                            "The civic record opened a new {category}: \"{title}\"."),
                    new ChronicleTemplate("government.civic-record-created.09",
                            "\"{title}\" was published for the Realm to follow."),
                    new ChronicleTemplate("government.civic-record-created.10",
                            "The active record now includes the {category} \"{title}\".")
            ),
            Set.of("title"),
            Set.of("category"));

    private GovernmentChronicleText() {
    }

    public static boolean visibleInArchive(PublicHistoryEntry entry) {
        if (entry == null) return false;
        return !entry.type().equals("vote-cast");
    }

    @Override
    public boolean supports(PublicHistoryEntry entry) {
        return entry != null && "government".equals(entry.category());
    }

    @Override
    public ChronicleProjection render(PublicHistoryEntry entry, ChronicleRenderContext context) {
        String actorName = context == null ? "" : context.actorName();
        return project(entry, actorName);
    }

    public static ChronicleProjection project(PublicHistoryEntry entry, String actorName) {
        if (entry == null) {
            return projection("Government Event", "", "Chronicle", "Government chronicle", "government.event", entry);
        }
        String type = entry.type();
        Map<String, String> metadata = entry.metadata();
        String title = titleFrom(metadata, entry.subjectId());
        String category = categoryLabel(metadata.getOrDefault("category", ""));
        String office = officeLabel(metadata.getOrDefault("office", entry.subjectType().equals("office")
                ? entry.subjectId() : ""));
        String actor = clean(actorName, "Realm Government");
        String text = clean(entry.text(), "A Government event was recorded.");
        return switch (type) {
            case "proposal-created" -> projection(
                    "Ember Proposal Submitted",
                    title.isBlank()
                            ? actor + " submitted a civic proposal."
                            : actor + " submitted \"" + title + "\" as a " + lower(category, "proposal") + ".",
                    categoryOrDefault(category, "Proposal"),
                    "Proposal entered public record", "government.proposal-created", entry);
            case "proposal-approved" -> projectionWithVariant(
                    "Proposal Approved",
                    approvedProposalBody(entry, category),
                    categoryOrDefault(category, "Proposal"),
                    "Approved by authority", approvedProposalVariant(entry), entry);
            case "proposal-rejected" -> projectionWithVariant(
                    "Proposal Rejected",
                    familyBody(entry, PROPOSAL_REJECTED_FAMILY, category),
                    categoryOrDefault(category, "Proposal"),
                    "Rejected by authority", familyVariant(entry, PROPOSAL_REJECTED_FAMILY), entry);
            case "proposal-citizen-ratification-opened" -> projection(
                    "Ember Vote Opened",
                    title.isBlank()
                            ? "A proposal was sent to Embers for ratification."
                            : "\"" + title + "\" was sent to Embers for ratification.",
                    categoryOrDefault(category, "Vote"),
                    "Ember ratification", "government.proposal-ratification-opened", entry);
            case "civic-record-created" -> projectionWithVariant(
                    recordTypeTitle(category) + " Created",
                    familyBody(entry, CIVIC_RECORD_CREATED_FAMILY, category),
                    categoryOrDefault(category, "Record"),
                    "Official record created", familyVariant(entry, CIVIC_RECORD_CREATED_FAMILY), entry);
            case "office-assigned" -> projection(
                    "Office Granted",
                    actor + " was appointed to " + lower(office, "office") + ".",
                    "Office",
                    "Authority changed", "government.office-assigned", entry);
            case "office-removed" -> projection(
                    "Office Removed",
                    actor + " was removed from " + lower(office, "office") + ".",
                    "Office",
                    "Authority changed", "government.office-removed", entry);
            case "leadership-election-reopened" -> projection(
                    "Election Reopened",
                    "The " + lower(office, "leadership") + " election reopened because the office became vacant.",
                    "Election",
                    "Vacancy reopened vote", "government.election-reopened", entry);
            case "founding-nominated" -> projection(
                    "Candidate Nominated",
                    actor + " entered the founding election.",
                    "Election",
                    "Candidate recorded", "government.founding-nominated", entry);
            case "founding-election-resolved", "founding-election-complete" -> projection(
                    "Founding Election Completed",
                    winnersText(metadata, "The founding election completed."),
                    "Election",
                    "Election result recorded", "government.founding-election-resolved", entry);
            case "founding-election-phase-resolved" -> projection(
                    "Election Phase Completed",
                    winnersText(metadata, "A founding election phase completed."),
                    "Election",
                    "Election phase recorded", "government.founding-election-phase-resolved", entry);
            case "realm-name-chosen", "realm-identity-set" -> projection(
                    "Realm Name Chosen",
                    text,
                    "Identity",
                    "Realm identity recorded", "government.realm-name-chosen", entry);
            case "realm-color-chosen", "realm-color-set" -> projection(
                    "Realm Color Chosen",
                    text,
                    "Identity",
                    "Realm color recorded", "government.realm-color-chosen", entry);
            case "government-form-chosen", "form-set" -> projection(
                    "Government Form Chosen",
                    text,
                    "Government",
                    "Government form recorded", "government.form-chosen", entry);
            case "realm-notice-sent" -> projection(
                    "Realm Notice Sent",
                    title.isBlank() ? text : "\"" + title + "\" was published as a Realm notice.",
                    "Notice",
                    "Notice published", "government.realm-notice-sent", entry);
            case "monarchy-succession" -> projection(
                    "Monarch Succeeded",
                    text,
                    "Office",
                    "Succession recorded", "government.monarchy-succession", entry);
            case "monarchy-vacancy", "authority-removed-inactive", "republic-vacancy" -> projection(
                    "Authority Seat Vacated",
                    text,
                    "Office",
                    "Vacancy recorded", "government.authority-vacancy", entry);
            default -> projection(historyTitle(type), text,
                    categoryOrDefault(category, "Chronicle"), historyDetailLabel(type), "government." + type, entry);
        };
    }

    static String historyTitle(String type) {
        if (type == null || type.isBlank()) return "Government Event";
        String friendly = switch (type) {
            case "founding-nominated" -> "Candidate Nominated";
            case "founding-election-phase-resolved" -> "Election Phase Completed";
            case "founding-election-resolved", "founding-election-complete" -> "Founding Election Completed";
            case "proposal-created" -> "Ember Proposal Submitted";
            case "proposal-approved" -> "Proposal Approved";
            case "proposal-rejected" -> "Proposal Rejected";
            case "proposal-citizen-ratification-opened" -> "Ember Vote Opened";
            case "vote-resolved" -> "Founding Vote Resolved";
            case "realm-name-chosen", "realm-identity-set" -> "Realm Name Chosen";
            case "realm-color-chosen", "realm-color-set" -> "Realm Color Chosen";
            case "government-form-chosen", "form-set" -> "Government Form Chosen";
            case "monarchy-succession" -> "Monarch Succeeded";
            case "monarchy-vacancy" -> "Monarchy Became Vacant";
            case "civic-record-created" -> "Civic Record Created";
            default -> "";
        };
        if (!friendly.isBlank()) return friendly;
        String[] parts = type.replace('_', '-').split("-");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (!builder.isEmpty()) builder.append(' ');
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.isEmpty() ? "Government Event" : builder.toString();
    }

    private static String historyDetailLabel(String type) {
        if (type == null) return "Government chronicle";
        return switch (type) {
            case "founding-nominated" -> "Candidate recorded";
            case "founding-election-phase-resolved", "founding-election-resolved", "founding-election-complete" ->
                    "Election result recorded";
            case "proposal-created" -> "Proposal entered public record";
            case "vote-resolved" -> "Winning choice recorded";
            default -> "Government chronicle";
        };
    }

    private static String winnersText(Map<String, String> metadata, String fallback) {
        String winners = metadata.getOrDefault("winners", "").trim();
        if (winners.isBlank()) return fallback;
        return "Embers elected " + winners + " founding authority holder"
                + ("1".equals(winners) ? "." : "s.");
    }

    static ChronicleTemplateFamily proposalApprovedFamily() {
        return PROPOSAL_APPROVED_FAMILY;
    }

    static ChronicleTemplateFamily proposalRejectedFamily() {
        return PROPOSAL_REJECTED_FAMILY;
    }

    static ChronicleTemplateFamily civicRecordCreatedFamily() {
        return CIVIC_RECORD_CREATED_FAMILY;
    }

    private static String approvedProposalBody(PublicHistoryEntry entry, String category) {
        return familyBody(entry, PROPOSAL_APPROVED_FAMILY, category);
    }

    private static String familyBody(PublicHistoryEntry entry, ChronicleTemplateFamily family, String category) {
        Map<String, String> metadata = entry.metadata();
        if (!family.hasRequiredMetadata(entry)) {
            return family.missingContextBody();
        }
        String variantId = familyVariant(entry, family);
        ChronicleTemplate template = family.templateByVariantId(variantId);
        String displayCategory = lower(category, "proposal");
        PublicHistoryEntry renderEntry = new PublicHistoryEntry(
                entry.eventId(),
                entry.timestamp(),
                entry.source(),
                entry.category(),
                entry.type(),
                entry.actorId(),
                entry.subjectType(),
                entry.subjectId(),
                entry.realmId(),
                Map.of("title", metadata.getOrDefault("title", ""), "category", displayCategory),
                entry.text());
        return template.render(renderEntry, ChronicleRenderContext.EMPTY, family.missingContextBody());
    }

    private static String approvedProposalVariant(PublicHistoryEntry entry) {
        return familyVariant(entry, PROPOSAL_APPROVED_FAMILY);
    }

    private static String familyVariant(PublicHistoryEntry entry, ChronicleTemplateFamily family) {
        return VARIANT_SELECTOR.selectVariantId(entry, family);
    }

    private static String titleFrom(Map<String, String> metadata, String fallback) {
        String title = metadata.getOrDefault("title", "").trim();
        return title.isBlank() ? clean(fallback, "") : title;
    }

    private static String recordTypeTitle(String category) {
        if (category == null || category.isBlank()) return "Civic Record";
        return category;
    }

    private static String categoryOrDefault(String category, String fallback) {
        return category == null || category.isBlank() ? fallback : category;
    }

    private static String categoryLabel(String category) {
        String value = clean(category, "");
        if (value.isBlank()) return "";
        return titleCase(value.replace('_', ' '));
    }

    private static String officeLabel(String office) {
        String value = clean(office, "");
        if (value.isBlank()) return "";
        return titleCase(value.replace('_', ' '));
    }

    private static String lower(String value, String fallback) {
        return clean(value, fallback).toLowerCase(Locale.ROOT);
    }

    private static String clean(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String titleCase(String value) {
        String[] parts = value.toLowerCase(Locale.ROOT).split(" ");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (!builder.isEmpty()) builder.append(' ');
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    private static ChronicleProjection projection(
            String title,
            String body,
            String category,
            String detailLabel,
            String family,
            PublicHistoryEntry entry
    ) {
        return new ChronicleProjection(title, body, category, detailLabel,
                ChronicleRendererRegistry.selectedVariantId(entry, family));
    }

    private static ChronicleProjection projectionWithVariant(
            String title,
            String body,
            String category,
            String detailLabel,
            String variantId,
            PublicHistoryEntry entry
    ) {
        return new ChronicleProjection(title, body, category, detailLabel,
                clean(variantId, ChronicleRendererRegistry.selectedVariantId(entry, "government.event")));
    }
}
