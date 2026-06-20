package com.knockoutmod.client.gecko;

import com.knockoutmod.client.KnockoutBodyRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

public final class KnockoutLyingGeoRenderer extends GeoObjectRenderer<KnockoutRenderContext> {
    private static final float PLAYER_SCALE = 0.9375F;

    private static KnockoutLyingGeoRenderer instance;

    private SkinPass skinPass = SkinPass.BASE;

    public KnockoutLyingGeoRenderer() {
        super(new KnockoutLyingGeoModel());
    }

    public static KnockoutLyingGeoRenderer getInstance() {
        if (instance == null) {
            instance = new KnockoutLyingGeoRenderer();
        }
        return instance;
    }

    @Override
    public long getInstanceId(KnockoutRenderContext animatable) {
        LivingEntity entity = animatable.getEntity();
        return entity != null ? entity.getId() : super.getInstanceId(animatable);
    }

    public void renderKnockedOut(
            LivingEntity entity,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            float partialTick
    ) {
        KnockoutRenderContext context = KnockoutRenderContext.INSTANCE;
        context.bind(entity);
        try {
            poseStack.pushPose();

            float bodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.scale(PLAYER_SCALE, PLAYER_SCALE, PLAYER_SCALE);
            float groundOffset = KnockoutLyingGroundAlign.computeYOffset(getGeoModel(), context);
            poseStack.translate(0.0F, groundOffset, 0.0F);

            int renderLight = KnockoutBodyRender.resolveKnockoutLight(entity, packedLight);
            ResourceLocation texture = getTextureLocation(context);

            skinPass = SkinPass.BASE;
            RenderType baseType = RenderType.entityCutoutNoCull(texture);
            defaultRender(
                    poseStack,
                    context,
                    bufferSource,
                    baseType,
                    bufferSource.getBuffer(baseType),
                    0.0F,
                    partialTick,
                    renderLight
            );

            skinPass = SkinPass.OVERLAY;
            RenderType overlayType = RenderType.entityTranslucent(texture);
            defaultRender(
                    poseStack,
                    context,
                    bufferSource,
                    overlayType,
                    bufferSource.getBuffer(overlayType),
                    0.0F,
                    partialTick,
                    renderLight
            );
            poseStack.popPose();
        } finally {
            skinPass = SkinPass.BASE;
            context.unbind();
        }
    }

    @Override
    public void renderCubesOfBone(
            PoseStack poseStack,
            GeoBone bone,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        if (bone.isHidden()) {
            return;
        }

        for (GeoCube cube : bone.getCubes()) {
            if (!shouldRenderCube(cube)) {
                continue;
            }
            renderCube(poseStack, cube, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

    private boolean shouldRenderCube(GeoCube cube) {
        boolean overlayCube = cube.inflate() > 0.0D;
        return switch (skinPass) {
            case BASE -> !overlayCube;
            case OVERLAY -> overlayCube;
        };
    }

    @Override
    public void preRender(
            PoseStack poseStack,
            KnockoutRenderContext animatable,
            BakedGeoModel model,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        this.objectRenderTranslations = new Matrix4f(poseStack.last().pose());
    }

    @Override
    public int getPackedOverlay(KnockoutRenderContext animatable, float u, float partialTick) {
        return OverlayTexture.NO_OVERLAY;
    }

    @Override
    public boolean firePreRenderEvent(
            PoseStack poseStack,
            BakedGeoModel model,
            MultiBufferSource bufferSource,
            float partialTick,
            int packedLight
    ) {
        return true;
    }

    private enum SkinPass {
        BASE,
        OVERLAY
    }
}
