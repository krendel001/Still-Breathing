package com.knockoutmod.menu;

import com.knockoutmod.entity.KnockoutTestDummyEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkHooks;

public final class DummyLootMenuProvider implements MenuProvider {
    private final KnockoutTestDummyEntity dummy;

    private DummyLootMenuProvider(KnockoutTestDummyEntity dummy) {
        this.dummy = dummy;
    }

    public static void open(ServerPlayer looter, KnockoutTestDummyEntity dummy) {
        if (!dummy.isKnockedOut() || looter.distanceToSqr(dummy) > 16.0D) {
            return;
        }

        NetworkHooks.openScreen(
                looter,
                new DummyLootMenuProvider(dummy),
                buf -> buf.writeVarInt(dummy.getId())
        );
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("knockoutmod.menu.dummy_loot_title", dummy.getDummyName());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DummyLootMenu(containerId, inventory, dummy.getId());
    }
}
