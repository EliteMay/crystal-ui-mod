package dev.kaito.prismglass;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Clear glass look using Minecraft GUI primitives. The world remains visible through the
 * pane; only its rim carries strong reflection. Real-time background refraction is not
 * simulated here, because that requires a separate framebuffer/post-process pass.
 */
public final class GlassPaint {
    private GlassPaint() {}

    public static int argb(int alpha, int rgb) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    /** Default glass tint is intentionally under 25% opacity. */
    public static int glassAlpha(int opacity) {
        return ClearGlassAlpha.forOpacity(opacity);
    }

    private static void roundedGradient(GuiGraphics g, int x, int y, int w, int h,
                                        int radius, int top, int bottom) {
        if (w <= radius * 2 || h <= radius * 2) return;
        g.fillGradient(x, y + radius, x + w, y + h - radius, top, bottom);
        for (int i = 0; i < radius; i++) {
            int inset = (int) Math.ceil(radius -
                    Math.sqrt(radius * radius - Math.pow(radius - i - 0.5, 2)));
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, top);
            g.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, bottom);
        }
    }

    /**
     * Main inventory panel: most of the image is physically clear, with localized
     * white rim light, gold glints, and a small-depth shadow on the bottom edge.
     */
    public static void surface(GuiGraphics g, int x, int y, int w, int h,
                               int opacity, int tint) {
        if (w < 14 || h < 14) return;
        if (GlassRenderer.pane(g, x, y, w, h, opacity, tint)) return;
        int a = glassAlpha(opacity);
        // Floating shadow is outside the panel, not a dark gradient over the world.
        roundedGradient(g, x - 2, y + 3, w + 4, h + 3, 5,
            0x10000814, 0x29000A15);
        roundedGradient(g, x, y, w, h, 4,
            argb(a, 0xE6F7FF), argb(Math.max(12, a - 7), 0xB9D6EA));

        // Distinct thin Fresnel-style edge reflection gives the pane its shape.
        g.fill(x + 6, y + 1, x + w - 6, y + 2, 0xB8FFFFFF);
        g.fill(x + 5, y + 2, x + 6, y + h - 5, 0x9BFFFFFF);
        g.fill(x + w - 6, y + 4, x + w - 5, y + h - 5, 0x68C8E3F0);
        g.fill(x + 6, y + h - 3, x + w - 6, y + h - 2, 0x8E344B5B);
        g.fill(x + 6, y + h - 4, x + w - 6, y + h - 3, 0x50FFFFFF);
        g.fill(x + 10, y + 3, x + w / 3, y + 4, 0x70FFF7E9);
        g.fill(x + w - 22, y + 2, x + w - 9, y + 3, argb(70, tint));
        // Small localized glare instead of a full-panel white wash.
        if (w > 55 && h > 30) {
            g.fillGradient(x + 7, y + 5, x + Math.min(44, w - 8), y + 15,
                0x20FFFFFF, 0x00FFFFFF);
        }
    }

    /** Batched slot wells (top-left corners in xy pairs): GPU etching, or legacy rims. */
    public static void slots(GuiGraphics g, int[] xy, int count, int tint) {
        if (GlassRenderer.slots(g, xy, count, tint)) return;
        for (int i = 0; i < count; i++) slot(g, xy[i * 2], xy[i * 2 + 1], tint);
    }

    /** Etched transparent inventory slot. One-pixel rims, not white opaque boxes. */
    public static void slot(GuiGraphics g, int x, int y, int tint) {
        g.fillGradient(x + 1, y + 1, x + 17, y + 17,
            0x211C3544, 0x2D122433);
        g.fill(x, y, x + 18, y + 1, 0xA5FFFFFF);
        g.fill(x, y + 1, x + 1, y + 18, 0x78FFFFFF);
        g.fill(x + 17, y + 1, x + 18, y + 18, 0x61415768);
        g.fill(x + 1, y + 17, x + 18, y + 18, 0x68405663);
        g.fill(x + 3, y + 2, x + 14, y + 3, argb(37, tint));
    }

    /** Optical edge only, without adding another opaque pane on top. */
    public static void rim(GuiGraphics g, int x, int y, int w, int h) {
        if (w < 14 || h < 14) return;
        g.fill(x + 4, y, x + w - 4, y + 1, 0x7DFFFFFF);
        g.fill(x, y + 4, x + 1, y + h - 4, 0x72E7F7FF);
        g.fill(x + w - 1, y + 4, x + w, y + h - 4, 0x51455B72);
        g.fill(x + 4, y + h - 1, x + w - 4, y + h, 0x58435769);
    }

    /** Soft translucent section divider. */
    public static void line(GuiGraphics g, int x, int y, int width, int tint) {
        g.fill(x, y, x + width, y + 1, 0x71FFFFFF);
        g.fill(x + width / 4, y + 1, x + width * 3 / 4, y + 2, 0x28405267);
    }

    /** Settings need additional neutral contrast; inventory must remain clear. */
    public static void panel(GuiGraphics g, int x, int y, int w, int h, int opacity, int tint) {
        if (GlassRenderer.pane(g, x, y, w, h, opacity, tint)) return;
        surface(g, x, y, w, h, opacity, tint);
        if (w > 34 && h > 14) {
            roundedGradient(g, x + 3, y + 3, w - 6, h - 6, 3,
                0x70E7F1F6, 0x76DBE9F1);
        }
    }

    /** Quiet edge glint for the PRISM button only. */
    public static void jewel(GuiGraphics g, int cx, int cy, int tint) {
        g.fill(cx - 1, cy - 3, cx + 1, cy + 4, 0x77FFFFFF);
        g.fill(cx - 3, cy, cx + 4, cy + 1, argb(105, tint));
        g.fill(cx, cy, cx + 1, cy + 1, 0xD3FFFFFF);
    }
}
