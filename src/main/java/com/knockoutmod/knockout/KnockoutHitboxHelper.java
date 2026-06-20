package com.knockoutmod.knockout;

import com.knockoutmod.entity.KnockoutTestDummyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class KnockoutHitboxHelper {
    public static final float LYING_LENGTH = 1.95F;
    public static final float LYING_HEIGHT = 0.35F;
    public static final float LYING_WIDTH = 0.6F;
    public static final float LYING_EYE_HEIGHT = 0.15F;
    public static final EntityDimensions LYING_DIMENSIONS = EntityDimensions.scalable(LYING_LENGTH, LYING_HEIGHT);

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
        entity.setPose(Pose.STANDING);
        entity.refreshDimensions();
        updateOrientedLyingBox(entity);
        snapToGround(entity);
    }

    public static void maintainKnockoutHitbox(LivingEntity entity) {
        if (!isKnockedOutLying(entity)) {
            return;
        }
        updateOrientedLyingBox(entity);
        snapToGround(entity);
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

    public static void snapToGround(LivingEntity entity) {
        Level level = entity.level();
        double groundTop = findGroundTop(level, entity.getX(), entity.getZ(), entity.getBoundingBox().maxY + 1.0D);
        if (groundTop == Double.NEGATIVE_INFINITY) {
            return;
        }

        double minY = entity.getBoundingBox().minY;
        if (minY + 1.0E-4 < groundTop) {
            entity.setPos(entity.getX(), entity.getY() + (groundTop - minY), entity.getZ());
            updateOrientedLyingBox(entity);
        }
    }

    private static double findGroundTop(Level level, double x, double z, double startY) {
        int topY = (int) Math.floor(startY);
        double best = Double.NEGATIVE_INFINITY;

        for (int y = topY; y >= topY - 4 && y >= level.getMinBuildHeight(); y--) {
            BlockPos below = BlockPos.containing(x, y - 1, z);
            VoxelShape shape = level.getBlockState(below).getCollisionShape(level, below);
            if (shape.isEmpty()) {
                continue;
            }
            best = Math.max(best, below.getY() + shape.max(Direction.Axis.Y));
        }

        return best;
    }

    /** Client/server-safe anchor for knockout ambience particles (torso, near the ground). */
    public static Vec3 getLyingSmokePosition(LivingEntity entity) {
        AABB box = entity.getBoundingBox();
        return new Vec3(
                box.getCenter().x,
                box.minY + LYING_HEIGHT * 0.28D,
                box.getCenter().z
        );
    }
}
