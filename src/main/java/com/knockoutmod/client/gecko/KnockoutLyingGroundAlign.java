package com.knockoutmod.client.gecko;

public final class KnockoutLyingGroundAlign {
    /**
     * World-space lift applied before the renderer flips the model on Y.
     * 0.0 put the mesh underground; 0.55 still floated above the lying hitbox.
     * This keeps the pre-rotated geo model close to the ground without relying on Gecko internals.
     */
    private static final float VISUAL_FEET_LIFT = 0.16F;

    private KnockoutLyingGroundAlign() {
    }

    public static float computeYOffset() {
        return VISUAL_FEET_LIFT;
    }
}
