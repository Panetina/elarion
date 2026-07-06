package panetina.elarion.addons.government.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;

import java.util.Locale;

public final class GovernmentUiGlyphs {
    public static final int ACTIVE_GREEN = 0xFF62C987;
    public static final int ACTIVE_GREEN_SHADOW = 0xFF1F5A38;
    public static final int REJECT_RED = 0xFFE05A47;
    public static final int REJECT_RED_SHADOW = 0xFF6E251F;
    public static final int WARM_CARD = 0xFF0E0704;
    public static final int WARM_CARD_SOFT = 0xFF171008;
    public static final int WARM_CARD_HOVER = 0xFF25170A;
    public static final int WARM_CARD_SELECTED = 0xFF111A12;
    public static final int WARM_BUTTON = 0xFF3F260F;
    public static final int WARM_BUTTON_HOVER = 0xFF5B3714;
    public static final int WARM_BUTTON_DISABLED = 0xFF1D160F;
    public static final int GOLD_BORDER = 0xFFD19B42;
    public static final int GOLD_SHADOW = 0xFF5B3513;
    public static final int GOLD_HIGHLIGHT = 0xFFFFD878;
    public static final int ROOT_SURFACE = 0xFF090503;
    public static final int ROOT_GRID = 0x24241910;

    private GovernmentUiGlyphs() {
    }

