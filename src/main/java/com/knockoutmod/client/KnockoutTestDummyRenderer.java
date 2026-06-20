package com.knockoutmod.client;

import com.knockoutmod.entity.KnockoutTestDummyEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

public class KnockoutTestDummyRenderer extends HumanoidMobRenderer<KnockoutTestDummyEntity, PlayerModel<KnockoutTestDummyEntity>> {
    public KnockoutTestDummyRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(KnockoutTestDummyEntity entity) {
        return DefaultPlayerSkin.getDefaultSkin(entity.getSkinUuid());
    }

    @Override
    protected boolean shouldShowName(KnockoutTestDummyEntity entity) {
        return entity.hasCustomName();
    }
}
