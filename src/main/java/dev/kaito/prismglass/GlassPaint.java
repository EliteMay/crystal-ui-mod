package dev.kaito.prismglass;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Small texture-free glass renderer. Deliberately avoids shaders, framebuffers and state
 * mutations so it remains safe with typical Forge render pipelines.
 */
public final class GlassPaint {
    private GlassPaint() {}

    public static int argb(int alpha, int rgb) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    private static void roundedGradient(GuiGraphics g, int x, int y, int w, int h,
                                        int radius, int top, int bottom) {
        if (w <= radius * 2 || h <= radius * 2) return;
        g.fillGradient(x, y + radius, x + w, y + h - radius, top, bottom);
        for (int i = 0; i < radius; i++) {
            // A stepped curve at native GUI pixels keeps edges crisp at all GUI scales.
            int inset = (int) Math.ceil(radius - Math.sqrt(radius * radius - Math.pow(radius - i - 0.5, 2)));
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, top);
            g.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, bottom);
        }
    }

    /** Matte tinted glass with recessed border, soft shadow and a restrained reflection. */
    public static void surface(GuiGraphics g, int x, int y, int w, int h, int opacity, int tint) {
        if (w < 14 || h < 14) return;
        int alpha = Math.max(85, Math.min(220, Math.round(opacity * 2.2f)));
        roundedGradient(g, x - 1, y + 3, w + 2, h + 2, 7, 0x36000000, 0x66000000);
        roundedGradient(g, x, y, w, h, 6,
                        argb(alpha, 0x283848), argb(Math.min(235, alpha + 22), 0x0C1724));
        // Subtle cold tint in the header and bottom edge, with no neon glow.
        g.fillGradient(x + 9, y + 4, x + w - 9, y + Math.min(23, h / 2),
                       argb(24, tint), argb(0, tint));
        g.fill(x + 9, y + 2, x + w - 9, y + 3, 0x7AFFFFFF);
        g.fill(x + 4, y + 7, x + 5, y + h - 9, 0x42EDF7FF);
        g.fill(x + w - 5, y + 7, x + w - 4, y + h - 9, argb(36, tint));
        g.fill(x + 9, y + h - 4, x + w - 9, y + h - 3, 0x25C0DDEB);
        // Optical "sheen": diffuse at the upper left, fading toward the panel body.
        g.fillGradient(x + 11, y + 6, x + Math.min(w - 11, 48), y + Math.min(h - 6, 30),
                       0x19FFFFFF, 0x00FFFFFF);
    }

    /** Etched inset for the actual vanilla inventory slots. Drawing never moves hitboxes. */
    public static void slot(GuiGraphics g, int x, int y, int tint) {
        g.fill(x, y + 1, x + 18, y + 18, 0x3A020810);
        g.fillGradient(x + 1, y + 1, x + 17, y + 17, 0x45253B49, 0x5B102030);
        g.fill(x + 2, y + 1, x + 16, y + 2, 0x5BFFFFFF);
        g.fill(x + 1, y + 2, x + 2, y + 15, 0x30FFFFFF);
        g.fill(x + 16, y + 2, x + 17, y + 16, argb(35, tint));
        g.fill(x + 2, y + 16, x + 16, y + 17, 0x28000000);
    }

    public static void line(GuiGraphics g, int x, int y, int width, int tint) {
        g.fillGradient(x, y, x + width, y + 1, 0x35FFFFFF, argb(24, tint));
    }

    /** Backward-compatible entry point used by buttons and settings. */
    public static void panel(GuiGraphics g, int x, int y, int w, int h, int opacity, int tint) {
        surface(g, x, y, w, h, opacity, tint);
    }
}
