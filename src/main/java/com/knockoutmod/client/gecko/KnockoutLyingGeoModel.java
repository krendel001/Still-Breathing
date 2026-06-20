package com.knockoutmod.client.gecko;

import com.knockoutmod.KnockoutMod;
import com.knockoutmod.entity.KnockoutTestDummyEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.model.GeoModel;

import java.util.UUID;

public class KnockoutLyingGeoModel extends GeoModel<KnockoutRenderContext> {
    private static final UUID FALLBACK_SKIN = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            KnockoutMod.MOD_ID,
            "geo/knockout_lying.geo.json"
    );
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(
            KnockoutMod.MOD_ID,
            "animations/knockout_lying.animation.json"
    );

    @Override
    public ResourceLocation getModelResource(KnockoutRenderContext animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(KnockoutRenderContext animatable) {
        LivingEntity entity = animatable.getEntity();
        if (entity instanceof AbstractClientPlayer player) {
            return player.getSkinTextureLocation();
        }
        if (entity instanceof KnockoutTestDummyEntity dummy) {
            return DefaultPlayerSkin.getDefaultSkin(dummy.getSkinUuid());
        }
        return DefaultPlayerSkin.getDefaultSkin(FALLBACK_SKIN);
    }

    @Override
    public ResourceLocation getAnimationResource(KnockoutRenderContext animatable) {
        return ANIMATION;
    }
}
