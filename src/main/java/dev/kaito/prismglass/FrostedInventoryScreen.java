package dev.kaito.prismglass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Cosmetic-only InventoryScreen with untouched vanilla slot positions and input. */
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
        int x = this.leftPos, y = this.topPos;
        int tint = preferences.theme().color, opacity = preferences.opacity();

        // Glass geometry is positioned *behind* vanilla's 176 x 166 slot area.
        GlassPaint.surface(g, x - 8, y - 10, 192, 186, opacity, tint);
        // The player preview and crafting station are faceted floating panes.
        GlassPaint.surface(g, x + 6, y + 6, 80, 73, Math.max(35, opacity - 12), tint);
        GlassPaint.surface(g, x + 93, y + 6, 77, 73, Math.max(35, opacity - 12), tint);
        GlassPaint.line(g, x + 8, y + 80, 160, tint);
        GlassPaint.facet(g, x + 117, y + 4, 36);
        GlassPaint.jewel(g, x + 87, y - 5, tint);

        for (int i = 0; i < 4; i++) slot(g, x + 8, y + 8 + i * 18, tint);
        slot(g, x + 77, y + 62, tint); // offhand
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) slot(g, x + 98 + col * 18, y + 18 + row * 18, tint);
        }
        slot(g, x + 154, y + 28, tint); // crafting result
        g.drawString(this.font, ">", x + 142, y + 35, 0xFF867A62, false);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) slot(g, x + 8 + col * 18, y + 84 + row * 18, tint);
        }
        for (int col = 0; col < 9; col++) slot(g, x + 8 + col * 18, y + 142, tint);

        if (Minecraft.getInstance().player != null) {
            renderEntityInInventoryFollowsMouse(g, x + 26, y + 8, x + 75, y + 78,
                30, 0.0625f, mouseX, mouseY, Minecraft.getInstance().player);
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
        // Dark ink contrasts against pearlescent glass across day and night scenes.
        g.drawString(this.font, Component.translatable("prismglass.craft"), 99, 8, 0xFF23394B, false);
        g.drawString(this.font, Component.translatable("prismglass.inventory"), 8, 72, 0xFF253A4C, false);
    }
}
