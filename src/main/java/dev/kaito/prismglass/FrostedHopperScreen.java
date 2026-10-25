package dev.kaito.prismglass;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HopperMenu;

/** Vanilla HopperScreen logic, input and slot coordinates with Prism's glass visuals. */
public final class FrostedHopperScreen extends HopperScreen {
    private final CrystalPreferences glassPreferences;

    public FrostedHopperScreen(HopperMenu menu, Inventory inventory, Component title,
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
