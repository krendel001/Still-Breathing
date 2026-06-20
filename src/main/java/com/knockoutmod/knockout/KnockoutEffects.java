package com.knockoutmod.knockout;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class KnockoutEffects {
    private static final int REFRESH = 25;

    private KnockoutEffects() {
    }

    public static void apply(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, REFRESH, 0, false, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, REFRESH, 0, false, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, REFRESH, 0, false, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, REFRESH, 3, false, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, REFRESH, 255, false, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, REFRESH, 255, false, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, REFRESH, 1, false, false, false));
    }

    public static void clear(LivingEntity entity) {
        entity.removeEffect(MobEffects.BLINDNESS);
        entity.removeEffect(MobEffects.DARKNESS);
        entity.removeEffect(MobEffects.CONFUSION);
        entity.removeEffect(MobEffects.WEAKNESS);
        entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        entity.removeEffect(MobEffects.DIG_SLOWDOWN);
        entity.removeEffect(MobEffects.HUNGER);
    }
}
