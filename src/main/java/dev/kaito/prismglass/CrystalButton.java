package dev.kaito.prismglass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;

/** Genuine clickable vanilla widget, styled to resemble a polished crystal charm. */
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
        GlassPaint.surface(g, x, y, w, h, alpha, tint);
        if (w >= 45 && h >= 19) {
            GlassPaint.jewel(g, x + 10, y + h / 2, tint);
        }
        if (isHoveredOrFocused()) {
            g.fill(x + 18, y + h - 3, x + w - 7, y + h - 2, 0x85FFE9BA);
        }
        int fg = active ? 0xFF20384C : 0xFF677681;
        g.drawCenteredString(Minecraft.getInstance().font, getMessage(),
            x + w / 2, y + (h - 8) / 2, fg);
    }
}
