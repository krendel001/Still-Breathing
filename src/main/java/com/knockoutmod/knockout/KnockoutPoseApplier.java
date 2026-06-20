package com.knockoutmod.knockout;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

public final class KnockoutPoseApplier {
    private KnockoutPoseApplier() {
    }

    public static void applyServer(Player player, KnockoutPose pose) {
        player.setForcedPose(KnockoutHitboxHelper.KNOCKOUT_POSE);
        player.setDeltaMovement(0.0D, 0.0D, 0.0D);
        player.fallDistance = 0.0F;
        player.setShiftKeyDown(false);
        player.setNoGravity(true);
        player.setOnGround(true);

        if (player.isSprinting()) {
            player.setSprinting(false);
        }
    }

    public static void clearServer(Player player) {
        player.setForcedPose(null);
        player.setNoGravity(false);
        KnockoutHitboxHelper.restoreHitbox(player);
    }

    public static void applyClient(Player player, KnockoutPose pose) {
        player.setForcedPose(KnockoutHitboxHelper.KNOCKOUT_POSE);
        player.setPose(KnockoutHitboxHelper.KNOCKOUT_POSE);
        player.setShiftKeyDown(false);
        player.setDeltaMovement(0.0D, 0.0D, 0.0D);
        player.setNoGravity(true);
        player.setOnGround(true);
    }

    public static void clearClient(Player player) {
        player.setForcedPose(null);
        player.setNoGravity(false);
        if (!KnockoutData.isKnockedOut(player)) {
            player.setPose(Pose.STANDING);
            KnockoutHitboxHelper.restoreHitbox(player);
        }
    }
}
