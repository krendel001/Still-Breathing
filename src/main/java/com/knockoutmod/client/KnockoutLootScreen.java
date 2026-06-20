package com.knockoutmod.client;

import com.knockoutmod.menu.KnockoutLootMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class KnockoutLootScreen extends AbstractKnockoutLootScreen<KnockoutLootMenu> {
    public KnockoutLootScreen(KnockoutLootMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
