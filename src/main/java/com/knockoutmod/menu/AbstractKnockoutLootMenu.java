package com.knockoutmod.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractKnockoutLootMenu extends AbstractContainerMenu implements KnockoutLootMenuView {
    private final KnockoutLootLayout.Layout layout;

    protected AbstractKnockoutLootMenu(MenuType<?> menuType, int containerId, int containerRows) {
        super(menuType, containerId);
        this.layout = KnockoutLootLayout.forContainerRows(containerRows);
    }

    @Override
    public KnockoutLootLayout.Layout getLayout() {
        return layout;
    }

    protected void addLooterInventorySlots(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(
                        inventory,
                        9 + col + row * 9,
                        8 + col * 18,
                        layout.playerInventoryY() + row * 18
                ));
            }
        }
    }

    protected void addLooterHotbarSlots(Inventory inventory) {
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, layout.playerHotbarY()));
        }
    }

    protected ItemStack quickMoveStackFromVictimArea(int index, int victimSlotCount) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return itemStack;
        }

        ItemStack stackInSlot = slot.getItem();
        itemStack = stackInSlot.copy();

        if (index < victimSlotCount) {
            if (!this.moveItemStackTo(stackInSlot, victimSlotCount, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stackInSlot, 0, victimSlotCount, false)) {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return itemStack;
    }
}
