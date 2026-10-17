package dev.kaito.prismglass;

import net.minecraft.client.gui.GuiGraphics;

/** Primitive-only render path; no custom shaders or extra textures required. */
public final class GlassPaint {
    private GlassPaint() {}

    public static int argb(int alpha, int rgb) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    public static void panel(GuiGraphics g, int x, int y, int w, int h, int opacity, int tint) {
        if (w < 6 || h < 6) return;
        g.fill(x + 3, y + 4, x + w + 3, y + h + 4, 0x42000000);
        int alpha = Math.round(opacity * 2.0f);
        g.fillGradient(x + 2, y + 2, x + w - 2, y + h - 2,
            argb(alpha, 0x172C45), argb(Math.max(25, alpha - 42), 0x080D20));
        g.fill(x + 3, y + 2, x + w - 3, y + 3, argb(115, 0xFFFFFF));
        g.fill(x + 2, y + 3, x + 3, y + h - 3, argb(75, 0xFFFFFF));
        g.fill(x + w - 3, y + 3, x + w - 2, y + h - 3, argb(65, tint));
        g.fill(x + 3, y + h - 3, x + w - 3, y + h - 2, argb(80, tint));
        g.fill(x + 2, y + 2, x + 12, y + 3, argb(180, tint));
        g.fill(x + 2, y + 2, x + 3, y + 12, argb(175, tint));
        g.fill(x + w - 12, y + h - 3, x + w - 2, y + h - 2, argb(120, tint));
    }

    public static void frame(GuiGraphics g, int x, int y, int w, int h, int tint) {
        if (w < 6 || h < 6) return;
        g.fill(x, y, x + w, y + 1, argb(100, tint));
        g.fill(x, y + 1, x + 1, y + h - 1, argb(90, tint));
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, argb(90, tint));
        g.fill(x, y + h - 1, x + w, y + h, argb(100, tint));
        g.fill(x, y, x + 12, y + 2, argb(200, 0xFFFFFF));
    }
}
