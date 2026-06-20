package com.knockoutmod.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class KnockoutHudRenderer {
    public static final int BAR_WIDTH = 182;
    public static final int BAR_HEIGHT = 5;

    private KnockoutHudRenderer() {
    }

    public static int secondsRemaining(int progress, int required) {
        return Math.max(0, (required - progress + 19) / 20);
    }

    public static void drawProgressBar(
            GuiGraphics graphics,
            Font font,
            int screenWidth,
            int centerY,
            Component title,
            float ratio,
            int fillColor,
            Component hint
    ) {
        int fill = Math.max(1, (int) (BAR_WIDTH * ratio));
        int x = (screenWidth - BAR_WIDTH) / 2;

        drawCenteredString(graphics, font, screenWidth, centerY - 12, title, 0xFFFFFF);
        graphics.fill(x, centerY, x + BAR_WIDTH, centerY + BAR_HEIGHT, 0xAA000000);
        graphics.fill(x, centerY, x + fill, centerY + BAR_HEIGHT, fillColor);

        if (hint != null) {
            drawCenteredString(graphics, font, screenWidth, centerY + 8, hint, 0xCCCCCC);
        }
    }

    public static void drawCenteredString(
            GuiGraphics graphics,
            Font font,
            int screenWidth,
            int y,
            Component text,
            int color
    ) {
        graphics.drawString(font, text, (screenWidth - font.width(text)) / 2, y, color, true);
    }
}
