package com.knockoutmod.menu;

import com.knockoutmod.config.KnockoutConfig;
import com.knockoutmod.knockout.KnockoutData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class KnockoutLootMenu extends AbstractKnockoutLootMenu {
    private final Player victim;

    public KnockoutLootMenu(int containerId, Inventory looterInventory, FriendlyByteBuf extra) {
        this(containerId, looterInventory, looterInventory.player.level().getPlayerByUUID(extra.readUUID()));
    }

    public KnockoutLootMenu(int containerId, Inventory looterInventory, Player victim) {
        super(
                ModMenus.KNOCKOUT_LOOT.get(),
                containerId,
                KnockoutConfig.SERVER.allowHotbarLoot.get() ? 5 : 4
        );
        this.victim = victim;

        if (victim == null) {
            throw new IllegalArgumentException("Missing knockout loot victim");
        }

        addVictimArmorSlots();
        addVictimOffhandSlot();
        addVictimMainInventorySlots();
        if (KnockoutConfig.SERVER.allowHotbarLoot.get()) {
            addVictimHotbarSlots();
        }
        addLooterInventorySlots(looterInventory);
        addLooterHotbarSlots(looterInventory);
    }

    private void addVictimArmorSlots() {
        int y = KnockoutLootLayout.containerSlotY(0);
        addSlot(new VictimSlot(victim, 39, 8, y));
        addSlot(new VictimSlot(victim, 38, 26, y));
        addSlot(new VictimSlot(victim, 37, 44, y));
        addSlot(new VictimSlot(victim, 36, 62, y));
    }

    private void addVictimOffhandSlot() {
        addSlot(new VictimSlot(victim, 40, 80, KnockoutLootLayout.containerSlotY(0)));
    }

    private void addVictimMainInventorySlots() {
        for (int row = 0; row < 3; row++) {
            int y = KnockoutLootLayout.containerSlotY(row + 1);
            for (int col = 0; col < 9; col++) {
                addSlot(new VictimSlot(victim, 9 + col + row * 9, 8 + col * 18, y));
            }
        }
    }

    private void addVictimHotbarSlots() {
        int y = KnockoutLootLayout.containerSlotY(4);
        for (int col = 0; col < 9; col++) {
            addSlot(new VictimSlot(victim, col, 8 + col * 18, y));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveStackFromVictimArea(index, slots.size() - 36);
    }

    @Override
    public boolean stillValid(Player player) {
        return victim.isAlive()
                && KnockoutData.isKnockedOut(victim)
                && player.distanceToSqr(victim) <= 16.0D;
    }

    public Player getVictim() {
        return victim;
    }

    private static final class VictimSlot extends Slot {
        private final Player victim;

        private VictimSlot(Player victim, int slotIndex, int x, int y) {
            super(victim.getInventory(), slotIndex, x, y);
            this.victim = victim;
        }

        @Override
        public boolean mayPickup(Player player) {
            return player != victim && KnockoutData.isKnockedOut(victim);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
