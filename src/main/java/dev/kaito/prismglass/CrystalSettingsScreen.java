package dev.kaito.prismglass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Separate opt-in settings screen. Vanilla option widgets remain intact. */
public final class CrystalSettingsScreen extends Screen {
    private final Screen parent;
    private final CrystalPreferences preferences;
    private int px, py, pw, ph;

    public CrystalSettingsScreen(Screen parent, CrystalPreferences preferences) {
        super(Component.translatable("prismglass.title"));
        this.parent = parent;
        this.preferences = preferences;
    }

    @Override protected void init() {
        pw = Math.min(396, width - 24);
        ph = Math.min(254, height - 24);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        int bx = px + 18;
        int bw = pw - 36;
        boolean tiny = ph < 174;
        boolean compact = ph < 230;
        int buttonHeight = tiny ? 18 : compact ? 22 : 27;
        int rowStep = tiny ? 20 : compact ? 27 : 35;
        int sy = py + (tiny ? 25 : compact ? 59 : 66);
        addRenderableWidget(new CrystalButton(bx, sy, bw, buttonHeight, enabledLabel(), preferences, button -> {
            preferences.toggle(); button.setMessage(enabledLabel());
        }));
        addRenderableWidget(new CrystalButton(bx, sy + rowStep, bw, buttonHeight, themeLabel(), preferences, button -> {
            preferences.cycleTheme(); button.setMessage(themeLabel());
        }));
        int half = (bw - 8) / 2;
        addRenderableWidget(new CrystalButton(bx, sy + rowStep * 2, half, buttonHeight, Component.literal("-  10%"), preferences, button -> {
            preferences.changeOpacity(-10);
        }));
        addRenderableWidget(new CrystalButton(bx + half + 8, sy + rowStep * 2, half, buttonHeight, Component.literal("+  10%"), preferences, button -> {
            preferences.changeOpacity(10);
        }));
        int footerY = py + ph - (tiny ? 21 : compact ? 29 : 42);
        addRenderableWidget(new CrystalButton(bx, footerY, half, buttonHeight,
            Component.translatable("prismglass.reset"), preferences, button -> {
                preferences.reset(); Minecraft.getInstance().setScreen(new CrystalSettingsScreen(parent, preferences));
            }));
        addRenderableWidget(new CrystalButton(bx + half + 8, footerY, half, buttonHeight,
            Component.translatable("gui.done"), preferences, button -> onClose()));
    }

    private Component enabledLabel() {
        return Component.translatable("prismglass.enabled", Component.translatable(preferences.isEnabled() ? "options.on" : "options.off"));
    }
    private Component themeLabel() {
        return Component.translatable("prismglass.theme", preferences.theme().name());
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float tick) {
        g.fillGradient(0, 0, width, height, 0x7C213348, 0xA00A1625);
        GlassPaint.panel(g, px, py, pw, ph, preferences.opacity(), preferences.theme().color);
        g.drawCenteredString(font, title, width / 2, py + (ph < 174 ? 10 : 18), 0xFF233B50);
        if (ph >= 174) {
            g.drawCenteredString(font, Component.translatable("prismglass.subtitle"), width / 2, py + 34, 0xFF536D7D);
            g.drawCenteredString(font, Component.translatable("prismglass.opacity", preferences.opacity()),
                                 width / 2, py + 54, 0xFF314C5B);
        }
        if (ph >= 230) {
            g.drawCenteredString(font, Component.translatable("prismglass.future"),
                                 width / 2, py + ph - 55, 0xFF627684);
        }
        super.render(g, mouseX, mouseY, tick);
    }

    @Override public void onClose() { Minecraft.getInstance().setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
