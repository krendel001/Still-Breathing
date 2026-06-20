package com.knockoutmod.knockout;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public final class KnockoutFinishEffects {
    private KnockoutFinishEffects() {
    }

    public static void play(LivingEntity victim, ServerPlayer attacker, int currentHit, int requiredHits) {
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }

        float progress = (float) currentHit / (float) requiredHits;
        float pitch = 0.75F + progress * 0.35F;
        double centerY = victim.getY() + KnockoutHitboxHelper.LYING_HEIGHT * 0.55D;

        attacker.swing(InteractionHand.MAIN_HAND, true);
        victim.hurtTime = 10;
        victim.hurtDuration = 10;
        victim.invulnerableTime = 20;

        level.playSound(
                null,
                victim.getX(),
                centerY,
                victim.getZ(),
                SoundEvents.PLAYER_HURT,
                SoundSource.PLAYERS,
                1.0F,
                pitch
        );
        level.playSound(
                null,
                victim.getX(),
                centerY,
                victim.getZ(),
                SoundEvents.PLAYER_ATTACK_KNOCKBACK,
                SoundSource.PLAYERS,
                0.85F,
                0.9F + progress * 0.2F
        );

        level.sendParticles(
                ParticleTypes.CRIT,
                victim.getX(),
                centerY,
                victim.getZ(),
                6,
                0.35D,
                0.12D,
                0.35D,
                0.08D
        );
        level.sendParticles(
                ParticleTypes.DAMAGE_INDICATOR,
                victim.getX(),
                centerY + 0.15D,
                victim.getZ(),
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
        );

        if (currentHit >= requiredHits) {
            level.playSound(
                    null,
                    victim.getX(),
                    centerY,
                    victim.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT,
                    SoundSource.PLAYERS,
                    1.0F,
                    0.85F
            );
            level.sendParticles(
                    ParticleTypes.SWEEP_ATTACK,
                    victim.getX(),
                    centerY,
                    victim.getZ(),
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
            return;
        }

        attacker.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                        "knockoutmod.message.finish_progress",
                        currentHit,
                        requiredHits
                ),
                true
        );
    }
}
