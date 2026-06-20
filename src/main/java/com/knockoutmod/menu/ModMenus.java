package com.knockoutmod.menu;

import com.knockoutmod.KnockoutMod;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, KnockoutMod.MOD_ID);

    public static final RegistryObject<MenuType<KnockoutLootMenu>> KNOCKOUT_LOOT =
            MENUS.register("knockout_loot", () -> IForgeMenuType.create(KnockoutLootMenu::new));

    public static final RegistryObject<MenuType<DummyLootMenu>> DUMMY_LOOT =
            MENUS.register("dummy_loot", () -> IForgeMenuType.create(DummyLootMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
