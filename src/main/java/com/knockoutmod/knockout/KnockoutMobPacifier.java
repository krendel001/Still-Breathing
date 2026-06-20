package com.knockoutmod.knockout;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public final class KnockoutMobPacifier {
    private static final double PACIFY_RADIUS = 32.0D;

    private KnockoutMobPacifier() {
    }

    public static void pacifyAround(ServerPlayer player, @Nullable Entity attacker) {
        if (attacker instanceof Mob mob) {
            releaseTarget(mob, player);
        }

        AABB box = player.getBoundingBox().inflate(PACIFY_RADIUS);
        for (Mob mob : player.serverLevel().getEntitiesOfClass(Mob.class, box)) {
            if (player.equals(mob.getTarget())) {
                releaseTarget(mob, player);
            }
        }
    }

    public static void releaseTarget(Mob mob, ServerPlayer player) {
        if (!player.equals(mob.getTarget())) {
            return;
        }

        mob.setTarget(null);
        mob.setAggressive(false);

        if (mob instanceof NeutralMob neutral) {
            neutral.setRemainingPersistentAngerTime(0);
            neutral.setPersistentAngerTarget(null);
        }
    }
}
