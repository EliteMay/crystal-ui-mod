package dev.kaito.prismglass;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.BrewingStandMenu;

/**
 * Only draws the brewing stand UI. All brewing logic and menu listeners stay vanilla.
 * Layout coordinates match Minecraft 1.21.1 BrewingStandScreen.renderBg.
 */
final class GlassBrewingSkin {
    private GlassBrewingSkin() {}

    static void draw(GuiGraphics g, int left, int top, int width, int height,
                     BrewingStandMenu menu, CrystalPreferences settings) {
        GlassContainerSkin.draw(g, left, top, width, height, menu, settings);
        int tint = settings.theme().color;

        // Fuel remaining, from vanilla Menu.getFuel() (0..20).
        int barX = left + 60, barY = top + 44;
        g.fill(barX, barY, barX + 18, barY + 4, 0x4834485C);
        int fuel = Math.max(0, Math.min(18, (menu.getFuel() * 18 + 19) / 20));
        if (fuel > 0) {
            g.fillGradient(barX, barY, barX + fuel, barY + 4,
                GlassPaint.argb(194, tint), 0xDCFFF4D6);
        }
        GlassPaint.rim(g, barX, barY, 18, 5);

        // Fill increases while 400 ticks of brewing are consumed.
        int progressX = left + 97, progressY = top + 16;
        g.fill(progressX, progressY, progressX + 9, progressY + 28, 0x36304355);
        int ticks = Math.max(0, Math.min(400, menu.getBrewingTicks()));
        int progress = ticks > 0 ? Math.max(0, Math.min(28, (int)(28 * (1f - ticks / 400f)))) : 0;
        if (progress > 0) {
            g.fillGradient(progressX + 2, progressY, progressX + 7,
                progressY + progress, 0xD5FFFFFF, GlassPaint.argb(196, tint));
        }
        GlassPaint.rim(g, progressX, progressY, 9, 28);

        // Vanilla bubble track, with a small active shimmer while brewing.
        int bubbleX = left + 63, bubbleY = top + 14;
        GlassPaint.rim(g, bubbleX, bubbleY, 12, 29);
        if (ticks > 0) {
            int pulse = (ticks / 4) % 7;
            int centerY = bubbleY + 24 - pulse * 3;
            g.fill(bubbleX + 4, centerY, bubbleX + 7, centerY + 3, 0xB6DBF8FF);
        }
    }
}
