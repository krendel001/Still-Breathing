package com.knockoutmod.client;

import com.knockoutmod.menu.DummyLootMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DummyLootScreen extends AbstractKnockoutLootScreen<DummyLootMenu> {
    public DummyLootScreen(DummyLootMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
