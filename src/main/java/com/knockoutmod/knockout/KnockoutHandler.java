package com.knockoutmod.knockout;

import com.knockoutmod.config.KnockoutConfig;
import com.knockoutmod.network.ModNetwork;
import com.knockoutmod.network.SyncKnockoutPacket;
import com.knockoutmod.network.SyncSelfReviveProgressPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.network.PacketDistributor;

public final class KnockoutHandler {
    private KnockoutHandler() {
    }

    public static boolean canBeKnockedOut(Player player) {
        if (!KnockoutConfig.SERVER.knockoutCreativePlayers.get()
                && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        if (KnockoutData.isInKnockoutGrace(player)) {
            return false;
        }
        return !KnockoutData.isKnockedOut(player);
    }

    public static boolean isInKnockoutGrace(Player player) {
        return KnockoutData.isInKnockoutGrace(player);
    }

    public static void enterKnockoutForced(ServerPlayer player) {
        if (KnockoutData.isKnockedOut(player)) {
            return;
        }
        KnockoutData.setGraceTicks(player, 0);
        applyKnockout(player);
    }

    public static void enterKnockout(ServerPlayer player, DamageSource source) {
        if (!canBeKnockedOut(player)) {
            return;
        }
        applyKnockout(player);
    }

    private static void applyKnockout(ServerPlayer player) {
        KnockoutData.setKnockedOut(player, true);
        KnockoutData.setKnockoutTicks(player, 0);
        KnockoutData.setFinishHits(player, 0);
        KnockoutData.setAllyReviveProgress(player, 0);
        KnockoutData.setSelfReviveProgress(player, 0);

        int stacks = Math.min(
                KnockoutData.getExhaustionStacks(player) + 1,
                KnockoutConfig.SERVER.maxExhaustionStacks.get()
        );
        KnockoutData.setExhaustionStacks(player, stacks);

        KnockoutPose pose = KnockoutPose.LYING;
        KnockoutData.setPose(player, pose);

        player.setHealth(1.0F);
        KnockoutPoseApplier.applyServer(player, pose);
        KnockoutHitboxHelper.applyKnockoutHitbox(player);
        player.hurtMarked = true;
        player.stopUsingItem();
        KnockoutEffects.apply(player);

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.PLAYER_HURT,
                SoundSource.PLAYERS,
                0.8F,
                0.6F
        );

        player.displayClientMessage(Component.translatable("knockoutmod.message.knocked_out"), true);
        syncSelfReviveProgress(player, 0, KnockoutConfig.SERVER.selfReviveHoldSeconds.get() * 20, false);
        syncKnockout(player);
    }

    public static void tickKnockedOut(ServerPlayer player) {
        if (!KnockoutData.isKnockedOut(player)) {
            return;
        }

        KnockoutData.setKnockoutTicks(player, KnockoutData.getKnockoutTicks(player) + 1);
        maintainKnockoutState(player);

        if (player.tickCount % 10 == 0) {
            syncKnockout(player);
        }

        int bleedOutTicks = KnockoutConfig.SERVER.bleedOutSeconds.get() * 20;
        int remainingSeconds = Math.max(0, (bleedOutTicks - KnockoutData.getKnockoutTicks(player)) / 20);
        if (player.tickCount % 20 == 0) {
            player.displayClientMessage(
                    Component.translatable("knockoutmod.message.bleed_timer", remainingSeconds),
                    true
            );
        }

        int checkInterval = KnockoutConfig.SERVER.selfReviveCheckSeconds.get() * 20;
        if (checkInterval > 0 && KnockoutData.getKnockoutTicks(player) % checkInterval == 0) {
            tryPassiveSelfRevive(player);
        }

        tickSelfReviveHold(player);

        if (KnockoutData.getKnockoutTicks(player) >= bleedOutTicks) {
            finishBleedOut(player);
        }
    }

    public static void tickGrace(ServerPlayer player) {
        int grace = KnockoutData.getGraceTicks(player);
        if (grace <= 0) {
            return;
        }
        KnockoutData.setGraceTicks(player, grace - 1);
    }

    public static void maintainKnockoutState(ServerPlayer player) {
        KnockoutPose pose = KnockoutData.getPose(player);
        KnockoutPoseApplier.applyServer(player, pose);
        KnockoutHitboxHelper.maintainKnockoutHitbox(player);

        if (player.isSwimming()) {
            player.setSwimming(false);
        }
        KnockoutEffects.apply(player);
    }

    public static void onKnockedOutDamage(ServerPlayer player, DamageSource source, float amount) {
        if (!KnockoutData.isKnockedOut(player)) {
            return;
        }

        if (source.getEntity() instanceof ServerPlayer attacker && attacker != player) {
            int hits = KnockoutData.getFinishHits(player) + 1;
            int required = KnockoutConfig.SERVER.finishHitsRequired.get();
            KnockoutData.setFinishHits(player, hits);
            KnockoutFinishEffects.play(player, attacker, hits, required);
            if (hits >= required) {
                killFromKnockout(player, Component.translatable("knockoutmod.message.finished_by_player", attacker.getDisplayName()));
            }
            return;
        }

        int extraTicks = (int) (amount * 20 * KnockoutConfig.SERVER.environmentalDamageMultiplier.get());
        KnockoutData.setKnockoutTicks(player, KnockoutData.getKnockoutTicks(player) + extraTicks);
    }

