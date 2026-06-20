package com.knockoutmod.knockout;

import com.knockoutmod.entity.KnockoutTestDummyEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class KnockoutHitboxHelper {
    public static final float LYING_LENGTH = 1.95F;
    public static final float LYING_HEIGHT = 0.35F;
    public static final float LYING_WIDTH = 0.6F;
    public static final float LYING_EYE_HEIGHT = KnockoutLyingAlign.CRAWL_EYE_HEIGHT;
    public static final EntityDimensions LYING_DIMENSIONS = EntityDimensions.scalable(LYING_LENGTH, LYING_HEIGHT);

    /** STANDING + custom dimensions — avoids vanilla SWIMMING render offset (~1 block down). */
    public static final Pose KNOCKOUT_POSE = Pose.STANDING;

    private static final double SURFACE_EPSILON = 1.0E-4D;

    private KnockoutHitboxHelper() {
    }

    public static boolean isKnockedOutLying(Entity entity) {
        if (entity instanceof Player player) {
            return KnockoutData.isKnockedOut(player) && KnockoutData.getPose(player) == KnockoutPose.LYING;
        }
        if (entity instanceof KnockoutTestDummyEntity dummy) {
            return dummy.isKnockedOut() && dummy.getKnockoutPose() == KnockoutPose.LYING;
        }
        return false;
    }

    public static void applyKnockoutHitbox(LivingEntity entity) {
        if (!isKnockedOutLying(entity)) {
            return;
        }

        double anchorFeetY = entity.getBoundingBox().minY;
        applyKnockoutPose(entity);
        snapFeetToSupport(entity, anchorFeetY);
    }

    public static void maintainKnockoutHitbox(LivingEntity entity) {
        if (!isKnockedOutLying(entity)) {
            return;
        }
        if (entity.getPose() != KNOCKOUT_POSE) {
            entity.setPose(KNOCKOUT_POSE);
        }
        updateOrientedLyingBox(entity);
        alignToSupportSurface(entity);
    }

    public static double getRenderFeetY(LivingEntity entity, float partialTick) {
        double entityY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        Level level = entity.level();
        AABB box = entity.getBoundingBox();
        if (level == null) {
            return box.minY;
        }

        double groundTop = KnockoutLyingAlign.resolveSupportSurfaceY(
                level,
                entity.getX(),
                entity.getZ(),
                entityY,
                box
        );
        if (groundTop != Double.NEGATIVE_INFINITY) {
            return groundTop;
        }
        return Math.max(box.minY, entityY);
    }

    public static void restoreHitbox(LivingEntity entity) {
        entity.setPose(Pose.STANDING);
        entity.refreshDimensions();
        entity.setBoundingBox(entity.getDimensions(Pose.STANDING).makeBoundingBox(entity.position()));
    }

    public static void updateOrientedLyingBox(LivingEntity entity) {
        double yawRad = Math.toRadians(entity.yBodyRot);
        double sin = Math.sin(yawRad);
        double cos = Math.cos(yawRad);
        double halfLen = LYING_LENGTH / 2.0D;
        double halfWidth = LYING_WIDTH / 2.0D;

        double cx = entity.getX();
        double cy = entity.getY();
        double cz = entity.getZ();
        double extentX = halfLen * Math.abs(sin) + halfWidth * Math.abs(cos);
        double extentZ = halfLen * Math.abs(cos) + halfWidth * Math.abs(sin);

        entity.setBoundingBox(new AABB(
                cx - extentX,
                cy,
                cz - extentZ,
                cx + extentX,
                cy + LYING_HEIGHT,
                cz + extentZ
        ));
    }

    private static void applyKnockoutPose(LivingEntity entity) {
        entity.setPose(KNOCKOUT_POSE);
        entity.refreshDimensions();
    }

    private static void snapFeetToSupport(LivingEntity entity, double anchorFeetY) {
        Level level = entity.level();
        double surface = KnockoutLyingAlign.resolveSupportSurfaceY(
                level,
                entity.getX(),
                entity.getZ(),
                anchorFeetY,
                null
        );
        double feetY = surface != Double.NEGATIVE_INFINITY
                ? Math.max(anchorFeetY, surface)
                : anchorFeetY;
        moveFeetTo(entity, feetY);
    }

    private static void alignToSupportSurface(LivingEntity entity) {
        Level level = entity.level();
        AABB box = entity.getBoundingBox();
        double surface = KnockoutLyingAlign.resolveSupportSurfaceY(level, entity.getX(), entity.getZ(), box.minY, box);
        if (surface == Double.NEGATIVE_INFINITY) {
            return;
        }
        if (Math.abs(box.minY - surface) > SURFACE_EPSILON) {
            moveFeetTo(entity, surface);
        }
    }

    private static void moveFeetTo(LivingEntity entity, double feetY) {
        entity.setPos(entity.getX(), feetY, entity.getZ());
        updateOrientedLyingBox(entity);
    }

    public static Vec3 getLyingSmokePosition(LivingEntity entity) {
        AABB box = entity.getBoundingBox();
        return new Vec3(
                box.getCenter().x,
                box.minY + LYING_HEIGHT * 0.28D,
                box.getCenter().z
        );
    }
}
