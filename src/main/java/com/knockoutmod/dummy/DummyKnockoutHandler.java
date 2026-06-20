package com.knockoutmod.dummy;

import com.knockoutmod.entity.KnockoutTestDummyEntity;
import com.knockoutmod.init.ModEntities;
import com.knockoutmod.knockout.KnockoutEffects;
import com.knockoutmod.knockout.KnockoutFinishEffects;
import com.knockoutmod.knockout.KnockoutHitboxHelper;
import com.knockoutmod.knockout.KnockoutPose;
import com.knockoutmod.config.KnockoutConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Pose;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class DummyKnockoutHandler {
    private static final Map<UUID, Integer> KNOCKOUT_TICKS = new WeakHashMap<>();
    private static final Map<UUID, Integer> FINISH_HITS = new WeakHashMap<>();

    private DummyKnockoutHandler() {
    }

    public static int getKnockoutTicks(KnockoutTestDummyEntity dummy) {
        return KNOCKOUT_TICKS.getOrDefault(dummy.getUUID(), 0);
    }

    public static void setKnockoutTicks(KnockoutTestDummyEntity dummy, int ticks) {
        if (ticks <= 0) {
            KNOCKOUT_TICKS.remove(dummy.getUUID());
            return;
        }
        KNOCKOUT_TICKS.put(dummy.getUUID(), ticks);
    }

    public static void enterKnockout(KnockoutTestDummyEntity dummy, DamageSource source) {
        if (dummy.isKnockedOut()) {
            return;
        }

        dummy.setKnockedOut(true);
        setKnockoutTicks(dummy, 0);
        FINISH_HITS.put(dummy.getUUID(), 0);
        dummy.setKnockoutPose(KnockoutPose.LYING);
        dummy.setHealth(1.0F);
        maintainKnockoutState(dummy);
        KnockoutHitboxHelper.applyKnockoutHitbox(dummy);

        dummy.level().playSound(
                null,
                dummy.blockPosition(),
                SoundEvents.PLAYER_HURT,
                SoundSource.NEUTRAL,
                0.8F,
                0.6F
        );
    }

    public static void enterKnockoutForced(KnockoutTestDummyEntity dummy) {
        enterKnockout(dummy, dummy.damageSources().generic());
    }

    public static void tickKnockedOut(KnockoutTestDummyEntity dummy) {
        if (!dummy.isKnockedOut()) {
            return;
        }

        setKnockoutTicks(dummy, getKnockoutTicks(dummy) + 1);
        maintainKnockoutState(dummy);
    }

    public static void maintainKnockoutState(KnockoutTestDummyEntity dummy) {
        dummy.setKnockoutPose(KnockoutPose.LYING);
        dummy.setPose(KnockoutHitboxHelper.KNOCKOUT_POSE);
        dummy.setDeltaMovement(0.0D, 0.0D, 0.0D);
        dummy.fallDistance = 0.0F;
        dummy.setNoAi(true);
        dummy.setNoGravity(true);
        dummy.setOnGround(true);
        KnockoutHitboxHelper.maintainKnockoutHitbox(dummy);
        KnockoutEffects.apply(dummy);
    }

    public static void onKnockedOutDamage(KnockoutTestDummyEntity dummy, DamageSource source, float amount) {
        if (source.getEntity() instanceof ServerPlayer attacker) {
            int hits = FINISH_HITS.getOrDefault(dummy.getUUID(), 0) + 1;
            int required = KnockoutConfig.SERVER.finishHitsRequired.get();
            FINISH_HITS.put(dummy.getUUID(), hits);
            KnockoutFinishEffects.play(dummy, attacker, hits, required);
            if (hits >= required) {
                killDummy(dummy, attacker, Component.translatable("knockoutmod.message.dummy_finished", dummy.getDummyName()));
            }
            return;
        }

        setKnockoutTicks(dummy, getKnockoutTicks(dummy) + (int) (amount * 10.0F));
    }

    public static void wakeUp(KnockoutTestDummyEntity dummy) {
        dummy.setKnockedOut(false);
        setKnockoutTicks(dummy, 0);
        FINISH_HITS.remove(dummy.getUUID());
        dummy.setPose(Pose.STANDING);
        dummy.setNoAi(false);
        dummy.setNoGravity(false);
        dummy.setHealth(dummy.getMaxHealth() * 0.5F);
        KnockoutHitboxHelper.restoreHitbox(dummy);
        KnockoutEffects.clear(dummy);

        dummy.level().playSound(
                null,
                dummy.blockPosition(),
                SoundEvents.PLAYER_LEVELUP,
                SoundSource.NEUTRAL,
                0.5F,
                1.2F
        );
    }

    public static void killDummy(KnockoutTestDummyEntity dummy, ServerPlayer killer, Component message) {
        if (killer != null) {
            killer.displayClientMessage(message, true);
        }
        FINISH_HITS.remove(dummy.getUUID());
        setKnockoutTicks(dummy, 0);
        KnockoutEffects.clear(dummy);
        dummy.discard();
    }

    public static void removeAllDummies(ServerPlayer contextPlayer) {
        for (ServerLevel level : contextPlayer.server.getAllLevels()) {
            List<KnockoutTestDummyEntity> dummies = new ArrayList<>(
                    level.getEntities(ModEntities.TEST_DUMMY.get(), dummy -> true)
            );
            dummies.forEach(KnockoutTestDummyEntity::discard);
        }
    }

    public static int countAllDummies(ServerPlayer contextPlayer) {
        int count = 0;
        for (ServerLevel level : contextPlayer.server.getAllLevels()) {
            count += level.getEntities(ModEntities.TEST_DUMMY.get(), dummy -> true).size();
        }
        return count;
    }
}
