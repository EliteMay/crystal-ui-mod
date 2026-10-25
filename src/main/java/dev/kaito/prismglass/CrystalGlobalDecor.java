package dev.kaito.prismglass;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Non-invasive visual chrome for vanilla menus and specialized containers.
 * Draws after vanilla controls; never captures input, replaces menus, or moves widgets.
 */
public final class CrystalGlobalDecor {
    private CrystalGlobalDecor() {}

    static boolean isVanillaScreenClass(String className) {
        return className.startsWith("net.minecraft.client.gui.screens.");
    }

    static boolean isExcluded(String className) {
        // Inputs requiring precise accessibility or emergency feedback retain vanilla visuals.
        return className.contains(".ChatScreen") ||
               className.contains(".DeathScreen") ||
               className.contains(".ReceivingLevelScreen");
    }

    public static void render(Screen screen, GuiGraphics graphics,
                              CrystalPreferences preferences) {
        if (!preferences.isEnabled() || screen.width < 180 || screen.height < 120) return;
        String className = screen.getClass().getName();
        if (!isVanillaScreenClass(className) || isExcluded(className)) return;

        if (screen instanceof AbstractContainerScreen<?> container) {
            // Special-purpose screens keep vanilla progress, book and input controls.
            int x = container.getGuiLeft();
            int y = container.getGuiTop();
            int w = container.getXSize();
            int h = container.getYSize();
            GlassPaint.rim(graphics, x - 8, y - 10, w + 16, h + 19);
            return;
        }

        // Menu chrome stays along the outside edge, never across text or list contents.
        int w = screen.width;
        int h = screen.height;
        int tint = preferences.theme().color;
        GlassPaint.rim(graphics, 7, 7, w - 14, h - 14);
        GlassPaint.line(graphics, 24, h - 17, Math.min(94, w / 4), tint);
        GlassPaint.line(graphics, w - 24 - Math.min(94, w / 4),
            h - 17, Math.min(94, w / 4), tint);

        // Vanilla button behavior remains intact; a thin glass reflection is cosmetic.
        for (GuiEventListener child : screen.children()) {
            if (!(child instanceof AbstractButton button) || !button.visible) continue;
            int bw = button.getWidth(), bh = button.getHeight();
            if (bw < 22 || bh < 16) continue;
            int bx = button.getX(), by = button.getY();
            if (bx < 0 || by < 0 || bx + bw > w || by + bh > h) continue;
            graphics.fillGradient(bx + 2, by + 2, bx + bw - 2, by + bh - 2,
                0x0EEAF7FF, 0x05E5D7FF);
            GlassPaint.rim(graphics, bx, by, bw, bh);
        }
    }
}
