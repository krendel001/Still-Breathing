package com.knockoutmod.knockout;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

public final class KnockoutPoseApplier {
    private KnockoutPoseApplier() {
    }

    public static void applyServer(Player player, KnockoutPose pose) {
        player.setForcedPose(Pose.STANDING);
        player.setDeltaMovement(0.0D, 0.0D, 0.0D);
        player.fallDistance = 0.0F;
        player.setShiftKeyDown(false);

        if (player.isSprinting()) {
            player.setSprinting(false);
        }
    }

    public static void clearServer(Player player) {
        player.setForcedPose(null);
        KnockoutHitboxHelper.restoreHitbox(player);
    }

    public static void applyClient(Player player, KnockoutPose pose) {
        player.setForcedPose(Pose.STANDING);
        player.setPose(Pose.STANDING);
        player.setShiftKeyDown(false);
        player.setDeltaMovement(0.0D, 0.0D, 0.0D);
    }

    public static void clearClient(Player player) {
        player.setForcedPose(null);
        if (!KnockoutData.isKnockedOut(player)) {
            player.setPose(Pose.STANDING);
            KnockoutHitboxHelper.restoreHitbox(player);
        }
    }
}