    public static void selectedFrame(DrawContext context, int x, int y, int width, int height) {
        context.fill(x + 1, y, x + width - 1, y + 1, ACTIVE_GREEN);
        context.fill(x, y + 1, x + 1, y + height - 1, ACTIVE_GREEN);
        context.fill(x + 1, y + height - 1, x + width - 1, y + height, ACTIVE_GREEN_SHADOW);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, ACTIVE_GREEN_SHADOW);
    }

    public static void rootSurface(DrawContext context, int x, int y, int width, int height, ElarionUiStyle style) {
        context.fill(x + 4, y + 4, x + width - 4, y + height - 4, ROOT_SURFACE);
        for (int gx = x + 14; gx < x + width - 8; gx += 14) {
            context.fill(gx, y + 64, gx + 1, y + height - 12, ROOT_GRID);
        }
        for (int gy = y + 72; gy < y + height - 10; gy += 14) {
            context.fill(x + 8, gy, x + width - 8, gy + 1, ROOT_GRID);
        }
        context.fill(x + 6, y + 6, x + width - 6, y + 7, 0x55FFD878);
        context.fill(x + 6, y + height - 7, x + width - 6, y + height - 6, GOLD_SHADOW);
        context.fill(x + width - 7, y + 6, x + width - 6, y + height - 6, GOLD_SHADOW);
    }

    public static void civicShell(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int headerHeight,
            ElarionUiStyle style
    ) {
        int bodyY = y + headerHeight - 1;
        thinBox(context, x, y, width, headerHeight, 0xFF130C06, GOLD_BORDER);
        thinBox(context, x, bodyY, width, height - headerHeight + 1, ROOT_SURFACE, GOLD_BORDER);
        context.fill(x + 2, bodyY, x + width - 2, bodyY + 1, GOLD_SHADOW);
        context.fill(x + 6, y + 5, x + width - 6, y + 6, 0x40FFD878);
        for (int gx = x + 16; gx < x + width - 8; gx += 14) {
            context.fill(gx, bodyY + 10, gx + 1, y + height - 10, ROOT_GRID);
        }
        for (int gy = bodyY + 16; gy < y + height - 10; gy += 14) {
            context.fill(x + 8, gy, x + width - 8, gy + 1, ROOT_GRID);
        }
    }

    public static void thinBox(DrawContext context, int x, int y, int width, int height, int fill, int border) {
        context.fill(x, y, x + width, y + height, fill);
        context.fill(x + 1, y, x + width - 1, y + 1, border);
        context.fill(x, y + 1, x + 1, y + height - 1, border);
        context.fill(x + 1, y + height - 1, x + width - 1, y + height, GOLD_SHADOW);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, GOLD_SHADOW);
    }

    public static void iconFrame(DrawContext context, int x, int y, int size, String iconId, ElarionUiStyle style) {
        thinBox(context, x, y, size, size, 0xFF160D06, GOLD_BORDER);
        context.fill(x + 3, y + 3, x + size - 3, y + 4, 0x33201910);
        icon(context, x + 6, y + 6, Math.max(8, size - 12), iconId, style);
    }

    public static void icon(DrawContext context, int x, int y, int size, String iconId, ElarionUiStyle style) {
        GovernmentUiIcons.identifier(iconId).ifPresentOrElse(
                texture -> context.drawTexture(texture, x, y, size, size, 0, 0, 16, 16, 16, 16),
                () -> draw(context, x, y, size, iconId, style)
        );
    }

    public static void crest(DrawContext context, int x, int y, int size, String iconId, ElarionUiStyle style) {
        icon(context, x, y, size, iconId, style);
    }

    public static void headerCard(
            DrawContext context,
            TextRenderer renderer,
            int x,
            int y,
            int width,
            int height,
            String iconId,
            String label,
            int textColor,
            ElarionUiStyle style
    ) {
        sectionBox(context, x, y, width, height, style);
        icon(context, x + 8, y + Math.max(3, (height - 14) / 2), 14, iconId, style);
        String visible = ElarionUiRenderer.ellipsize(renderer, label, Math.max(1, width - 32));
        ElarionUiTypography.draw(context, renderer, visible, x + 28, y + Math.max(4, (height - 8) / 2), textColor, false);
    }

    public static void colorCard(
            DrawContext context,
            TextRenderer renderer,
            int x,
            int y,
            int width,
            int height,
            String label,
            int color,
        ElarionUiStyle style
    ) {
        sectionBox(context, x, y, width, height, style);
        int swatchY = y + Math.max(4, (height - 16) / 2);
        ElarionUiRenderer.beveledBox(context, x + 8, swatchY, 16, 16, style.insetColor(), GOLD_SHADOW, style);
        context.fill(x + 12, swatchY + 4, x + 20, swatchY + 12, color);
        context.fill(x + 12, swatchY + 4, x + 20, swatchY + 5, 0x66FFFFFF);
        context.fill(x + 12, swatchY + 11, x + 20, swatchY + 12, 0x66000000);
        String visible = ElarionUiRenderer.ellipsize(renderer, label, Math.max(1, width - 36));
        ElarionUiTypography.draw(context, renderer, visible, x + 31, y + Math.max(4, (height - 8) / 2), style.textColor(), false);
    }

    public static void rowBox(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            boolean selected,
            boolean hovered,
            boolean muted,
            ElarionUiStyle style
    ) {
        int fill = muted ? WARM_BUTTON_DISABLED : selected ? WARM_CARD_SELECTED : hovered ? WARM_CARD_HOVER : WARM_CARD_SOFT;
        int border = selected ? ACTIVE_GREEN : GOLD_BORDER;
        int shadow = selected ? ACTIVE_GREEN_SHADOW : GOLD_SHADOW;
        context.fill(x, y, x + width, y + height, fill);
        context.fill(x + 1, y, x + width - 1, y + 1, border);
        context.fill(x, y + 1, x + 1, y + height - 1, border);
        context.fill(x + 1, y + height - 1, x + width - 1, y + height, shadow);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, shadow);
        context.fill(x + 2, y + 2, x + width - 2, y + 3, selected ? 0x2631B26A : 0x16FFD878);
        if (hovered && !selected) context.fill(x + 2, y + 3, x + width - 2, y + 4, 0x18D19B42);
        if (selected) context.fill(x + 2, y + 3, x + 4, y + height - 3, ACTIVE_GREEN);
    }

    public static void sectionBox(DrawContext context, int x, int y, int width, int height, ElarionUiStyle style) {
        thinBox(context, x, y, width, height, WARM_CARD, GOLD_BORDER);
    }

    public static void tag(
            DrawContext context,
            TextRenderer renderer,
            int x,
            int y,
            String label,
            boolean active,
            ElarionUiStyle style
    ) {
        tag(context, renderer, x, y, label, active, 116, style);
    }

    public static void tag(
            DrawContext context,
            TextRenderer renderer,
            int x,
            int y,
            String label,
            boolean active,
            int maxWidth,
            ElarionUiStyle style
    ) {
        int safeMax = Math.max(18, maxWidth);
        String visible = ElarionUiRenderer.ellipsize(renderer, displayTag(label), Math.max(1, safeMax - 10));
        int width = Math.min(safeMax, tagWidth(renderer, visible));
        int border = active ? tagColor(label) : 0xFF746450;
        int fill = active ? tagFill(label) : 0x8815100B;
        context.fill(x, y, x + width, y + 10, fill);
        context.fill(x, y, x + 2, y + 10, border);
        context.fill(x + 2, y, x + width, y + 1, active ? tagShadow(label) : GOLD_SHADOW);
        ElarionUiTypography.draw(context, renderer, visible, x + 5, y + 1, active ? tagTextColor(label) : style.mutedColor(), false);
    }

    public static int tagWidth(TextRenderer renderer, String label) {
        return Math.max(24, ElarionUiTypography.width(renderer, label == null ? "" : label) + 10);
    }

    public static int tagColor(String label) {
        return switch (normalizedTag(label)) {
            case "citizen proposal", "proposal", "active", "settled", "passed", "open", "proposed" -> ACTIVE_GREEN;
            case "security", "reject", "rejected" -> 0xFFE36A5A;
            case "economy", "law", "laws", "rule", "rules", "pending", "current" -> GOLD_BORDER;
            case "infrastructure", "project", "projects", "government proposal" -> 0xFF6EA7E8;
            case "culture", "notice", "notices", "faith" -> 0xFFB17CE8;
            case "locked", "waiting" -> 0xFF8A8172;
            default -> 0xFF62B8C9;
        };
    }

    public static String civicIdentityLabel(String formLabel, String realmName) {
        String realm = cleanRealmName(realmName);
        String form = cleanFormLabel(formLabel);
        if (form.isBlank() || "Unchosen".equalsIgnoreCase(form)) return realm.isBlank() ? "Realm" : realm;
        String realmLower = realm.toLowerCase(Locale.ROOT);
        String formLower = form.toLowerCase(Locale.ROOT);
        if (realmLower.equals(formLower) || realmLower.startsWith(formLower + " of ")
                || realmLower.startsWith(formLower + " ")) {
            return realm;
        }
        return form + " of " + (realm.isBlank() ? "Realm" : realm);
    }

    public static void actionButton(
            DrawContext context,
            TextRenderer renderer,
            int x,
            int y,
            int width,
            int height,
            String label,
            boolean hovered,
            boolean active,
            boolean primary,
            ElarionUiStyle style
    ) {
        int fill = !active ? WARM_BUTTON_DISABLED
                : primary ? hovered ? 0xFF235A2F : 0xFF1B4624
                : hovered ? WARM_BUTTON_HOVER : WARM_BUTTON;
        int border = !active ? GOLD_SHADOW : primary ? ACTIVE_GREEN : GOLD_BORDER;
        ElarionUiRenderer.beveledBox(context, x, y, width, height, fill, border, style);
        context.fill(x + 3, y + 2, x + width - 3, y + 3,
                active ? primary ? 0x4462C987 : 0x44FFD878 : 0x225B3513);
        context.fill(x + 3, y + height - 4, x + width - 3, y + height - 3,
                active ? 0x55201108 : 0x44201108);
        String visible = ElarionUiRenderer.ellipsize(renderer, label, Math.max(1, width - 10));
        int color = active ? style.textColor() : style.mutedColor();
        ElarionUiTypography.draw(context, renderer, visible, x + Math.max(4, (width - ElarionUiTypography.width(renderer, visible)) / 2),
                y + Math.max(3, (height - 8) / 2), color, false);
    }

    public static void progressRow(
            DrawContext context,
            TextRenderer renderer,
            int x,
            int y,
            int width,
            String label,
            long value,
            long total,
            int color,
            boolean selected,
            boolean selectable,
            ElarionUiStyle style
    ) {
        long safeTotal = Math.max(1, total);
        float ratio = Math.min(1.0F, Math.max(0.0F, value / (float) safeTotal));
        int trackX = x + (selectable ? 24 : 10);
        int trackWidth = GovernmentUiComponents.voteTrackWidth(width, selectable);
        int fill = Math.round(trackWidth * ratio);
        int optionFill = selected ? WARM_CARD_SELECTED : 0xFF120B06;
        int optionBorder = selected ? color : 0xCC5B3513;
        context.fill(x, y, x + width, y + GovernmentUiComponents.VOTE_OPTION_HEIGHT, optionFill);
        context.fill(x, y, x + width, y + 1, optionBorder);
        context.fill(x, y, x + 1, y + GovernmentUiComponents.VOTE_OPTION_HEIGHT, optionBorder);
        context.fill(x, y + GovernmentUiComponents.VOTE_OPTION_HEIGHT - 1,
                x + width, y + GovernmentUiComponents.VOTE_OPTION_HEIGHT, 0xAA3B210E);
        context.fill(x + width - 1, y, x + width, y + GovernmentUiComponents.VOTE_OPTION_HEIGHT, 0xAA3B210E);
        context.fill(x + 2, y + 2, x + width - 2, y + 3, selected ? 0x2231B26A : 0x16FFD878);
        if (selectable) {
            int boxColor = selected ? color : 0xFF8A6A3A;
            context.fill(x + 8, y + 8, x + 18, y + 18, 0xFF080504);
            context.fill(x + 8, y + 8, x + 18, y + 9, boxColor);
            context.fill(x + 8, y + 8, x + 9, y + 18, boxColor);
            context.fill(x + 8, y + 17, x + 18, y + 18, 0xFF3B210E);
            context.fill(x + 17, y + 8, x + 18, y + 18, 0xFF3B210E);
            if (selected) icon(context, x + 9, y + 9, 8, "settled", style);
        }
        String count = value + (selectable ? " (" + Math.round(ratio * 100.0F) + "%)" : " / " + safeTotal);
        int textX = x + (selectable ? 24 : 10);
        ElarionUiTypography.draw(context, renderer, ElarionUiRenderer.ellipsize(renderer, label, width - 112),
                textX, y + 6, color, false);
        ElarionUiTypography.draw(context, renderer, count, x + width - 8 - ElarionUiTypography.width(renderer, count),
                y + 6, style.textColor(), false);
        context.fill(trackX, y + 20, trackX + trackWidth, y + 24, 0xFF050302);
        context.fill(trackX, y + 20, trackX + trackWidth, y + 21, 0xFF1B1008);
        if (fill > 0) {
            int fillRight = trackX + Math.min(trackWidth - 1, Math.max(1, fill));
            context.fill(trackX + 1, y + 21, fillRight, y + 23, color);
            context.fill(trackX + 1, y + 21, fillRight, y + 22, 0x44FFFFFF);
        }
    }

    public static void draw(DrawContext context, int x, int y, int size, String iconId, ElarionUiStyle style) {
        String icon = iconId == null || iconId.isBlank() ? "proposal" : iconId;
        int gold = style.titleColor();
        int dark = style.bevelShadowColor();
        int green = ACTIVE_GREEN;
        int red = REJECT_RED;
        int midX = x + size / 2;
        int midY = y + size / 2;
        switch (icon) {
            case "civic_crest", "realm_name" -> {
                context.fill(x + size / 4, y + size / 5, x + size * 3 / 4, y + size * 4 / 5, dark);
                context.fill(x + size / 3, y + size / 4, x + size * 2 / 3, y + size * 3 / 4, gold);
                context.fill(midX - 2, y + 2, midX + 3, y + size / 2, gold);
            }
            case "seat_crest", "office", "leader_election" -> {
                context.fill(x + 2, y + size / 3, x + size - 2, y + size / 2, gold);
                context.fill(x + 5, y + size / 5, x + 8, y + size / 3, gold);
                context.fill(midX - 2, y + 2, midX + 3, y + size / 3, gold);
                context.fill(x + size - 8, y + size / 5, x + size - 5, y + size / 3, gold);
                context.fill(x + 4, y + size * 2 / 3, x + size - 4, y + size * 5 / 6, dark);
            }
            case "law" -> {
                context.fill(x + 4, y + 3, midX, y + size - 3, gold);
                context.fill(midX + 1, y + 3, x + size - 4, y + size - 3, gold);
                context.fill(midX, y + 4, midX + 1, y + size - 4, dark);
            }
            case "proposal" -> {
                context.fill(x + 4, y + 4, x + size - 5, y + size - 3, gold);
                context.fill(x + 7, y + 7, x + size - 8, y + size - 6, style.insetColor());
                context.fill(x + size - 8, y + 4, x + size - 5, y + size - 8, dark);
                context.fill(x + 9, y + 10, x + size - 10, y + 12, dark);
                context.fill(x + 9, y + 15, x + size - 12, y + 17, dark);
            }
            case "archive", "history" -> {
                context.fill(x + 4, y + 3, x + size - 4, y + 6, gold);
                context.fill(x + 6, y + 6, x + size - 6, y + size - 6, dark);
                context.fill(x + 4, y + size - 6, x + size - 4, y + size - 3, gold);
                context.fill(midX - 2, midY - 2, midX + 3, midY + 3, gold);
            }
            case "people", "current_votes" -> {
                context.fill(midX - 3, y + 4, midX + 4, y + 10, gold);
                context.fill(midX - 5, y + 11, midX + 6, y + size - 4, gold);
                context.fill(x + 3, y + 8, x + 8, y + 13, dark);
                context.fill(x + 2, y + 14, x + 9, y + size - 5, dark);
                context.fill(x + size - 8, y + 8, x + size - 3, y + 13, dark);
                context.fill(x + size - 9, y + 14, x + size - 2, y + size - 5, dark);
            }
            case "government_form" -> {
                context.fill(x + 4, y + size - 5, x + size - 4, y + size - 2, gold);
                context.fill(x + 5, y + 4, x + size - 5, y + 7, gold);
                context.fill(x + 7, y + 7, x + 10, y + size - 5, gold);
                context.fill(midX - 2, y + 7, midX + 3, y + size - 5, gold);
                context.fill(x + size - 10, y + 7, x + size - 7, y + size - 5, gold);
            }
            case "approve", "settled" -> {
                context.fill(x + 3, midY, x + size / 3, midY + 3, green);
                context.fill(x + size / 3, midY + 3, x + size - 3, midY + 6, green);
                context.fill(x + size - 6, midY - 4, x + size - 3, midY + 6, green);
            }
            case "reject" -> {
                context.fill(x + 4, y + 4, x + 7, y + 7, red);
                context.fill(x + 7, y + 7, x + size - 4, y + size - 4, red);
                context.fill(x + size - 7, y + 4, x + size - 4, y + 7, red);
                context.fill(x + 4, y + size - 7, x + 7, y + size - 4, red);
            }
            case "realm_color" -> {
                context.fill(x + 4, y + 4, midX, y + size - 4, green);
                context.fill(midX, y + 4, x + size - 4, y + size - 4, gold);
            }
            case "economy" -> {
                context.fill(x + 5, y + 6, x + size - 5, y + size - 4, gold);
                context.fill(x + 7, y + 4, x + size - 7, y + 7, green);
                context.fill(midX - 1, y + 7, midX + 2, y + size - 5, dark);
            }
            case "security" -> {
                context.fill(midX - 2, y + 3, midX + 3, y + size - 4, gold);
                context.fill(x + 4, midY - 2, x + size - 4, midY + 3, dark);
                context.fill(x + 5, y + size - 6, x + size - 5, y + size - 3, gold);
            }
            case "infrastructure", "project" -> {
                context.fill(x + 4, y + size - 7, x + size - 4, y + size - 3, dark);
                context.fill(x + 7, y + 5, x + 11, y + size - 5, gold);
                context.fill(x + size - 11, y + 5, x + size - 7, y + size - 5, gold);
                context.fill(x + 6, midY - 2, x + size - 6, midY + 2, gold);
            }
            case "culture", "notice" -> {
                context.fill(x + 5, y + 4, x + size - 5, y + size - 4, 0xFF8E64D9);
                context.fill(x + 8, y + 7, x + size - 8, y + size - 7, style.insetColor());
                context.fill(midX - 2, y + 8, midX + 3, y + size - 8, gold);
            }
            case "timer" -> {
                context.fill(x + 5, y + 3, x + size - 5, y + 6, gold);
                context.fill(midX - 2, y + 6, midX + 3, y + size - 5, gold);
                context.fill(x + 5, y + size - 6, x + size - 5, y + size - 3, gold);
            }
            default -> {
                context.fill(x + 4, y + 3, x + size - 4, y + size - 3, gold);
                context.fill(x + 7, y + 6, x + size - 7, y + size - 6, style.insetColor());
                context.fill(x + 9, y + 9, x + size - 9, y + 11, dark);
                context.fill(x + 9, y + 14, x + size - 9, y + 16, dark);
            }
        }
    }

    private static String displayTag(String label) {
        String clean = label == null ? "" : label.trim().replace('_', ' ');
        if (clean.isBlank()) return "Info";
        String lower = clean.toLowerCase(Locale.ROOT);
        return switch (lower) {
            case "realm project" -> "Infrastructure";
            case "civic rule" -> "Rule";
            default -> clean;
        };
    }

    private static int tagFill(String label) {
        return switch (normalizedTag(label)) {
            case "citizen proposal", "proposal", "active", "settled", "passed", "open", "proposed" -> 0xCC142017;
            case "security", "reject", "rejected" -> 0xCC2A1412;
            case "economy", "law", "laws", "rule", "rules", "pending", "current" -> 0xCC24190D;
            case "infrastructure", "project", "projects", "government proposal" -> 0xCC121D2C;
            case "culture", "notice", "notices", "faith" -> 0xCC22162F;
            case "locked", "waiting" -> 0xCC1D1812;
            default -> 0xCC122329;
        };
    }

    private static int tagTextColor(String label) {
        int color = tagColor(label);
        return color == GOLD_BORDER ? GOLD_HIGHLIGHT : color;
    }

    private static int tagShadow(String label) {
        return switch (normalizedTag(label)) {
            case "citizen proposal", "proposal", "active", "settled", "passed", "open", "proposed" -> ACTIVE_GREEN_SHADOW;
            case "security", "reject", "rejected" -> REJECT_RED_SHADOW;
            case "infrastructure", "project", "projects", "government proposal" -> 0xFF25425D;
            case "culture", "notice", "notices", "faith" -> 0xFF422B5E;
            case "locked", "waiting" -> 0xFF3A332A;
            default -> GOLD_SHADOW;
        };
    }

    private static String normalizedTag(String label) {
        return displayTag(label).toLowerCase(Locale.ROOT);
    }

    private static String cleanRealmName(String realmName) {
        String clean = realmName == null ? "" : realmName.trim().replaceAll("\\s+", " ");
        if (clean.toLowerCase(Locale.ROOT).startsWith("realm of ")) return clean.substring(9).trim();
        return clean;
    }

    private static String cleanFormLabel(String formLabel) {
        String clean = formLabel == null ? "" : formLabel.trim().replaceAll("\\s+", " ");
        return clean.equalsIgnoreCase("Unchosen") ? "" : clean;
    }
}
