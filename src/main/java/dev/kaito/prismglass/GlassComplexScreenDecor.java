package dev.kaito.prismglass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
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
               exactScreenClass == StonecutterScreen.class;
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

        // Readable glass title backplate. Drawn after vanilla text only in a non-clickable
        // header region, outside the book, recipe buttons, armor preview, and inventory.
        int titleWidth = Math.min(w - 18, Math.max(60, font.width(screen.getTitle()) + 16));
        g.fillGradient(x + 5, y + 3, x + 5 + titleWidth, y + 16,
            0x9B102437, 0x67243B52);
        GlassPaint.rim(g, x + 5, y + 3, titleWidth, 13);
        g.drawString(font, screen.getTitle(), x + 12, y + 6, 0xFFF0F8FF, true);

        if (screen.getClass() == EnchantmentScreen.class) {
            EnchantmentMenu menu = ((EnchantmentScreen) screen).getMenu();
            // Small status glints at the edge of the original option row. The vanilla
            // selection text, level requirement, animated book and button hitbox remain.
            for (int i = 0; i < 3; i++) {
                int optionY = y + 19 + i * 19;
                int c = i < menu.costs.length ? menu.costs[i] : 0;
                GlassPaint.rim(g, x + 103, optionY - 1, 65, 18);
                g.fill(x + 165, optionY + 5, x + 167, optionY + 11,
                    c > 0 ? GlassPaint.argb(180, tint) : 0x33485C6E);
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
        }
    }
}
