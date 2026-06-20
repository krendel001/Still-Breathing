package com.knockoutmod.menu;

public final class KnockoutLootLayout {
    public static final int IMAGE_WIDTH = 176;
    private static final int CHEST_TOP_PADDING = 18;
    private static final int PLAYER_GAP = 12;
    private static final int HOTBAR_OFFSET = 58;

    private KnockoutLootLayout() {
    }

    public static Layout forContainerRows(int containerRows) {
        int playerInventoryY = CHEST_TOP_PADDING + containerRows * 18 + PLAYER_GAP;
        int playerHotbarY = playerInventoryY + HOTBAR_OFFSET;
        int imageHeight = 114 + containerRows * 18;
        int inventoryLabelY = imageHeight - 94;
        return new Layout(containerRows, inventoryLabelY, playerInventoryY, playerHotbarY, imageHeight);
    }

    public static int containerSlotY(int row) {
        return CHEST_TOP_PADDING + row * 18;
    }

    public record Layout(
            int containerRows,
            int inventoryLabelY,
            int playerInventoryY,
            int playerHotbarY,
            int imageHeight
    ) {
    }
}