    private static void tickSelfReviveHold(ServerPlayer player) {
        int required = KnockoutConfig.SERVER.selfReviveHoldSeconds.get() * 20;

        if (!SelfReviveHoldTracker.isHolding(player)) {
            int progress = KnockoutData.getSelfReviveProgress(player);
            if (progress > 0) {
                KnockoutData.setSelfReviveProgress(player, Math.max(0, progress - 2));
                syncSelfReviveProgress(player, KnockoutData.getSelfReviveProgress(player), required, false);
            } else if (KnockoutData.getKnockoutTicks(player) % 10 == 0) {
                syncSelfReviveProgress(player, 0, required, false);
            }
            return;
        }

        int progress = KnockoutData.getSelfReviveProgress(player) + 1;
        KnockoutData.setSelfReviveProgress(player, progress);
        syncSelfReviveProgress(player, progress, required, true);

        if (progress >= required) {
            KnockoutData.setSelfReviveProgress(player, 0);
            syncSelfReviveProgress(player, 0, required, false);
            if (rollSelfRevive(player, KnockoutConfig.SERVER.activeSelfReviveChance.get())) {
                wakeUp(player, null, false);
            } else {
                killFromKnockout(player, Component.translatable("knockoutmod.message.self_revive_failed_death"));
            }
        }
    }

    private static void syncSelfReviveProgress(ServerPlayer player, int progress, int required, boolean active) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncSelfReviveProgressPacket(progress, required, active)
        );
    }

    private static void tryPassiveSelfRevive(ServerPlayer player) {
        double progress = (double) KnockoutData.getKnockoutTicks(player)
                / (KnockoutConfig.SERVER.bleedOutSeconds.get() * 20.0D);
        double chance = KnockoutConfig.SERVER.baseSelfReviveChance.get()
                + progress * (KnockoutConfig.SERVER.maxSelfReviveChance.get()
                - KnockoutConfig.SERVER.baseSelfReviveChance.get());
        chance -= KnockoutData.getExhaustionStacks(player) * KnockoutConfig.SERVER.exhaustionPenaltyPerStack.get();
        chance = Math.max(0.0D, Math.min(KnockoutConfig.SERVER.maxSelfReviveChance.get(), chance));

        if (rollSelfRevive(player, chance)) {
            wakeUp(player, null, false);
        }
    }

    private static boolean rollSelfRevive(ServerPlayer player, double chance) {
        return player.getRandom().nextDouble() < chance;
    }

    public static void wakeUp(ServerPlayer player, ServerPlayer helper, boolean assisted) {
        KnockoutData.clearKnockoutState(player);
        SelfReviveHoldTracker.clear(player);

        int graceSeconds = KnockoutConfig.SERVER.knockoutGraceSeconds.get();
        if (graceSeconds > 0) {
            KnockoutData.setGraceTicks(player, graceSeconds * 20);
        }

        KnockoutPoseApplier.clearServer(player);
        KnockoutEffects.clear(player);
        player.setHealth((float) (KnockoutConfig.SERVER.recoveryHealthHearts.get() * 2.0D));

        FoodData food = player.getFoodData();
        food.setFoodLevel(Math.max(food.getFoodLevel() - 6, 0));
        food.setSaturation(Math.max(food.getSaturationLevel() - 4.0F, 0.0F));

        int weaknessSeconds = KnockoutConfig.SERVER.recoveryWeaknessSeconds.get();
        if (weaknessSeconds > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, weaknessSeconds * 20, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, weaknessSeconds * 10, 0));
        }

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS,
                0.5F,
                1.2F
        );

        if (assisted && helper != null) {
            player.displayClientMessage(
                    Component.translatable("knockoutmod.message.woken_by", helper.getDisplayName()),
                    false
            );
            helper.displayClientMessage(
                    Component.translatable("knockoutmod.message.revived_someone", player.getDisplayName()),
                    true
            );
        } else {
            player.displayClientMessage(Component.translatable("knockoutmod.message.woken_up"), false);
        }

        if (graceSeconds > 0) {
            player.displayClientMessage(
                    Component.translatable("knockoutmod.message.grace_period", graceSeconds),
                    true
            );
        }

        syncSelfReviveProgressOnWake(player);
        syncKnockout(player);
    }

    private static void syncSelfReviveProgressOnWake(ServerPlayer player) {
        syncSelfReviveProgress(player, 0, KnockoutConfig.SERVER.selfReviveHoldSeconds.get() * 20, false);
    }

    private static void finishBleedOut(ServerPlayer player) {
        killFromKnockout(player, Component.translatable("knockoutmod.message.bled_out"));
    }

    public static void killFromKnockout(ServerPlayer player, Component deathMessage) {
        KnockoutData.clearKnockoutState(player);
        SelfReviveHoldTracker.clear(player);
        KnockoutData.setBypassKnockout(player, true);
        KnockoutPoseApplier.clearServer(player);
        KnockoutEffects.clear(player);
        syncSelfReviveProgress(player, 0, KnockoutConfig.SERVER.selfReviveHoldSeconds.get() * 20, false);
        syncKnockout(player);
        player.sendSystemMessage(deathMessage);
        player.hurt(player.damageSources().generic(), Float.MAX_VALUE);
    }

    public static void giveUp(ServerPlayer player) {
        if (!KnockoutData.isKnockedOut(player)) {
            return;
        }
        killFromKnockout(player, Component.translatable("knockoutmod.message.gave_up"));
    }

    public static void syncKnockout(ServerPlayer player) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                SyncKnockoutPacket.fromPlayer(player)
        );
    }
}
