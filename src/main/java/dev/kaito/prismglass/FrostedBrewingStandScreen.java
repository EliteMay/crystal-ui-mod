package dev.kaito.prismglass;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BrewingStandMenu;

/**
 * Glass background over vanilla BrewingStandScreen behavior.
 * Input, slots, edits, scrolling and networking are not replaced.
 */
public final class FrostedBrewingStandScreen extends BrewingStandScreen {
    private final CrystalPreferences preferences;

    public FrostedBrewingStandScreen(BrewingStandMenu menu, Inventory inventory,
                            Component title, CrystalPreferences preferences) {
        super(menu, inventory, title);
        this.preferences = preferences;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        if (!preferences.isEnabled()) {
            super.renderBg(g, partialTick, mouseX, mouseY);
            return;
        }
        GlassBrewingSkin.draw(g, leftPos, topPos, imageWidth, imageHeight,
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
