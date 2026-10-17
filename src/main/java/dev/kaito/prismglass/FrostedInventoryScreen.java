package dev.kaito.prismglass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;

/** Preserve vanilla inventory interactions; replace cosmetic background only. */
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

        // One transparent pane. Do not layer opaque cards over it.
        GlassPaint.surface(g, x - 7, y - 9, 190, 184, opacity, tint);
        GlassPaint.rim(g, x + 6, y + 6, 80, 73);
        GlassPaint.rim(g, x + 93, y + 6, 77, 73);
        GlassPaint.line(g, x + 8, y + 81, 160, tint);

        for (int i = 0; i < 4; i++) slot(g, x + 8, y + 8 + i * 18, tint);
        slot(g, x + 77, y + 62, tint); // offhand
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) slot(g, x + 98 + col * 18, y + 18 + row * 18, tint);
        }
        slot(g, x + 154, y + 28, tint); // crafting output
        g.drawString(this.font, ">", x + 142, y + 35, 0xFFE7F0F6, true);
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
        }
        // No labels on the glass: previous INVENTORY title overlapped armor/offhand
        // at GUI scale 3-4, while the CRAFT title collided with the frame.
    }
}
