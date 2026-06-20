package com.knockoutmod.knockout;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Ground alignment inspired by {@code fewizz/crawl} (1.20):
 * low eye height, no sleeping/bed offsets — collision-shape floor scan for surface snap.
 */
public final class KnockoutLyingAlign {
    /** crawl {@code Shared.CRAWLING} / prone eye height. */
    public static final float CRAWL_EYE_HEIGHT = 0.6F;

    private KnockoutLyingAlign() {
    }

    /** crawl {@code setupTransforms}: {@code pitch / 10F} surface nudge when fully down. */
    public static float crawlSurfaceNudge(float layProgress) {
        return layProgress / 10.0F;
    }

    /**
     * Highest solid collision top under (x, z) near {@code feetHint}.
     * Uses block collision shapes (reliable in 1-block holes and on slopes).
     */
    public static double findSupportSurfaceY(Level level, double x, double z, double feetHint) {
        int startY = Mth.ceil(feetHint + 0.01D);
        double best = Double.NEGATIVE_INFINITY;

        for (int y = startY; y >= startY - 8 && y >= level.getMinBuildHeight(); y--) {
            BlockPos pos = BlockPos.containing(x, y, z);
            if (!level.isLoaded(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }

            VoxelShape shape = state.getCollisionShape(level, pos);
            if (shape.isEmpty()) {
                continue;
            }

            double top = pos.getY() + shape.max(Direction.Axis.Y);
            best = Math.max(best, top);
        }

        return best;
    }

    public static double resolveSupportSurfaceY(Level level, double x, double z, double feetHint, AABB sampleBox) {
        double best = findSupportSurfaceY(level, x, z, feetHint);

        if (sampleBox != null) {
            for (int xi = 0; xi <= 2; xi++) {
                double sampleX = Mth.lerp(xi / 2.0D, sampleBox.minX, sampleBox.maxX);
                for (int zi = 0; zi <= 2; zi++) {
                    double sampleZ = Mth.lerp(zi / 2.0D, sampleBox.minZ, sampleBox.maxZ);
                    best = Math.max(best, findSupportSurfaceY(level, sampleX, sampleZ, feetHint));
                }
            }
        }

        return best;
    }
}
