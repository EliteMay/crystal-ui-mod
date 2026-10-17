package dev.kaito.prismglass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;

/** A real clickable Minecraft widget, not a decorative overlay over a vanilla button. */
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

    @Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int tint = preferences.theme().color;
        GlassPaint.panel(graphics, getX(), getY(), width, height,
                         isHoveredOrFocused() ? Math.min(90, preferences.opacity() + 15) : preferences.opacity(), tint);
        int fg = active ? 0xFFF5FCFF : 0xFF7A8490;
        graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                                    getX() + width / 2, getY() + (height - 8) / 2, fg);
    }
}
