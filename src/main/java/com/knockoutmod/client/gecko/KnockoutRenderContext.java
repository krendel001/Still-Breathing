package com.knockoutmod.client.gecko;

import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class KnockoutRenderContext implements GeoAnimatable {
    public static final KnockoutRenderContext INSTANCE = new KnockoutRenderContext();

    private static final ThreadLocal<LivingEntity> CURRENT_ENTITY = new ThreadLocal<>();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private KnockoutRenderContext() {
    }

    public void bind(LivingEntity entity) {
        CURRENT_ENTITY.set(entity);
    }

    public LivingEntity getEntity() {
        return CURRENT_ENTITY.get();
    }

    public void unbind() {
        CURRENT_ENTITY.remove();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0, state -> {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object object) {
        LivingEntity entity = getEntity();
        if (entity == null) {
            return 0.0D;
        }
        return entity.tickCount;
    }
}
