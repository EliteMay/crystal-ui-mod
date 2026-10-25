package dev.kaito.prismglass;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.SmokerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SmokerMenu;

/**
 * Keeps Minecraft's furnace recipe book, slots, and input behavior.
 * Only the background and labels are redrawn using Prism's shared glass style.
 */
public final class FrostedSmokerScreen extends SmokerScreen {
    private final CrystalPreferences preferences;

    public FrostedSmokerScreen(SmokerMenu menu, Inventory inventory, Component title,
                              CrystalPreferences preferences) {
        super(menu, inventory, title);
        this.preferences = preferences;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        if (!preferences.isEnabled()) {
            super.renderBg(g, partialTick, mouseX, mouseY);
            return;
        }
        GlassFurnaceSkin.draw(g, leftPos, topPos, imageWidth, imageHeight,
            getMenu(), preferences);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        if (!preferences.isEnabled()) {
            super.renderLabels(g, mouseX, mouseY);
            return;
        }
        GlassContainerSkin.label(g, font, title, titleLabelX, titleLabelY);
        GlassContainerSkin.label(g, font, playerInventoryTitle,
            inventoryLabelX, inventoryLabelY);
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        if (!preferences.isEnabled()) super.renderBlurredBackground(partialTick);
    }

    @Override
    public void renderTransparentBackground(GuiGraphics g) {
        if (!preferences.isEnabled()) {
            super.renderTransparentBackground(g);
            return;
        }
        GlassContainerSkin.screenBackground(g, width, height);
    }
}
