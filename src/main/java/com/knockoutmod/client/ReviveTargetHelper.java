package com.knockoutmod.client;

import com.knockoutmod.entity.KnockoutTestDummyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class ReviveTargetHelper {
    private ReviveTargetHelper() {
    }

    public static Entity getCrosshairKnockoutTarget(Minecraft minecraft) {
        if (minecraft.player == null
                || !(minecraft.hitResult instanceof EntityHitResult entityHit)
                || minecraft.hitResult.getType() != HitResult.Type.ENTITY) {
            return null;
        }

        Entity entity = entityHit.getEntity();
        return isKnockoutTarget(entity, minecraft.player) ? entity : null;
    }

    public static boolean isKnockoutTarget(Entity entity, Player interactor) {
        if (interactor.distanceToSqr(entity) > 9.0D) {
            return false;
        }

        if (entity instanceof Player victim) {
            return victim != interactor && KnockoutClientState.isReviveTarget(victim);
        }

        return entity instanceof KnockoutTestDummyEntity dummy && dummy.isKnockedOut();
    }
}
