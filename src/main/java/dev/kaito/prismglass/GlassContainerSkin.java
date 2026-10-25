package dev.kaito.prismglass;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/** Visual-only container skin: coordinates and click handling remain vanilla. */
final class GlassContainerSkin {
    private GlassContainerSkin() {}

    static void draw(GuiGraphics graphics, int x, int y, int width, int height,
                     AbstractContainerMenu menu, CrystalPreferences preferences) {
        final int tint = preferences.theme().color;
        GlassPaint.surface(graphics, x - 6, y - 8, width + 12, height + 15,
            preferences.opacity(), tint);

        // Slot locations are read from the original menu. Nothing is moved or rebuilt.
        int count = menu.slots.size();
        if (count > 0 && count <= 200) {
            int[] xy = new int[count * 2];
            for (int i = 0; i < count; i++) {
                Slot slot = menu.slots.get(i);
                xy[2 * i] = x + slot.x;
                xy[2 * i + 1] = y + slot.y;
            }
            GlassPaint.slots(graphics, xy, count, tint);
            // Most vanilla menus end in the 36 standard player inventory slots.
            if (count >= 36) {
                int firstPlayerY = Integer.MAX_VALUE;
                for (int i = count - 36; i < count; i++) {
                    firstPlayerY = Math.min(firstPlayerY, menu.slots.get(i).y);
                }
                if (firstPlayerY >= 24 && firstPlayerY < height - 12) {
                    GlassPaint.line(graphics, x + 7, y + firstPlayerY - 4,
                        width - 14, tint);
                }
            }
        }
    }

    static void label(GuiGraphics graphics, Font font, Component text, int x, int y) {
        int length = font.width(text);
        if (length == 0) return;
        graphics.fillGradient(x - 3, y - 2, x + length + 4, y + 11,
            0x4515283C, 0x210B233A);
        graphics.drawString(font, text, x, y, 0xFFF0F8FF, true);
    }

    static void screenBackground(GuiGraphics graphics, int width, int height) {
        graphics.fillGradient(0, 0, width, height, 0x2C06121F, 0x48081220);
    }
}
