package panetina.elarion.addons.guilds.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.addons.guilds.model.GuildPermission;
import panetina.elarion.addons.guilds.network.GuildScreenActionPayload;
import panetina.elarion.addons.guilds.network.GuildDonationPayload;
import panetina.elarion.addons.guilds.network.GuildScreenOpenPayload;
import panetina.elarion.addons.guilds.network.GuildUiFeedbackPayload;
import panetina.elarion.core.client.ui.ElarionCivicColors;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionPixelCanvas32;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionTextInput;
import panetina.elarion.core.client.ui.ElarionUiIcons;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Bounded Guild management projection; every mutation remains server-authoritative. */
public final class GuildScreen extends ElarionScreen {
    private static final int LOGICAL_WIDTH = 680;
    private static final int LOGICAL_HEIGHT = 420;
    private static final int BODY_X = 18;
    private static final int BODY_Y = 96;
    private static final int BODY_WIDTH = 644;
    private static final int BODY_HEIGHT = 276;
    private static final int FOOTER_Y = 382;
    private static final int TAB_Y = 62;
    private static final int TAB_WIDTH = 101;
    private static final int TAB_GAP = 5;

    private GuildScreenOpenPayload payload;
    private final ElarionPixelCanvas32 iconCanvas = new ElarionPixelCanvas32();
    private final ElarionTextInput announcement = new ElarionTextInput(500, false);
    private final ElarionTextInput donation = new ElarionTextInput(10, false);
    private final ElarionTextInput roleId = new ElarionTextInput(24, false);
    private final ElarionTextInput roleName = new ElarionTextInput(96, false);
    private final EnumSet<GuildPermission> selectedPermissions = EnumSet.noneOf(GuildPermission.class);
    private final List<Hit> hits = new ArrayList<>();
    private Tab tab = Tab.OVERVIEW;
    private Input input = Input.NONE;
    private ElarionScaledLayout layout;
    private ElarionUiStyle style;
    private String feedback = "";
    private boolean feedbackError;
    private int scroll;

    public GuildScreen(GuildScreenOpenPayload payload) {
        super(Text.literal("Guild"));
        this.payload = payload;
        iconCanvas.load(payload.iconPixels());
    }

    public boolean belongsTo(GuildScreenOpenPayload update) {
        return payload.guildId().equals(update.guildId());
    }

    public void updatePayload(GuildScreenOpenPayload update) {
        payload = update;
        iconCanvas.load(update.iconPixels());
        feedback = "Changes saved.";
        feedbackError = false;
        clampScroll();
    }

    public void feedback(GuildUiFeedbackPayload update) {
        feedback = update.message();
        feedbackError = !update.successful();
    }

    @Override
    protected void init() {
        style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        layout = ElarionScaledLayout.fit(width, height, LOGICAL_WIDTH, LOGICAL_HEIGHT, 8, 70, 1.0F);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hits.clear();
        context.fill(0, 0, width, height, style.backgroundOverlayColor());
        double lx = layout.logicalX(mouseX);
        double ly = layout.logicalY(mouseY);
        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);

