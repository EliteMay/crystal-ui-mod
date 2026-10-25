package dev.kaito.prismglass;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;

/** Vanilla CraftingScreen logic, input and slot coordinates with Prism's glass visuals. */
public final class FrostedCraftingScreen extends CraftingScreen {
    private final CrystalPreferences glassPreferences;

    public FrostedCraftingScreen(CraftingMenu menu, Inventory inventory, Component title,
                             CrystalPreferences preferences) {
        super(menu, inventory, title);
        this.glassPreferences = preferences;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (!glassPreferences.isEnabled()) {
            super.renderBg(graphics, partialTick, mouseX, mouseY);
            return;
        }
        GlassContainerSkin.draw(graphics, leftPos, topPos, imageWidth, imageHeight,
            getMenu(), glassPreferences);
        // Crafting book and result slots remain vanilla; only the decorative arrow is restored.
        graphics.drawString(font, ">", leftPos + 125, topPos + 35, 0xFFE8F2FC, true);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!glassPreferences.isEnabled()) {
            super.renderLabels(graphics, mouseX, mouseY);
            return;
        }
        GlassContainerSkin.label(graphics, font, title, titleLabelX, titleLabelY);
        GlassContainerSkin.label(graphics, font, playerInventoryTitle,
            inventoryLabelX, inventoryLabelY);
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        if (!glassPreferences.isEnabled()) super.renderBlurredBackground(partialTick);
    }

    @Override
    public void renderTransparentBackground(GuiGraphics graphics) {
        if (!glassPreferences.isEnabled()) {
            super.renderTransparentBackground(graphics);
            return;
        }
        GlassContainerSkin.screenBackground(graphics, width, height);
    }
}
