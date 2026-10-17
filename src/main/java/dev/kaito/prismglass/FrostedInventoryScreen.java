package dev.kaito.prismglass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Cosmetic InventoryScreen subclass. All vanilla slot actions, recipe book, player inventory,
 * networking and item rendering stay in the original InventoryScreen implementation.
 */
public final class FrostedInventoryScreen extends InventoryScreen {
    private final CrystalPreferences preferences;

    public FrostedInventoryScreen(Player player, CrystalPreferences preferences) {
        super(player);
        this.preferences = preferences;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        if (!preferences.isEnabled()) {
            super.renderBg(g, partialTick, mouseX, mouseY);
            return;
        }
        int x = this.leftPos;
        int y = this.topPos;
        int tint = preferences.theme().color;
        int opacity = preferences.opacity();

        // Vanilla's 176 x 166 coordinates remain unchanged for every slot.
        GlassPaint.surface(g, x - 8, y - 10, 192, 186, opacity, tint);
        GlassPaint.surface(g, x + 6, y + 6, 80, 73, Math.max(30, opacity - 15), tint);
        GlassPaint.surface(g, x + 93, y + 6, 77, 73, Math.max(30, opacity - 15), tint);
        GlassPaint.line(g, x + 8, y + 80, 160, tint);

        // Vanilla slot positions; for 16px item sprites the background is 18 x 18.
        for (int i = 0; i < 4; i++) slot(g, x + 8, y + 8 + i * 18, tint);
        slot(g, x + 77, y + 62, tint); // offhand
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) slot(g, x + 98 + j * 18, y + 18 + i * 18, tint);
        }
        slot(g, x + 154, y + 28, tint); // crafting output
        g.drawString(this.font, ">", x + 142, y + 35, 0xFFDAEAF2, false);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) slot(g, x + 8 + col * 18, y + 84 + row * 18, tint);
        }
        for (int col = 0; col < 9; col++) slot(g, x + 8 + col * 18, y + 142, tint);

        // Preserve animated player model with the exact vanilla preview geometry.
        if (Minecraft.getInstance().player != null) {
            renderEntityInInventoryFollowsMouse(g, x + 26, y + 8, x + 75, y + 78,
                                               30, 0.0625f, mouseX, mouseY,
                                               Minecraft.getInstance().player);
        }
    }

    private static void slot(GuiGraphics g, int x, int y, int tint) {
        GlassPaint.slot(g, x - 1, y - 1, tint);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        if (!preferences.isEnabled()) {
            super.renderLabels(g, mouseX, mouseY);
            return;
        }
        g.drawString(this.font, Component.translatable("prismglass.craft"), 99, 8, 0xFFDFEDF4, false);
        g.drawString(this.font, Component.translatable("prismglass.inventory"), 8, 72, 0xFFDBEBF3, false);
    }
}
