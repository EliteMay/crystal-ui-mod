package dev.kaito.prismglass;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Shader-free luminous crystal renderer. All geometry is behind the real Minecraft
 * widgets, so item hitboxes, text focus and inventory packets are left unchanged.
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
            int inset = (int) Math.ceil(radius - Math.sqrt(radius * radius -
                Math.pow(radius - i - 0.5, 2)));
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, top);
            g.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, bottom);
        }
    }

    /** A very restrained halo outside a large panel, not a solid neon frame. */
    private static void halo(GuiGraphics g, int x, int y, int w, int h, int tint) {
        for (int i = 4; i >= 1; i--) {
            int a = (5 - i) * 2 + 2;
            g.fill(x - i, y + 9, x - i + 1, y + h - 10, argb(a, tint));
            g.fill(x + w + i - 1, y + 9, x + w + i, y + h - 10, argb(a, tint));
            g.fill(x + 9, y - i, x + w - 9, y - i + 1, argb(a, tint));
        }
    }

    /**
     * Seraph crystal: milky translucent glass, cool shadows and white-gold
     * edge highlights. This is an approximation, not a real blur/refraction shader.
     */
    public static void surface(GuiGraphics g, int x, int y, int w, int h, int opacity, int tint) {
        if (w < 14 || h < 14) return;
        int top = Math.max(90, Math.min(225, Math.round(opacity * 1.95f)));
        int bottom = Math.max(102, Math.min(231, Math.round(opacity * 2.12f)));
        if (w > 110 && h > 90) halo(g, x, y, w, h, tint);

        // Depth belongs outside the translucent body: the crystal itself stays light.
        roundedGradient(g, x - 2, y + 4, w + 4, h + 2, 7, 0x19091C30, 0x3C051323);
        roundedGradient(g, x - 1, y + 2, w + 2, h + 1, 7, 0x1DDBEEFF, 0x33090C14);
        roundedGradient(g, x, y, w, h, 6,
            argb(top, 0xF7FBFF), argb(bottom, 0xD7EBF4));

        // Pearlescent reflection stays at the top; the middle is kept uncluttered.
        g.fillGradient(x + 8, y + 5, x + w - 8, y + Math.min(h - 7, 22),
            0x39FFFFFF, 0x00FFFFFF);
        g.fill(x + 9, y + 2, x + w - 9, y + 3, 0xBAFFFFFF);
        g.fill(x + 4, y + 9, x + 5, y + h - 9, 0x80FFFFFF);
        g.fill(x + w - 5, y + 9, x + w - 4, y + h - 9, 0x5AD7E7F4);
        g.fill(x + 9, y + h - 4, x + w - 9, y + h - 3, 0x6FA4C1D2);

        // Extremely fine warm metallic trim. Gold provides a sacred accent
        // without becoming a thick yellow outline.
        g.fill(x + 13, y + 4, x + w / 2, y + 5, 0x44FFF2D3);
        g.fill(x + w / 2, y + 4, x + w - 13, y + 5, argb(38, tint));
        g.fill(x + 8, y + h - 6, x + w - 8, y + h - 5, 0x22E6CFA6);

        // Prism highlights: three very subtle color bands in the glass edge.
        if (w >= 65 && h >= 25) {
            int third = (w - 30) / 3;
            g.fill(x + 15, y + 6, x + 15 + third, y + 7, 0x30AEDDFB);
            g.fill(x + 15 + third, y + 6, x + 15 + third * 2, y + 7, 0x2CDECCFF);
            g.fill(x + 15 + third * 2, y + 6, x + w - 15, y + 7, 0x33FFF0C6);
        }
    }

    /** Engraved cut-crystal ornamental shape. No triangles or custom shaders. */
    public static void jewel(GuiGraphics g, int cx, int cy, int tint) {
        g.fill(cx - 1, cy - 5, cx + 1, cy + 6, 0x76FFFFFF);
        g.fill(cx - 4, cy - 1, cx + 5, cy + 1, 0x8CFFFFFF);
        g.fill(cx - 1, cy - 3, cx + 1, cy + 4, argb(130, tint));
        g.fill(cx - 3, cy, cx + 4, cy + 1, 0x9BFFECCC);
        g.fill(cx, cy - 1, cx + 1, cy + 2, 0xFFEFFFFF);
    }

    /** One faint etched facet across the header, away from inventory slots. */
    public static void facet(GuiGraphics g, int x, int y, int length) {
        for (int i = 0; i < length; i++) {
            int py = y + i / 4;
            g.fill(x + i, py, x + i + 1, py + 1, 0x20FFFFFF);
        }
    }

    /** Fixed 18px geometry matches vanilla item backgrounds exactly. */
    public static void slot(GuiGraphics g, int x, int y, int tint) {
        g.fillGradient(x, y, x + 18, y + 18, 0x45FFFFFF, 0x35B7D6E3);
        g.fill(x + 1, y + 1, x + 17, y + 2, 0x9AFFFFFF);
        g.fill(x + 1, y + 2, x + 2, y + 16, 0x7AFFFFFF);
        g.fill(x + 16, y + 2, x + 17, y + 16, 0x65C9B6A2);
        g.fill(x + 2, y + 16, x + 16, y + 17, 0x6DA4B8C6);
        g.fillGradient(x + 3, y + 3, x + 15, y + 10, 0x20FFFFFF, 0x00FFFFFF);
        g.fill(x, y, x + 18, y + 1, argb(34, tint));
    }

    public static void line(GuiGraphics g, int x, int y, int width, int tint) {
        g.fill(x, y, x + width, y + 1, 0x56FFFFFF);
        g.fill(x + width / 3, y + 1, x + 2 * width / 3, y + 2, argb(35, tint));
    }

    /** Compatibility for settings widgets, which need the same visual language. */
    public static void panel(GuiGraphics g, int x, int y, int w, int h, int opacity, int tint) {
        surface(g, x, y, w, h, opacity, tint);
    }
}
