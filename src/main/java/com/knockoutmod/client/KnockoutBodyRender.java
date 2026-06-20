package com.knockoutmod.client;

import com.knockoutmod.client.gecko.KnockoutLyingGeoRenderer;
import com.knockoutmod.knockout.KnockoutHitboxHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public final class KnockoutBodyRender {
    private KnockoutBodyRender() {
    }

    public static void translateToEntityFeet(
            LivingEntity entity,
            PoseStack poseStack,
            double cameraX,
            double cameraY,
            double cameraZ,
            float partialTick
    ) {
        AABB box = entity.getBoundingBox();
        double entityX = Mth.lerp(partialTick, entity.xo, entity.getX());
        double entityY = box.minY;
        double entityZ = Mth.lerp(partialTick, entity.zo, entity.getZ());
        poseStack.translate(entityX - cameraX, entityY - cameraY, entityZ - cameraZ);
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
        KnockoutHitboxHelper.maintainKnockoutHitbox(entity);
        int renderLight = resolveKnockoutLight(entity, packedLight);
        KnockoutLyingGeoRenderer.getInstance().renderKnockedOut(
                entity,
                poseStack,
                bufferSource,
                renderLight,
                partialTick
        );
    }
}
