package dev.kaito.prismglass;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * High-contrast pixel text without the opaque label stickers introduced in v0.6.
 * A one-GUI-pixel pearl outline keeps ink readable over both bright skies and dark blocks.
 */
public final class GlassTypography {
    private GlassTypography() {}

    public static void centered(GuiGraphics g, Font font, Component text,
                                int centerX, int topY, boolean enabled) {
        int outline = 0xA8F8FCFF;
        g.drawCenteredString(font, text, centerX - 1, topY, outline);
        g.drawCenteredString(font, text, centerX + 1, topY, outline);
        g.drawCenteredString(font, text, centerX, topY - 1, outline);
        g.drawCenteredString(font, text, centerX, topY + 1, outline);
        g.drawCenteredString(font, text, centerX, topY,
            enabled ? 0xFF152C3C : 0xFF536879);
    }
}
