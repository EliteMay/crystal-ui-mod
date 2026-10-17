package dev.kaito.prismglass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;

/** Crystal button with a small, readable local frosted label inset. */
public final class CrystalButton extends AbstractButton {
    public interface Press { void onPress(CrystalButton button); }
    private final Press press;
    private final CrystalPreferences preferences;

    public CrystalButton(int x, int y, int w, int h, Component text,
                         CrystalPreferences preferences, Press press) {
        super(x, y, w, h, text);
        this.press = press;
        this.preferences = preferences;
    }

    @Override public void onPress() { press.onPress(this); }

    @Override protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @Override protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
        int x = getX(), y = getY(), w = width, h = height;
        int alpha = isHoveredOrFocused() ? Math.min(90, preferences.opacity() + 10) : preferences.opacity();
        int tint = preferences.theme().color;

        boolean gpu = GlassRenderer.button(g, x, y, w, h, isHoveredOrFocused(), preferences.opacity(), tint);
        if (!gpu) GlassPaint.panel(g, x, y, w, h, alpha, tint);
        if (!gpu && w >= 68 && h >= 22) GlassPaint.jewel(g, x + 10, y + h / 2, tint);
        if (!gpu && isHoveredOrFocused()) {
            g.fill(x + 8, y + h - 3, x + w - 8, y + h - 2, 0x72FFF2DC);
        }

        // Text-only local backing keeps the rest of the expensive GPU glass visible.
        int textW = Minecraft.getInstance().font.width(getMessage());
        int plateW = Math.min(w - 10, textW + 16);
        int plateX = x + (w - plateW) / 2;
        int plateY = y + (h - 14) / 2;
        if (plateW > 8 && h >= 17) {
            g.fillGradient(plateX, plateY, plateX + plateW, plateY + 14,
                0xBCEEF6F9, 0xB4E2EEF4);
        }

        int fg = active ? 0xFF102838 : 0xFF5B6E7B;
        g.drawCenteredString(Minecraft.getInstance().font, getMessage(),
            x + w / 2, y + (h - 8) / 2, fg);
    }
}
