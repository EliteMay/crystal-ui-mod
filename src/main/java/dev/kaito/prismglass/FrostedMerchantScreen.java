package dev.kaito.prismglass;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;

/**
 * Glass background over vanilla MerchantScreen behavior.
 * Input, slots, edits, scrolling and networking are not replaced.
 */
public final class FrostedMerchantScreen extends MerchantScreen {
    private final CrystalPreferences preferences;

    public FrostedMerchantScreen(MerchantMenu menu, Inventory inventory,
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

        // Visual tracks for the vanilla trade list. The parent still owns
        // offer selection, paging, scrolling, prices and sold-out indicators.
        int tint = preferences.theme().color;
        int listX = leftPos + 6, listY = topPos + 17;
        for (int row = 0; row < 7; row++) {
            int rowY = listY + row * 20;
            g.fillGradient(listX, rowY, listX + 91, rowY + 18,
                0x251E3D57, 0x0A182B3D);
            GlassPaint.rim(g, listX, rowY, 91, 18);
        }
        GlassPaint.line(g, leftPos + 103, topPos + 12,
            Math.max(20, imageWidth - 111), tint);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        if (!preferences.isEnabled()) {
            super.renderLabels(g, mouseX, mouseY);
            return;
        }
        // Vanilla MerchantScreen owns XP labels and trade status text.
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
