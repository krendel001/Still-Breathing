package com.knockoutmod.client.gecko;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.loading.json.raw.ModelProperties;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.RenderUtils;

/**
 * Computes the Y offset needed to place the lowest point of the lying geo model on the ground.
 */
public final class KnockoutLyingGroundAlign {
    private static final float PLAYER_SCALE = 0.9375F;
    private static final float GROUND_NUDGE = 0.03F;

    private static float cachedModelFloorY = Float.NaN;

    private KnockoutLyingGroundAlign() {
    }

    public static float computeYOffset(
            GeoModel<KnockoutRenderContext> geoModel,
            KnockoutRenderContext context
    ) {
        return Math.max(0.0F, getModelFloorY(geoModel, context));
    }

    public static void invalidateCache() {
        cachedModelFloorY = Float.NaN;
    }

    private static float getModelFloorY(GeoModel<KnockoutRenderContext> geoModel, KnockoutRenderContext context) {
        if (!Float.isNaN(cachedModelFloorY)) {
            return cachedModelFloorY;
        }

        BakedGeoModel baked = geoModel.getBakedModel(geoModel.getModelResource(context));
        PoseStack probe = new PoseStack();
        probe.scale(-1.0F, -1.0F, 1.0F);
        probe.scale(PLAYER_SCALE, PLAYER_SCALE, PLAYER_SCALE);

        float minY = Float.POSITIVE_INFINITY;
        for (GeoBone bone : baked.topLevelBones()) {
            minY = Math.min(minY, scanBoneMinY(probe, bone));
        }

        float scannedOffset = Float.isFinite(minY) ? -minY + GROUND_NUDGE : 0.53F;
        float boundsOffset = computeVisibleBoundsOffset(baked.properties()) + GROUND_NUDGE;
        cachedModelFloorY = Math.max(scannedOffset, boundsOffset);
        return cachedModelFloorY;
    }

    private static float computeVisibleBoundsOffset(ModelProperties properties) {
        if (properties == null || properties.visibleBoundsHeight() == null) {
            return 0.0F;
        }

        double[] offset = properties.visibleBoundsOffset();
        if (offset == null || offset.length < 2) {
            return 0.0F;
        }

        double centerY = offset[1];
        double halfHeight = properties.visibleBoundsHeight() * 0.5D;
        double bottomY = centerY - halfHeight;
        return bottomY < 0.0D ? (float) -bottomY : 0.0F;
    }

    private static float scanBoneMinY(PoseStack parentPose, GeoBone bone) {
        PoseStack bonePose = new PoseStack();
        bonePose.last().pose().set(parentPose.last().pose());
        RenderUtils.prepMatrixForBone(bonePose, bone);

        float minY = Float.POSITIVE_INFINITY;
        for (GeoCube cube : bone.getCubes()) {
            if (cube.inflate() > 0.0D) {
                continue;
            }
            minY = Math.min(minY, scanCubeMinY(bonePose, cube));
        }

        for (GeoBone child : bone.getChildBones()) {
            minY = Math.min(minY, scanBoneMinY(bonePose, child));
        }

        return minY;
    }

    private static float scanCubeMinY(PoseStack bonePose, GeoCube cube) {
        PoseStack cubePose = new PoseStack();
        cubePose.last().pose().set(bonePose.last().pose());
        RenderUtils.translateToPivotPoint(cubePose, cube);
        RenderUtils.rotateMatrixAroundCube(cubePose, cube);
        RenderUtils.translateAwayFromPivotPoint(cubePose, cube);

        Matrix4f matrix = cubePose.last().pose();
        float minY = Float.POSITIVE_INFINITY;
        Vector4f point = new Vector4f();

        for (GeoQuad quad : cube.quads()) {
            if (quad == null) {
                continue;
            }
            for (GeoVertex vertex : quad.vertices()) {
                Vector3f position = vertex.position();
                point.set(position.x(), position.y(), position.z(), 1.0F);
                point.mul(matrix);
                minY = Math.min(minY, point.y());
            }
        }

        return minY;
    }
}
