package dev.kaito.prismglass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.inventory.Slot;

/**
 * Decorative, non-interactive overlay for vanilla screens with private, complex renderers.
 *
 * Enchanting owns an animated 3D book and text choices, stonecutter owns private recipe-list
 * drawing and scrolling, and smithing owns template icons plus its armor stand preview.
 * No screen is replaced or menu state changed here.
 */
final class GlassComplexScreenDecor {
    private GlassComplexScreenDecor() {}

    static boolean supports(Class<?> exactScreenClass) {
        return exactScreenClass == EnchantmentScreen.class ||
               exactScreenClass == SmithingScreen.class ||
               exactScreenClass == StonecutterScreen.class ||
               exactScreenClass == GrindstoneScreen.class;
    }

    /** Draws the same optical pane behind optional transparent vanilla textures. */
    static void drawBackdrop(AbstractContainerScreen<?> screen, GuiGraphics g,
                             CrystalPreferences preferences) {
        if (!preferences.isEnabled() || !supports(screen.getClass())) return;
        int x = screen.getGuiLeft(), y = screen.getGuiTop();
        int w = screen.getXSize(), h = screen.getYSize();
        if (w < 160 || h < 110) return;
        GlassPaint.surface(g, x - 6, y - 8, w + 12, h + 15,
            preferences.opacity(), preferences.theme().color);
    }

    static void render(AbstractContainerScreen<?> screen, GuiGraphics g,
                       CrystalPreferences preferences) {
        if (!preferences.isEnabled() || !supports(screen.getClass())) return;
        int x = screen.getGuiLeft();
        int y = screen.getGuiTop();
        int w = screen.getXSize();
        int h = screen.getYSize();
        if (w < 160 || h < 110) return;

        int tint = preferences.theme().color;
        Font font = Minecraft.getInstance().font;

        // Cover the native title with a fully opaque, narrow legibility strip
        // before drawing it once at the SAME baseline. The old translucent strip
        // exposed the dark original glyphs underneath and caused doubled text.
        int labelWidth = Math.min(w - 12, Math.max(58, font.width(screen.getTitle()) + 14));
        g.fillGradient(x + 5, y + 3, x + 5 + labelWidth, y + 16,
            0xFF14293C, 0xFF1D3547);
        GlassPaint.rim(g, x + 5, y + 3, labelWidth, 13);
        g.drawString(font, screen.getTitle(), x + 8, y + 6, 0xFFF1F8FF, true);

        // Draw narrow slot outlines above optional clear textures. Original items,
        // click targets and private rendering logic remain untouched.
        int menuSlots = screen.getMenu().slots.size();
        if (menuSlots <= 200) {
            for (int i = 0; i < menuSlots; i++) {
                Slot slot = screen.getMenu().slots.get(i);
                GlassPaint.rim(g, x + slot.x, y + slot.y, 18, 18);
            }
        }

        if (screen.getClass() == EnchantmentScreen.class) {
            EnchantmentMenu menu = ((EnchantmentScreen) screen).getMenu();
            // Small status glints at the edge of the original option row. The vanilla
            // selection text, level requirement, animated book and button hitbox remain.
            for (int i = 0; i < 3; i++) {
                int optionY = y + 19 + i * 19;
                int c = i < menu.costs.length ? menu.costs[i] : 0;
                // The vanilla option art and animated selection text must stay visible.
                // A 1px status mark to the right does not cross the option glyphs.
                g.fill(x + 170, optionY + 5, x + 171, optionY + 11,
                    c > 0 ? GlassPaint.argb(150, tint) : 0x33485C6E);
            }
        } else if (screen.getClass() == SmithingScreen.class) {
            // Highlight exactly the 3 native ingredient slots. The armor stand,
            // template hints and error rendering are provided by vanilla SmithingScreen.
            int slots = Math.min(3, screen.getMenu().slots.size());
            for (int i = 0; i < slots; i++) {
                Slot slot = screen.getMenu().slots.get(i);
                GlassPaint.rim(g, x + slot.x - 1, y + slot.y - 1, 20, 20);
            }
            GlassPaint.rim(g, x + 96, y + 18, Math.max(24, w - 103), 63);
        } else if (screen.getClass() == StonecutterScreen.class) {
            StonecutterMenu menu = ((StonecutterScreen) screen).getMenu();
            // Border outside the recipe grid, never over the recipe items or scrollbar.
            GlassPaint.rim(g, x + 51, y + 15, 112, 82);
            int count = menu.getNumRecipes();
            if (count > 0) {
                int selected = menu.getSelectedRecipeIndex();
                String countText = selected >= 0
                    ? (selected + 1) + "/" + count : Integer.toString(count);
                int width = font.width(countText);
                g.drawString(font, countText, x + w - 9 - width, y + 6,
                    0xFFE0F5FF, true);
            }
        } else if (screen.getClass() == GrindstoneScreen.class) {
            // Grinder keeps its native repair / disenchant slots and error behavior.
            // This tiny rim is cosmetic; neither the screen nor its menu is replaced.
            GlassPaint.rim(g, x + 5, y + 16, 86, 57);
        }
    }
}
