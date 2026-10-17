package dev.kaito.prismglass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;

/** Preserve vanilla inventory interactions; replace cosmetic background only. */
public final class FrostedInventoryScreen extends InventoryScreen {
    private static final boolean DEBUG = Boolean.getBoolean("prismglass.debug");
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
        if (DEBUG) System.out.println("[PrismGlass] renderBg ready=" + GlassShaders.ready());
        int x = this.leftPos;
        int y = this.topPos;
        int tint = preferences.theme().color;
        int opacity = preferences.opacity();

        // One transparent pane. Do not layer opaque cards over it.
        GlassPaint.surface(g, x - 7, y - 9, 190, 184, opacity, tint);
        GlassPaint.rim(g, x + 6, y + 6, 80, 73);
        GlassPaint.rim(g, x + 93, y + 6, 77, 73);
        GlassPaint.line(g, x + 8, y + 81, 160, tint);

        int[] xy = new int[2 * 46];
        int n = 0;
        for (int i = 0; i < 4; i++) { xy[n++] = x + 7; xy[n++] = y + 7 + i * 18; }
        xy[n++] = x + 76; xy[n++] = y + 61; // offhand
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) { xy[n++] = x + 97 + col * 18; xy[n++] = y + 17 + row * 18; }
        }
        xy[n++] = x + 153; xy[n++] = y + 27; // crafting output
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) { xy[n++] = x + 7 + col * 18; xy[n++] = y + 83 + row * 18; }
        }
        for (int col = 0; col < 9; col++) { xy[n++] = x + 7 + col * 18; xy[n++] = y + 141; }
        GlassPaint.slots(g, xy, n / 2, tint);
        g.drawString(this.font, ">", x + 142, y + 35, 0xFFE7F0F6, true);

        if (Minecraft.getInstance().player != null) {
            renderEntityInInventoryFollowsMouse(g, x + 26, y + 8, x + 75, y + 78,
                30, 0.0625f, mouseX, mouseY, Minecraft.getInstance().player);
        }
    }

    /**
     * AbstractContainerScreen.renderBackground() calls renderBg(), so it must not be overridden.
     * The glass does its own local blur: drop vanilla's full-screen blur and use a light dim.
     */
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
        g.fillGradient(0, 0, this.width, this.height, 0x3A06101C, 0x5A06101C);
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
