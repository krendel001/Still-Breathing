package com.knockoutmod.client;

import com.knockoutmod.menu.AbstractKnockoutLootMenu;
import com.knockoutmod.menu.KnockoutLootLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

abstract class AbstractKnockoutLootScreen<T extends AbstractKnockoutLootMenu> extends AbstractContainerScreen<T> {
    private static final ResourceLocation CHEST_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

    private final KnockoutLootLayout.Layout layout;

    protected AbstractKnockoutLootScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.layout = menu.getLayout();
        this.imageWidth = KnockoutLootLayout.IMAGE_WIDTH;
        this.imageHeight = layout.imageHeight();
        this.inventoryLabelY = layout.inventoryLabelY();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        int containerSectionHeight = layout.containerRows() * 18 + 17;
        graphics.blit(CHEST_TEXTURE, left, top, 0, 0, this.imageWidth, containerSectionHeight);
        graphics.blit(CHEST_TEXTURE, left, top + containerSectionHeight, 0, 126, this.imageWidth, 96);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
