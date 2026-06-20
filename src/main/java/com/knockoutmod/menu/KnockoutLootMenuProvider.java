package com.knockoutmod.menu;

import com.knockoutmod.knockout.KnockoutData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkHooks;

public record KnockoutLootMenuProvider(Player victim) implements MenuProvider {
    public static void open(ServerPlayer looter, Player victim) {
        if (!KnockoutData.isKnockedOut(victim) || looter == victim) {
            return;
        }
        if (looter.distanceToSqr(victim) > 16.0D) {
            return;
        }

        NetworkHooks.openScreen(
                looter,
                new KnockoutLootMenuProvider(victim),
                buf -> buf.writeUUID(victim.getUUID())
        );
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("knockoutmod.menu.loot_title", victim.getDisplayName());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new KnockoutLootMenu(containerId, inventory, victim);
    }
}