        ElarionCivicUi.attachedShell(context, 0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT, 54);
        renderHeader(context);
        renderTabs(context, lx, ly);
        ElarionCivicUi.thinBox(context, BODY_X, BODY_Y, BODY_WIDTH, BODY_HEIGHT,
                ElarionCivicColors.ROOT_SURFACE, ElarionCivicColors.GOLD_BORDER);
        renderBody(context, lx, ly);
        renderFooter(context);
        context.getMatrices().pop();
    }

    private void renderHeader(DrawContext context) {
        iconCanvas.render(context, 18, 11, 1);
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, payload.displayName(), 330),
                60, 13, style.titleColor(), true);
        ElarionUiTypography.draw(context, textRenderer,
                (payload.secret() ? "Secret Guild" : "Public Guild") + "  |  " + payload.members().size() + " members",
                60, 31, style.mutedColor(), false);
        ElarionUiTypography.drawRight(context, textRenderer, "Tag " + payload.tag(), 632, 20,
                style.mutedColor(), false);
        ElarionCivicUi.closeButton(context, 646, 14, 16);
        hits.add(new Hit(646, 14, 16, 16, this::close));
    }

    private void renderTabs(DrawContext context, double mouseX, double mouseY) {
        int x = BODY_X;
        for (Tab value : Tab.values()) {
            boolean hovered = inside(mouseX, mouseY, x, TAB_Y, TAB_WIDTH, 24);
            ElarionUiRenderer.tab(context, textRenderer, x, TAB_Y, TAB_WIDTH, 24,
                    value.label, value == tab, hovered, ElarionUiThemes.variant("default"));
            Tab selected = value;
            hits.add(new Hit(x, TAB_Y, TAB_WIDTH, 24, () -> selectTab(selected)));
            x += TAB_WIDTH + TAB_GAP;
        }
    }

    private void renderBody(DrawContext context, double mouseX, double mouseY) {
        switch (tab) {
            case OVERVIEW -> renderOverview(context, mouseX, mouseY);
            case MEMBERS -> renderMembers(context, mouseX, mouseY);
            case ANNOUNCEMENTS -> renderAnnouncements(context, mouseX, mouseY);
            case INVITATIONS -> renderInvitations(context, mouseX, mouseY);
            case ROLES -> renderRoles(context, mouseX, mouseY);
            case EMBLEM -> renderEmblem(context, mouseX, mouseY);
        }
    }

    private void renderOverview(DrawContext context, double mouseX, double mouseY) {
        sectionTitle(context, "Guild Overview", "guild", 34, 112);
        card(context, 34, 142, 286, 76, "Leader", memberName(payload.leaderId()), "Canonical Guild authority");
        card(context, 340, 142, 286, 76, "Visibility", payload.secret() ? "Secret" : "Public",
                payload.secret() ? "Hidden from public projections" : "Visible in approved projections");
        card(context, 34, 232, 286, 76, "Guild Level", "Level " + payload.level(),
                payload.totalContributed() + " Sigils contributed");
        String next = payload.nextLevelContribution() == 0L ? "Maximum level" : "Next: " + payload.nextLevelContribution() + " Sigils";
        card(context, 340, 232, 286, 76, "Members", payload.members().size() + " / " + payload.memberCapacity(), next);
        renderInput(context, donation, "Sigils to donate", 34, 326, 258, Input.DONATION);
        boolean validDonation = donation.text().trim().matches("[1-9][0-9]*");
        button(context, mouseX, mouseY, 302, 326, 156, 24, "Donate Sigils", validDonation,
                ElarionCivicUi.Tone.PRIMARY,
                this::donate);
        if (client != null && client.player != null && !payload.leaderId().equals(client.player.getUuid())) {
            button(context, mouseX, mouseY, 494, 326, 132, 24, "Leave Guild", true,
                    ElarionCivicUi.Tone.DESTRUCTIVE, () -> send("leave", null, "", new byte[0]));
        }
    }

    private void renderMembers(DrawContext context, double mouseX, double mouseY) {
        sectionTitle(context, "Members", "members", 34, 112);
        List<GuildScreenOpenPayload.Member> visible = window(payload.members(), 10);
        int y = 140;
        for (GuildScreenOpenPayload.Member member : visible) {
            boolean leader = payload.leaderId().equals(member.id());
            ElarionCivicUi.rowSurface(context, 34, y, 592, 20, false, false, true);
            String joined = "Joined " + java.time.Instant.ofEpochMilli(member.joinedAt())
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            ElarionUiTypography.draw(context, textRenderer,
                    ElarionUiRenderer.ellipsize(textRenderer, member.name() + "  |  " + joined, 278), 44, y + 6,
                    leader ? style.titleColor() : style.textColor(), false);
            ElarionUiTypography.draw(context, textRenderer, leader ? "Leader" : roleLabel(member.role()),
                    334, y + 6, style.mutedColor(), false);
            if (!leader && can(GuildPermission.ASSIGN_ROLES) && canManage(member)) {
                String next = nextAssignableRole(member.role());
                if (!next.isBlank()) button(context, mouseX, mouseY, 486, y + 1, 130, 18,
                        "Set " + roleLabel(next), true, ElarionCivicUi.Tone.NORMAL,
                        () -> send("assign_role", member.id(), next, new byte[0]));
            }
            y += 22;
        }
        renderRange(context, payload.members().size(), 10);
    }

    private void renderAnnouncements(DrawContext context, double mouseX, double mouseY) {
        sectionTitle(context, "Announcements", "announcement", 34, 112);
        List<GuildScreenOpenPayload.Announcement> visible = window(payload.announcements(), 7);
        int y = 140;
        if (visible.isEmpty()) muted(context, "No Guild announcements yet.", 44, y + 6);
        for (GuildScreenOpenPayload.Announcement entry : visible) {
            ElarionCivicUi.rowSurface(context, 34, y, 592, 24, false, false, true);
            ElarionUiTypography.draw(context, textRenderer,
                    ElarionUiRenderer.ellipsize(textRenderer, entry.author() + ": " + entry.body(), 570),
                    44, y + 8, style.textColor(), false);
            y += 27;
        }
        if (can(GuildPermission.PUBLISH_ANNOUNCEMENTS)) {
            renderInput(context, announcement, "Write an announcement", 34, 326, 462, Input.ANNOUNCEMENT);
            button(context, mouseX, mouseY, 506, 326, 120, 24, "Publish", !announcement.text().trim().isBlank(),
                    ElarionCivicUi.Tone.PRIMARY,
                    () -> send("publish_announcement", null, announcement.text().trim(), new byte[0]));
        }
    }

    private void renderInvitations(DrawContext context, double mouseX, double mouseY) {
        sectionTitle(context, "Invite eligible players", "invite", 34, 112);
        if (!can(GuildPermission.INVITE)) {
            muted(context, "Your Guild role cannot issue invitations.", 44, 150);
            return;
        }
        List<GuildScreenOpenPayload.InviteCandidate> visible = window(payload.inviteCandidates(), 9);
        int y = 140;
        if (visible.isEmpty()) muted(context, "No eligible online characters are available.", 44, y + 6);
        for (GuildScreenOpenPayload.InviteCandidate candidate : visible) {
            ElarionCivicUi.rowSurface(context, 34, y, 592, 22, false, false, true);
            ElarionUiTypography.draw(context, textRenderer,
                    ElarionUiRenderer.ellipsize(textRenderer, candidate.name(), 430), 44, y + 7,
                    style.textColor(), false);
            button(context, mouseX, mouseY, 516, y + 2, 100, 18, "Invite", true,
                    ElarionCivicUi.Tone.PRIMARY,
                    () -> send("invite", candidate.id(), "", new byte[0]));
            y += 24;
        }
        renderRange(context, payload.inviteCandidates().size(), 9);
    }

    private void renderRoles(DrawContext context, double mouseX, double mouseY) {
        sectionTitle(context, "Roles and permissions", "roles", 34, 112);
        int y = 140;
        for (GuildScreenOpenPayload.Role role : payload.roles().stream().limit(8).toList()) {
            ElarionCivicUi.rowSurface(context, 34, y, 292, 22, false, false, true);
            ElarionUiTypography.draw(context, textRenderer,
                    ElarionUiRenderer.ellipsize(textRenderer, "#" + role.position() + " " + role.displayName(), 110), 44, y + 7,
                    style.titleColor(), false);
            ElarionUiTypography.draw(context, textRenderer,
                    ElarionUiRenderer.ellipsize(textRenderer, String.join(", ", role.permissions()), 150),
                    164, y + 7, style.mutedColor(), false);
            y += 24;
        }
        if (!can(GuildPermission.MANAGE_ROLES)) {
            muted(context, "Only authorized roles can create new Guild roles.", 350, 150);
            return;
        }
        renderInput(context, roleId, "Role ID", 350, 140, 128, Input.ROLE_ID);
        renderInput(context, roleName, "Display name", 488, 140, 138, Input.ROLE_NAME);
        int index = 0;
        for (GuildPermission permission : GuildPermission.values()) {
            int px = 350 + (index % 2) * 138;
            int py = 178 + (index / 2) * 28;
            boolean selected = selectedPermissions.contains(permission);
            button(context, mouseX, mouseY, px, py, 128, 22,
                    (selected ? "[x] " : "[ ] ") + friendly(permission), true,
                    selected ? ElarionCivicUi.Tone.PRIMARY : ElarionCivicUi.Tone.NORMAL,
                    () -> toggle(permission));
            index++;
        }
        boolean valid = !roleId.text().trim().isBlank() && !roleName.text().trim().isBlank();
        button(context, mouseX, mouseY, 488, 326, 138, 24, "Create Role", valid,
                ElarionCivicUi.Tone.PRIMARY, () -> createRole());
    }

    private void renderEmblem(DrawContext context, double mouseX, double mouseY) {
        sectionTitle(context, "Guild Emblem", "guild", 34, 112);
        if (!can(GuildPermission.REDRAW_ICON)) {
            muted(context, "Your Guild role cannot redraw the emblem.", 44, 150);
            iconCanvas.render(context, 420, 144, 4);
            return;
        }
        iconCanvas.render(context, 42, 142, 6);
        iconCanvas.renderPalette(context, 262, 150, 22);
        muted(context, "Choose a colour, then paint the 32 x 32 canvas.", 262, 204);
        button(context, mouseX, mouseY, 262, 238, 142, 24, "Clear canvas", true,
                ElarionCivicUi.Tone.MUTED, iconCanvas::clear);
        button(context, mouseX, mouseY, 262, 272, 142, 24, "Save emblem", true,
                ElarionCivicUi.Tone.PRIMARY,
                () -> send("redraw_icon", null, "", iconCanvas.pixels()));
    }

    private void renderFooter(DrawContext context) {
        String text = feedback.isBlank()
                ? "All changes are requests; the server validates membership, permissions, and revisions."
                : feedback;
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, text, 630), 24, FOOTER_Y + 7,
                feedback.isBlank() ? style.mutedColor() : feedbackError ? style.errorColor() : style.feedbackColor(),
                false);
    }

    private void sectionTitle(DrawContext context, String title, String icon, int x, int y) {
        ElarionUiIcons.drawOrDefault(context, icon, x, y, 18);
        ElarionUiTypography.draw(context, textRenderer, title, x + 26, y + 4, style.titleColor(), false);
    }

    private void card(DrawContext context, int x, int y, int width, int height,
                      String label, String value, String detail) {
        ElarionCivicUi.headerShell(context, x, y, width, height, 24);
        ElarionUiTypography.draw(context, textRenderer, label.toUpperCase(java.util.Locale.ROOT),
                x + 10, y + 8, style.mutedColor(), false);
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, value, width - 20), x + 10, y + 34,
                style.titleColor(), false);
        muted(context, ElarionUiRenderer.ellipsize(textRenderer, detail, width - 20), x + 10, y + 54);
    }

    private void renderInput(DrawContext context, ElarionTextInput value, String placeholder,
                             int x, int y, int width, Input target) {
        ElarionCivicUi.thinBox(context, x, y, width, 24, ElarionCivicColors.MESSAGE_BODY,
                input == target ? ElarionCivicColors.ACTIVE_GREEN : ElarionCivicColors.GOLD_SHADOW);
        String text = value.text().isBlank() ? placeholder : value.text();
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, text, width - 14), x + 7, y + 8,
                value.text().isBlank() ? style.mutedColor() : style.textColor(), false);
        if (input == target && value.caretVisible()) {
            int caretX = x + 7 + Math.min(width - 14,
                    ElarionUiTypography.width(textRenderer, ElarionUiRenderer.ellipsize(textRenderer,
                            value.text(), width - 14)));
            context.fill(caretX, y + 6, caretX + 1, y + 18, style.titleColor());
        }
        hits.add(new Hit(x, y, width, 24, () -> focus(target)));
    }

    private void button(DrawContext context, double mouseX, double mouseY,
                        int x, int y, int width, int height, String label, boolean active,
                        ElarionCivicUi.Tone tone, Runnable action) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        ElarionCivicUi.compactActionButton(context, textRenderer, x, y, width, height,
                label, hovered, false, active, active ? tone : ElarionCivicUi.Tone.MUTED, style);
        if (active) hits.add(new Hit(x, y, width, height, action));
    }

    private void send(String action, UUID target, String value, byte[] pixels) {
        feedback = "Submitting request...";
        feedbackError = false;
        ClientPlayNetworking.send(new GuildScreenActionPayload(action, target,
                value == null ? "" : value, pixels == null ? new byte[0] : pixels));
    }

    private void createRole() {
        String permissions = selectedPermissions.stream().map(Enum::name).sorted().collect(Collectors.joining(","));
        send("create_role", null, roleId.text().trim() + "\n" + roleName.text().trim() + "\n" + permissions,
                new byte[0]);
    }

    private void toggle(GuildPermission permission) {
        if (!selectedPermissions.remove(permission)) selectedPermissions.add(permission);
    }

    private boolean can(GuildPermission permission) {
        return payload.viewerPermissions().contains(permission.name());
    }

    private String memberName(UUID id) {
        return payload.members().stream().filter(member -> member.id().equals(id)).findFirst()
                .map(GuildScreenOpenPayload.Member::name).orElse("Unknown");
    }

    private String roleLabel(String roleId) {
        if ("owner".equals(roleId)) return "Leader";
        return payload.roles().stream().filter(role -> role.id().equals(roleId)).findFirst()
                .map(GuildScreenOpenPayload.Role::displayName).orElse(roleId);
    }

    private String nextAssignableRole(String currentRole) {
        int viewerPosition = viewerPosition();
        List<String> roles = payload.roles().stream()
                .filter(role -> !"owner".equals(role.id()) && role.position() > viewerPosition)
                .sorted(java.util.Comparator.comparingInt(GuildScreenOpenPayload.Role::position))
                .map(GuildScreenOpenPayload.Role::id).toList();
        if (roles.isEmpty()) return "";
        int index = roles.indexOf(currentRole);
        return roles.get((index + 1 + roles.size()) % roles.size());
    }

    private void donate() {
        try {
            ClientPlayNetworking.send(new GuildDonationPayload(UUID.randomUUID(), Long.parseLong(donation.text().trim())));
        } catch (NumberFormatException exception) {
            feedback = "Enter a whole positive Sigil amount.";
            feedbackError = true;
        }
    }

    private boolean canManage(GuildScreenOpenPayload.Member member) {
        return viewerPosition() < rolePosition(member.role());
    }

    private int viewerPosition() {
        if (client != null && client.player != null && payload.leaderId().equals(client.player.getUuid())) return 1;
        if (client == null || client.player == null) return Integer.MAX_VALUE;
        return payload.members().stream().filter(member -> member.id().equals(client.player.getUuid()))
                .findFirst().map(member -> rolePosition(member.role())).orElse(Integer.MAX_VALUE);
    }

    private int rolePosition(String roleId) {
        return payload.roles().stream().filter(role -> role.id().equals(roleId)).findFirst()
                .map(GuildScreenOpenPayload.Role::position).orElse(Integer.MAX_VALUE);
    }

    private static String friendly(GuildPermission permission) {
        String text = permission.name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private void renderRange(DrawContext context, int total, int visible) {
        if (total <= visible) return;
        int first = Math.min(total, scroll + 1);
        int last = Math.min(total, scroll + visible);
        ElarionUiTypography.drawRight(context, textRenderer, first + "-" + last + " / " + total,
                626, 350, style.mutedColor(), false);
    }

    private <T> List<T> window(List<T> values, int visible) {
        int first = Math.min(scroll, Math.max(0, values.size() - visible));
        return values.subList(first, Math.min(values.size(), first + visible));
    }

    private int itemCount() {
        return switch (tab) {
            case MEMBERS -> payload.members().size();
            case ANNOUNCEMENTS -> payload.announcements().size();
            case INVITATIONS -> payload.inviteCandidates().size();
            default -> 0;
        };
    }

    private int visibleRows() {
        return switch (tab) {
            case MEMBERS -> 10;
            case ANNOUNCEMENTS -> 7;
            case INVITATIONS -> 9;
            default -> 1;
        };
    }

    private void clampScroll() {
        scroll = Math.max(0, Math.min(scroll, Math.max(0, itemCount() - visibleRows())));
    }

    private void selectTab(Tab selected) {
        tab = selected;
        scroll = 0;
        focus(Input.NONE);
        feedback = "";
    }

    private void focus(Input target) {
        input = target;
        announcement.focused(target == Input.ANNOUNCEMENT);
        donation.focused(target == Input.DONATION);
        roleId.focused(target == Input.ROLE_ID);
        roleName.focused(target == Input.ROLE_NAME);
    }

    private ElarionTextInput focusedInput() {
        return switch (input) {
            case ANNOUNCEMENT -> announcement;
            case DONATION -> donation;
            case ROLE_ID -> roleId;
            case ROLE_NAME -> roleName;
            case NONE -> null;
        };
    }

    private void muted(DrawContext context, String text, int x, int y) {
        ElarionUiTypography.draw(context, textRenderer, text, x, y, style.mutedColor(), false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        double lx = layout.logicalX(mouseX);
        double ly = layout.logicalY(mouseY);
        if (tab == Tab.EMBLEM && can(GuildPermission.REDRAW_ICON)) {
            if (iconCanvas.click((int) lx, (int) ly, 42, 142, 6)
                    || iconCanvas.selectPalette((int) lx, (int) ly, 262, 150, 22)) return true;
        }
        for (int index = hits.size() - 1; index >= 0; index--) {
            Hit hit = hits.get(index);
            if (inside(lx, ly, hit.x, hit.y, hit.width, hit.height)) {
                hit.action.run();
                return true;
            }
        }
        focus(Input.NONE);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount == 0.0D) return false;
        scroll += verticalAmount < 0.0D ? 1 : -1;
        clampScroll();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        ElarionTextInput focused = focusedInput();
        if (focused == null) return true;
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            focused.backspace();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_V && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 && client != null) {
            focused.append(client.keyboard.getClipboard());
            return true;
        }
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        ElarionTextInput focused = focusedInput();
        return focused != null && focused.type(chr);
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    private enum Tab {
        OVERVIEW("Overview"), MEMBERS("Members"), ANNOUNCEMENTS("News"),
        INVITATIONS("Invites"), ROLES("Roles"), EMBLEM("Emblem");
        private final String label;
        Tab(String label) { this.label = label; }
    }

    private enum Input { NONE, ANNOUNCEMENT, DONATION, ROLE_ID, ROLE_NAME }

    private record Hit(int x, int y, int width, int height, Runnable action) { }
}
