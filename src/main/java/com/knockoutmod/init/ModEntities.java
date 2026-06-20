package com.knockoutmod.init;

import com.knockoutmod.KnockoutMod;
import com.knockoutmod.entity.KnockoutTestDummyEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = KnockoutMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, KnockoutMod.MOD_ID);

    public static final RegistryObject<EntityType<KnockoutTestDummyEntity>> TEST_DUMMY =
            ENTITIES.register("test_dummy", () -> EntityType.Builder
                    .of(KnockoutTestDummyEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("test_dummy"));

    private ModEntities() {
    }

    public static void register(net.minecraftforge.eventbus.api.IEventBus modBus) {
        ENTITIES.register(modBus);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(TEST_DUMMY.get(), KnockoutTestDummyEntity.createAttributes().build());
    }
}
