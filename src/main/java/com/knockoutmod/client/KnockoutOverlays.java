package com.knockoutmod.client;

import com.knockoutmod.KnockoutMod;
import com.knockoutmod.entity.KnockoutTestDummyEntity;
import com.knockoutmod.network.GiveUpKnockoutPacket;
import com.knockoutmod.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = KnockoutMod.MOD_ID, value = Dist.CLIENT)
public final class KnockoutOverlays {
    private static final int GIVE_UP_BUTTON_WIDTH = 140;
    private static final int GIVE_UP_BUTTON_HEIGHT = 20;

    private static int giveUpButtonX;
    private static int giveUpButtonY;
    private static int giveUpButtonRight;
    private static int giveUpButtonBottom;

    private KnockoutOverlays() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        if (ReviveClientState.isActive()) {
            renderAllyReviveProgress(minecraft, graphics, screenWidth, screenHeight);
            return;
        }

        if (KnockoutClientState.isKnockedOut(minecraft.player)) {
            renderSelfReviveHud(minecraft, graphics, screenWidth, screenHeight);
            renderGiveUpButton(minecraft, graphics, screenWidth, screenHeight);
            return;
        }

        renderReviveHint(minecraft, graphics, screenWidth, screenHeight);
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (event.getAction() != GLFW.GLFW_PRESS || event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || minecraft.screen != null
                || !KnockoutClientState.isKnockedOut(minecraft.player)
                || !isMouseOverGiveUpButton(minecraft)) {
            return;
        }

        ModNetwork.CHANNEL.sendToServer(new GiveUpKnockoutPacket());
        event.setCanceled(true);
    }

    private static void renderAllyReviveProgress(
            Minecraft minecraft,
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight
    ) {
        int centerY = screenHeight / 2 + 28;
        Component title = Component.translatable(
                "knockoutmod.overlay.reviving",
                ReviveClientState.getVictimName(),
                Math.round(ReviveClientState.getRatio() * 100.0F)
        );
        Component hint = Component.translatable(
                "knockoutmod.overlay.reviving_hint",
                KnockoutHudRenderer.secondsRemaining(
                        ReviveClientState.getProgress(),
                        ReviveClientState.getRequired()
                )
        );
        KnockoutHudRenderer.drawProgressBar(
                graphics,
                minecraft.font,
                screenWidth,
                centerY,
                title,
                ReviveClientState.getRatio(),
                0xFF55CC55,
                hint
        );
    }

    private static void renderSelfReviveHud(
            Minecraft minecraft,
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight
    ) {
        int centerY = screenHeight / 2 + 18;

        if (SelfReviveClientState.isActive()) {
            Component title = Component.translatable(
                    "knockoutmod.overlay.self_revive_progress",
                    Math.round(SelfReviveClientState.getRatio() * 100.0F)
            );
            Component hint = Component.translatable(
                    "knockoutmod.overlay.self_revive_hold",
                    KnockoutHudRenderer.secondsRemaining(
                            SelfReviveClientState.getProgress(),
                            SelfReviveClientState.getRequired()
                    )
            );
            KnockoutHudRenderer.drawProgressBar(
                    graphics,
                    minecraft.font,
                    screenWidth,
                    centerY,
                    title,
                    SelfReviveClientState.getRatio(),
                    0xFFCCAA33,
                    hint
            );
            return;
        }

        KnockoutHudRenderer.drawCenteredString(
                graphics,
                minecraft.font,
                screenWidth,
                centerY,
                Component.translatable("knockoutmod.overlay.self_revive_hint"),
                0xFFFFFF
        );
        KnockoutHudRenderer.drawCenteredString(
                graphics,
                minecraft.font,
                screenWidth,
                centerY + 12,
                Component.translatable("knockoutmod.overlay.self_revive_warning"),
                0xFFAA5555
        );
    }

    private static void renderReviveHint(
            Minecraft minecraft,
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight
    ) {
        Entity target = ReviveTargetHelper.getCrosshairKnockoutTarget(minecraft);
        if (target == null) {
            return;
        }

        Component hint = target instanceof KnockoutTestDummyEntity
                ? Component.translatable("knockoutmod.overlay.revive_hint_dummy")
                : Component.translatable("knockoutmod.overlay.revive_hint_player");
        KnockoutHudRenderer.drawCenteredString(
                graphics,
                minecraft.font,
                screenWidth,
                screenHeight / 2 + 12,
                hint,
                0xFFFFFF
        );
    }

    private static void renderGiveUpButton(
            Minecraft minecraft,
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight
    ) {
        giveUpButtonX = (screenWidth - GIVE_UP_BUTTON_WIDTH) / 2;
        giveUpButtonY = screenHeight / 2 + 52;
        giveUpButtonRight = giveUpButtonX + GIVE_UP_BUTTON_WIDTH;
        giveUpButtonBottom = giveUpButtonY + GIVE_UP_BUTTON_HEIGHT;

        boolean hovered = isMouseOverGiveUpButton(minecraft);
        int background = hovered ? 0xCCAA2222 : 0xCC661111;
        int border = hovered ? 0xFFFF5555 : 0xFFAA3333;

        graphics.fill(giveUpButtonX, giveUpButtonY, giveUpButtonRight, giveUpButtonBottom, background);
        graphics.fill(giveUpButtonX, giveUpButtonY, giveUpButtonRight, giveUpButtonY + 1, border);
        graphics.fill(giveUpButtonX, giveUpButtonBottom - 1, giveUpButtonRight, giveUpButtonBottom, border);
        graphics.fill(giveUpButtonX, giveUpButtonY, giveUpButtonX + 1, giveUpButtonBottom, border);
        graphics.fill(giveUpButtonRight - 1, giveUpButtonY, giveUpButtonRight, giveUpButtonBottom, border);

        Component label = Component.translatable("knockoutmod.overlay.give_up");
        int textX = giveUpButtonX + (GIVE_UP_BUTTON_WIDTH - minecraft.font.width(label)) / 2;
        int textY = giveUpButtonY + (GIVE_UP_BUTTON_HEIGHT - 8) / 2;
        graphics.drawString(minecraft.font, label, textX, textY, 0xFFFFFF, true);

        KnockoutHudRenderer.drawCenteredString(
                graphics,
                minecraft.font,
                screenWidth,
                giveUpButtonBottom + 6,
                Component.translatable("knockoutmod.overlay.give_up_hint"),
                0xAAAAAA
        );
    }

    private static boolean isMouseOverGiveUpButton(Minecraft minecraft) {
        if (giveUpButtonRight <= giveUpButtonX || giveUpButtonBottom <= giveUpButtonY) {
            return false;
        }

        double mouseX = minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth()
                / minecraft.getWindow().getScreenWidth();
        double mouseY = minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight()
                / minecraft.getWindow().getScreenHeight();
        return mouseX >= giveUpButtonX
                && mouseX <= giveUpButtonRight
                && mouseY >= giveUpButtonY
                && mouseY <= giveUpButtonBottom;
    }
}
