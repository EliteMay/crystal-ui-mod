package dev.kaito.prismglass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Readable, self-contained Japanese settings UI.
 * Does not require switching Minecraft's global language.
 */
public final class CrystalSettingsScreen extends Screen {
    private final Screen parent;
    private final CrystalPreferences preferences;
    private int px, py, pw, ph;

    public CrystalSettingsScreen(Screen parent, CrystalPreferences preferences) {
        super(Component.literal(PrismJapaneseText.title()));
        this.parent = parent;
        this.preferences = preferences;
    }

    @Override protected void init() {
        pw = Math.min(268, width - 24);
        ph = Math.min(202, height - 24);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        int bx = px + 18;
        int bw = pw - 36;
        boolean tiny = ph < 174;
        int buttonHeight = tiny ? 18 : 25;
        int rowStep = tiny ? 20 : 29;
        int sy = py + (tiny ? 25 : 61);

        addRenderableWidget(new CrystalButton(bx, sy, bw, buttonHeight, enabledLabel(), preferences, button -> {
            preferences.toggle();
            button.setMessage(enabledLabel());
        }));
        addRenderableWidget(new CrystalButton(bx, sy + rowStep, bw, buttonHeight, themeLabel(), preferences, button -> {
            preferences.cycleTheme();
            button.setMessage(themeLabel());
        }));
        int half = (bw - 8) / 2;
        addRenderableWidget(new CrystalButton(bx, sy + rowStep * 2, half, buttonHeight,
            Component.literal("- 10%"), preferences, button -> preferences.changeOpacity(-10)));
        addRenderableWidget(new CrystalButton(bx + half + 8, sy + rowStep * 2, half, buttonHeight,
            Component.literal("+ 10%"), preferences, button -> preferences.changeOpacity(10)));

        int footerY = py + ph - (tiny ? 21 : 29);
        addRenderableWidget(new CrystalButton(bx, footerY, half, buttonHeight,
            Component.literal(PrismJapaneseText.reset()), preferences, button -> {
                preferences.reset();
                Minecraft.getInstance().setScreen(new CrystalSettingsScreen(parent, preferences));
            }));
        addRenderableWidget(new CrystalButton(bx + half + 8, footerY, half, buttonHeight,
            Component.literal(PrismJapaneseText.done()), preferences, button -> onClose()));
    }

    private Component enabledLabel() {
        return Component.literal(PrismJapaneseText.enabled(preferences.isEnabled()));
    }

    private Component themeLabel() {
        return Component.literal(PrismJapaneseText.theme(preferences.theme()));
    }

    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float tick) {
        g.fillGradient(0, 0, width, height, 0x4A0E1B2B, 0x700A1625);
        GlassPaint.panel(g, px, py, pw, ph, preferences.opacity(), preferences.theme().color);

        // Local clarity layer. Keep the remainder of the panel truly glass-like.
        int upper = ph < 174 ? 21 : 54;
        g.fillGradient(px + 13, py + 9, px + pw - 13, py + upper,
            0xC4EFF7FB, 0xACE0EDF4);
        g.fill(px + 19, py + upper, px + pw - 19, py + upper + 1, 0x35FFF2D4);
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float tick) {
        super.render(g, mouseX, mouseY, tick);
        int ink = 0xFF132A3A;
        g.drawCenteredString(font, title, width / 2, py + (ph < 174 ? 15 : 17), ink);
        if (ph >= 174) {
            g.drawCenteredString(font, Component.literal(PrismJapaneseText.subtitle()),
                width / 2, py + 30, ink);
            g.drawCenteredString(font, Component.literal(PrismJapaneseText.opacity(preferences.opacity())),
                width / 2, py + 44, ink);
        }
    }

    @Override public void onClose() { Minecraft.getInstance().setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
