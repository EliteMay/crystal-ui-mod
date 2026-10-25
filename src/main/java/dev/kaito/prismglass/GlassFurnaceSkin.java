package dev.kaito.prismglass;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.AbstractFurnaceMenu;

/**
 * Shared background for furnace, blast furnace, and smoker.
 * The native menu drives flame and cooking progress; no animation state is copied.
 */
final class GlassFurnaceSkin {
    private GlassFurnaceSkin() {}

    static void draw(GuiGraphics g, int left, int top, int width, int height,
                     AbstractFurnaceMenu menu, CrystalPreferences prefs) {
        int tint = prefs.theme().color;
        GlassContainerSkin.draw(g, left, top, width, height, menu, prefs);

        // The original 1.21.1 furnace uses (56,36) for flame and (79,34) for
        // the cooking arrow. These are decoration only, never hit targets.
        int flameX = left + 56;
        int flameY = top + 36;
        g.fill(flameX, flameY, flameX + 14, flameY + 14, 0x2C122536);
        GlassPaint.rim(g, flameX, flameY, 14, 14);
        if (menu.isLit()) {
            int lit = Math.max(0, Math.min(14, (int) Math.ceil(menu.getLitProgress() * 13f) + 1));
            if (lit > 0) {
                g.fillGradient(flameX + 3, flameY + 14 - lit, flameX + 11, flameY + 14,
                    0xB8FFF0B1, 0xE0FFB95C);
                g.fill(flameX + 6, flameY + 14 - Math.max(1, lit - 2),
                    flameX + 8, flameY + 14, 0xDCFDF8E7);
            }
        }

        int arrowX = left + 79;
        int arrowY = top + 34;
        // Neutral track remains readable even when no recipe is cooking.
        g.fillGradient(arrowX, arrowY + 5, arrowX + 18, arrowY + 11,
            0x4C6A8296, 0x60465E70);
        g.fill(arrowX + 18, arrowY + 2, arrowX + 21, arrowY + 14, 0x5B8EA9B7);
        g.fill(arrowX + 21, arrowY + 5, arrowX + 24, arrowY + 11, 0x5B8EA9B7);
        int filled = Math.max(0, Math.min(24, (int) Math.ceil(menu.getBurnProgress() * 24f)));
        if (filled > 0) {
            int shaft = Math.min(18, filled);
            g.fillGradient(arrowX, arrowY + 6, arrowX + shaft, arrowY + 10,
                0xC8FFF4C7, GlassPaint.argb(170, tint));
            if (filled > 18) {
                g.fill(arrowX + 18, arrowY + 6, arrowX + Math.min(24, filled),
                    arrowY + 10, 0xC8FFFFFF);
            }
        }
    }
}
