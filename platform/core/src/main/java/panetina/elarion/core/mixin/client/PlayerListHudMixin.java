package panetina.elarion.core.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import panetina.elarion.core.client.ClientIdentityCache;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow
    protected abstract List<PlayerListEntry> collectPlayerEntries();

    @Shadow
    public abstract Text getPlayerName(PlayerListEntry entry);

    @Shadow
    protected abstract void renderLatencyIcon(DrawContext context, int width, int x, int y, PlayerListEntry entry);

    @Inject(method = "collectPlayerEntries", at = @At("RETURN"), cancellable = true)
    private void elarion$filterPlayerEntries(CallbackInfoReturnable<List<PlayerListEntry>> cir) {
        List<PlayerListEntry> visible = new ArrayList<>(cir.getReturnValue());
        visible.removeIf(entry -> ClientIdentityCache.shouldHideTabEntry(entry.getProfile().getId()));
        visible.sort(Comparator
                .comparing((PlayerListEntry entry) -> ClientIdentityCache.find(entry.getProfile().getId())
                        .map(identity -> identity.realmName().isBlank() ? "~" : identity.realmName())
                        .orElse("~"))
                .thenComparing(entry -> ClientIdentityCache.find(entry.getProfile().getId())
                        .map(identity -> identity.baseName().toLowerCase(java.util.Locale.ROOT))
                        .orElse(entry.getProfile().getName().toLowerCase(java.util.Locale.ROOT))));
        cir.setReturnValue(visible);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void elarion$renderRealmGroupedTablist(
            DrawContext context,
            int scaledWindowWidth,
            Scoreboard scoreboard,
            ScoreboardObjective objective,
            CallbackInfo ci
    ) {
        if (objective != null) {
            return;
        }
        List<PlayerListEntry> entries = collectPlayerEntries();
        if (!shouldRenderRealmHeaders(entries)) {
            return;
        }

        ci.cancel();
        renderRealmGrouped(context, scaledWindowWidth, entries);
    }

    private boolean shouldRenderRealmHeaders(List<PlayerListEntry> entries) {
        if (entries.isEmpty()) return false;
        return entries.stream().anyMatch(entry -> ClientIdentityCache.find(entry.getProfile().getId())
                .map(identity -> !identity.realmName().isBlank())
                .orElse(false));
    }

    private void renderRealmGrouped(DrawContext context, int scaledWindowWidth, List<PlayerListEntry> entries) {
        List<TabRow> rows = groupedRows(entries);
        if (rows.isEmpty()) return;

        int rowWidth = calculateRowWidth(rows);
        int columns = 1;
        int rowsPerColumn = rows.size();
        while (rowsPerColumn > 20) {
            columns++;
            rowsPerColumn = (rows.size() + columns - 1) / columns;
        }
        int totalWidth = columns * rowWidth + (columns - 1) * 5;
        int left = scaledWindowWidth / 2 - totalWidth / 2;
        int top = 10;
        int background = client.options.getTextBackgroundColor(0x55332211);
        int headerBackground = client.options.getTextBackgroundColor(0xAA3A2412);
        int headerBorder = 0xCCB0792C;
        int textColor = 0xFFFFFFFF;
        int headerColor = 0xFFFFD37A;

        for (int index = 0; index < rows.size(); index++) {
            int column = index / rowsPerColumn;
            int row = index % rowsPerColumn;
            int x = left + column * (rowWidth + 5);
            int y = top + row * 9;
            TabRow tabRow = rows.get(index);
            if (tabRow.header() != null) {
                renderHeaderRow(context, tabRow.header(), x, y, rowWidth, headerBackground, headerBorder, headerColor);
            } else {
                renderPlayerRow(context, tabRow.entry(), x, y, rowWidth, background, textColor);
            }
        }
    }

    private List<TabRow> groupedRows(List<PlayerListEntry> entries) {
        List<TabRow> rows = new ArrayList<>();
        String currentRealm = null;
        for (PlayerListEntry entry : entries) {
            String realm = ClientIdentityCache.find(entry.getProfile().getId())
                    .map(identity -> identity.realmName().isBlank() ? "Wilderness" : identity.realmName())
                    .orElse("Wilderness");
            if (!Objects.equals(currentRealm, realm)) {
                currentRealm = realm;
                rows.add(TabRow.header(realm));
            }
            rows.add(TabRow.player(entry));
        }
        return rows;
    }

    private int calculateRowWidth(List<TabRow> rows) {
        int width = 80;
        for (TabRow row : rows) {
            if (row.header() != null) {
                width = Math.max(width, client.textRenderer.getWidth(row.header()) + 12);
            } else {
                width = Math.max(width, client.textRenderer.getWidth(getPlayerName(row.entry())) + 24);
            }
        }
        return Math.min(width, 160);
    }

    private void renderHeaderRow(
            DrawContext context,
            String title,
            int x,
            int y,
            int width,
            int background,
            int border,
            int color
    ) {
        context.fill(x, y, x + width, y + 8, background);
        context.fill(x, y, x + width, y + 1, border);
        context.fill(x, y + 7, x + width, y + 8, border);
        int textX = x + width / 2 - client.textRenderer.getWidth(title) / 2;
        context.drawTextWithShadow(client.textRenderer, title, textX, y, color);
    }

    private void renderPlayerRow(
            DrawContext context,
            PlayerListEntry entry,
            int x,
            int y,
            int width,
            int background,
            int color
    ) {
        context.fill(x, y, x + width, y + 8, background);
        int nameX = x + 1;
        if (!client.isInSingleplayer() && !client.getNetworkHandler().getConnection().isEncrypted()) {
            // Match vanilla: player heads are only shown in singleplayer or encrypted sessions.
        } else {
            boolean upsideDown = entry.getProfile().getName().equals("Dinnerbone")
                    || entry.getProfile().getName().equals("Grumm");
            PlayerSkinDrawer.draw(context, entry.getSkinTextures().texture(), x, y, 8, true, upsideDown);
            nameX += 9;
        }
        Text name = getPlayerName(entry);
        int nameWidth = client.textRenderer.getWidth(name);
        int pingX = Math.min(x + width - 13, nameX + nameWidth + 8);
        pingX = Math.max(pingX, nameX + 16);
        int maxNameWidth = Math.max(8, pingX - nameX - 3);
        if (nameWidth <= maxNameWidth) {
            context.drawTextWithShadow(client.textRenderer, name, nameX, y,
                    entry.getGameMode() == GameMode.SPECTATOR ? 0x90FFFFFF : color);
        } else {
            String trimmed = client.textRenderer.trimToWidth(name.getString(), Math.max(1, maxNameWidth - 6)) + "...";
            context.drawTextWithShadow(client.textRenderer, trimmed, nameX, y,
                    entry.getGameMode() == GameMode.SPECTATOR ? 0x90FFFFFF : color);
        }
        renderLatencyIcon(context, Math.max(13, pingX - x + 13), x, y, entry);
    }

    private record TabRow(String header, PlayerListEntry entry) {
        static TabRow header(String title) {
            return new TabRow(title, null);
        }

        static TabRow player(PlayerListEntry entry) {
            return new TabRow(null, entry);
        }
    }
}
