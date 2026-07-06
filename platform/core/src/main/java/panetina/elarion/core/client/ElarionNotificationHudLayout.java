package panetina.elarion.core.client;

import panetina.elarion.core.client.ui.ElarionUiMetrics;
import panetina.elarion.core.client.ui.ElarionUiTypography;

final class ElarionNotificationHudLayout {
    static final int SCREEN_MARGIN = 6;
    static final int RAIL_WIDTH = 30;
    static final int RAIL_PANEL_GAP = 1;
    static final int PANEL_X = RAIL_WIDTH + RAIL_PANEL_GAP;
    static final int PANEL_WIDTH = 196;
    static final int LOGICAL_WIDTH = RAIL_WIDTH + RAIL_PANEL_GAP + PANEL_WIDTH;
    static final int LOGICAL_HEIGHT = 236;
    static final int MAX_PANEL_HEIGHT = 220;
    static final int DRAWER_HEADER_HEIGHT = 24;
    static final int CLOSE_SIZE = 12;
    static final int LIST_MARGIN = 6;
    static final int LIST_TOP = DRAWER_HEADER_HEIGHT + 4;
    static final int LIST_BOTTOM_MARGIN = 6;
    static final int EMPTY_CARD_HEIGHT = 48;
    static final int BASE_ROW_HEIGHT = 34;
    static final int CLAIM_HEIGHT = 14;

    private ElarionNotificationHudLayout() {
    }

    static int listX() {
        return PANEL_X + LIST_MARGIN;
    }

    static int closeX() {
        return PANEL_X + PANEL_WIDTH - CLOSE_SIZE - 6;
    }

    static int listWidth() {
        return PANEL_WIDTH - LIST_MARGIN * 2;
    }

    static int rowHeight() {
        return Math.max(BASE_ROW_HEIGHT, 8 + ElarionUiTypography.lineHeight() * 2);
    }

    static int actionHeaderHeight() {
        return ElarionUiTypography.lineHeight() + 8;
    }

    static int actionButtonHeight() {
        return ElarionUiMetrics.controlHeight(CLAIM_HEIGHT, 4);
    }

    static int boundedDetailDrawerHeight(
            int contentTop,
            int contentHeight,
            int actionBandHeight,
            int selectedPointerBottom
    ) {
        int desired = contentTop + Math.max(1, contentHeight) + 4
                + Math.max(0, actionBandHeight) + LIST_BOTTOM_MARGIN;
        return Math.min(MAX_PANEL_HEIGHT, Math.max(desired, selectedPointerBottom));
    }

    static boolean railPointerVisible(boolean selected, int pointerCenterY, int drawerHeight) {
        return selected && pointerCenterY >= 0 && pointerCenterY < drawerHeight;
    }

    static Metrics metrics() {
        return new Metrics(
                RAIL_WIDTH,
                PANEL_X,
                PANEL_WIDTH,
                DRAWER_HEADER_HEIGHT,
                closeX(),
                6,
                CLOSE_SIZE,
                listX(),
                LIST_TOP,
                listWidth(),
                EMPTY_CARD_HEIGHT,
                rowHeight(),
                actionHeaderHeight(),
                actionButtonHeight(),
                MAX_PANEL_HEIGHT);
    }

    record Metrics(
            int railWidth,
            int panelX,
            int panelWidth,
            int drawerHeaderHeight,
            int closeX,
            int closeY,
            int closeSize,
            int listX,
            int listTop,
            int listWidth,
            int emptyCardHeight,
            int rowHeight,
            int actionHeaderHeight,
            int actionButtonHeight,
            int maxPanelHeight
    ) {
        int panelRight() {
            return panelX + panelWidth;
        }

        int closeCenterX() {
            return closeX + closeSize / 2;
        }

        int closeCenterY() {
            return closeY + closeSize / 2;
        }

        int headerCenterY() {
            return drawerHeaderHeight / 2;
        }

        int listRight() {
            return listX + listWidth;
        }

        int minimumEmptyDrawerHeight() {
            return listTop + emptyCardHeight + LIST_BOTTOM_MARGIN;
        }
    }
}
