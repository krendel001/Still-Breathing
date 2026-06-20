package com.knockoutmod.menu;

import com.knockoutmod.entity.KnockoutTestDummyEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DummyLootMenu extends AbstractKnockoutLootMenu {
    private static final int CONTAINER_ROWS = 4;

    private final KnockoutTestDummyEntity dummy;

    public DummyLootMenu(int containerId, Inventory looterInventory, FriendlyByteBuf extra) {
        this(containerId, looterInventory, extra.readVarInt());
    }

    public DummyLootMenu(int containerId, Inventory looterInventory, int dummyEntityId) {
        super(ModMenus.DUMMY_LOOT.get(), containerId, CONTAINER_ROWS);
        Player looter = looterInventory.player;
        if (!(looter.level().getEntity(dummyEntityId) instanceof KnockoutTestDummyEntity target)) {
            throw new IllegalArgumentException("Missing knockout dummy loot target");
        }
        this.dummy = target;

        addDummyArmorSlots();
        addDummyInventorySlots();
        addLooterInventorySlots(looterInventory);
        addLooterHotbarSlots(looterInventory);
    }

    private void addDummyArmorSlots() {
        int y = KnockoutLootLayout.containerSlotY(0);
        addSlot(new DummyEquipmentSlot(dummy, EquipmentSlot.HEAD, 8, y));
        addSlot(new DummyEquipmentSlot(dummy, EquipmentSlot.CHEST, 26, y));
        addSlot(new DummyEquipmentSlot(dummy, EquipmentSlot.LEGS, 44, y));
        addSlot(new DummyEquipmentSlot(dummy, EquipmentSlot.FEET, 62, y));
        addSlot(new DummyEquipmentSlot(dummy, EquipmentSlot.MAINHAND, 80, y));
    }

    private void addDummyInventorySlots() {
        for (int row = 0; row < 3; row++) {
            int y = KnockoutLootLayout.containerSlotY(row + 1);
            for (int col = 0; col < 9; col++) {
                addSlot(new DummyContainerSlot(dummy, col + row * 9, 8 + col * 18, y));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveStackFromVictimArea(index, slots.size() - 36);
    }

    @Override
    public boolean stillValid(Player player) {
        return dummy.isAlive() && dummy.isKnockedOut() && player.distanceToSqr(dummy) <= 16.0D;
    }

    private static Container equipmentContainer(KnockoutTestDummyEntity dummy, EquipmentSlot equipmentSlot) {
        return new Container() {
            @Override
            public int getContainerSize() {
                return 1;
            }

            @Override
            public boolean isEmpty() {
                return dummy.getItemBySlot(equipmentSlot).isEmpty();
            }

            @Override
            public ItemStack getItem(int index) {
                return dummy.getItemBySlot(equipmentSlot);
            }

            @Override
            public ItemStack removeItem(int index, int count) {
                ItemStack stack = dummy.getItemBySlot(equipmentSlot);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                ItemStack split = stack.split(count);
                dummy.setItemSlot(equipmentSlot, stack);
                return split;
            }

            @Override
            public ItemStack removeItemNoUpdate(int index) {
                ItemStack stack = dummy.getItemBySlot(equipmentSlot);
                dummy.setItemSlot(equipmentSlot, ItemStack.EMPTY);
                return stack;
            }

            @Override
            public void setItem(int index, ItemStack stack) {
                dummy.setItemSlot(equipmentSlot, stack);
            }

            @Override
            public void setChanged() {
            }

            @Override
            public boolean stillValid(Player player) {
                return dummy.isKnockedOut();
            }

            @Override
            public void clearContent() {
                dummy.setItemSlot(equipmentSlot, ItemStack.EMPTY);
            }
        };
    }

    private static final class DummyEquipmentSlot extends Slot {
        private final KnockoutTestDummyEntity dummy;

        private DummyEquipmentSlot(KnockoutTestDummyEntity dummy, EquipmentSlot equipmentSlot, int x, int y) {
            super(equipmentContainer(dummy, equipmentSlot), 0, x, y);
            this.dummy = dummy;
        }

        @Override
        public boolean mayPickup(Player player) {
            return dummy.isKnockedOut();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    private static final class DummyContainerSlot extends Slot {
        private final KnockoutTestDummyEntity dummy;

        private DummyContainerSlot(KnockoutTestDummyEntity dummy, int slotIndex, int x, int y) {
            super(dummy.getLootContainer(), slotIndex, x, y);
            this.dummy = dummy;
        }

        @Override
        public boolean mayPickup(Player player) {
            return dummy.isKnockedOut();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
