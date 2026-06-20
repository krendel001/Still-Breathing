package com.knockoutmod.client;

import com.knockoutmod.client.gecko.KnockoutLyingGeoRenderer;
import com.knockoutmod.knockout.KnockoutLyingAlign;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class KnockoutBodyRender {
    private KnockoutBodyRender() {
    }

    public static int resolveKnockoutLight(LivingEntity entity, int packedLight) {
        int block = Math.max(LightTexture.block(packedLight), 14);
        int sky = Math.max(LightTexture.sky(packedLight), 14);
        if (entity == Minecraft.getInstance().player) {
            block = 15;
            sky = 15;
        }
        return LightTexture.pack(block, sky);
    }

    public static void renderLyingBody(
            LivingEntity entity,
            PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource bufferSource,
            int packedLight,
            float partialTick
    ) {
        KnockoutClientState.noteKnockoutStart(entity);

        double entityY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double boxFeetY = entity.getBoundingBox().minY;
        float layProgress = KnockoutClientState.getLayProgress(entity, partialTick);
        float crawlNudge = KnockoutLyingAlign.crawlSurfaceNudge(layProgress);

        poseStack.pushPose();
        poseStack.translate(0.0D, (boxFeetY - entityY) + crawlNudge, 0.0D);

        int renderLight = resolveKnockoutLight(entity, packedLight);
        KnockoutLyingGeoRenderer.getInstance().renderKnockedOut(
                entity,
                poseStack,
                bufferSource,
                renderLight,
                partialTick
        );
        poseStack.popPose();
    }
}
