package dev.kaito.prismglass;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;

/**
 * Glass background over vanilla AnvilScreen behavior.
 * Input, slots, edits, scrolling and networking are not replaced.
 */
public final class FrostedAnvilScreen extends AnvilScreen {
    private final CrystalPreferences preferences;

    public FrostedAnvilScreen(AnvilMenu menu, Inventory inventory,
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
        GlassContainerSkin.draw(g, leftPos, topPos, imageWidth, imageHeight,
            getMenu(), preferences);

        // The EditBox and its text remain owned by AnvilScreen.subInit().
        // Only its backing plate replaces the vanilla name-field texture.
        int editX = leftPos + 59, editY = topPos + 20;
        g.fillGradient(editX, editY, editX + 110, editY + 16,
            0x65273B50, 0x3F182D45);
        GlassPaint.rim(g, editX, editY, 110, 16);
        g.drawString(font, ">", leftPos + 106, topPos + 48, 0xFFEAF7FF, true);
        // Preserve the vanilla error icon and "Too Expensive" status logic.
        renderErrorIcon(g, leftPos, topPos);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        if (!preferences.isEnabled()) {
            super.renderLabels(g, mouseX, mouseY);
            return;
        }
        // AnvilScreen.renderLabels owns the live repair-cost/invalid-state text.
        // Render it unchanged so the amount and error conditions remain accurate.
        super.renderLabels(g, mouseX, mouseY);
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
