package dev.kaito.prismglass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.nio.file.Path;

/** Adds a settings entry and subtle outer frame without interfering with slot hitboxes. */
public final class ClientScreenEvents {
    private CrystalPreferences preferences;

    private CrystalPreferences prefs() {
        if (preferences == null) {
            Path path = Minecraft.getInstance().gameDirectory.toPath().resolve("config/prismglass.properties");
            preferences = CrystalPreferences.load(path);
        }
        return preferences;
    }

    private static boolean supported(Screen screen) {
        return screen.getClass() == InventoryScreen.class || screen.getClass() == OptionsScreen.class;
    }

    @SubscribeEvent public void onInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!supported(screen) || screen.width < 220 || screen.height < 140) return;
        CrystalPreferences state = prefs();
        int x = screen.width - 84;
        event.addListener(new CrystalButton(x, 7, 77, 22, Component.literal("PRISM"), state,
            button -> Minecraft.getInstance().setScreen(new CrystalSettingsScreen(screen, state))));
    }

    @SubscribeEvent public void onRender(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        if (!supported(screen) || !prefs().isEnabled()) return;
        int w = screen.width;
        int h = screen.height;
        if (w < 220 || h < 140) return;
        GuiGraphics g = event.getGuiGraphics();
        int tint = prefs().theme().color;
        GlassPaint.frame(g, 5, 5, w - 10, h - 10, tint);
        int tagW = Math.min(117, w / 3);
        GlassPaint.panel(g, 9, 9, tagW, 25, prefs().opacity(), tint);
        g.drawString(Minecraft.getInstance().font, "PRISM / " + (screen instanceof InventoryScreen ? "BAG" : "SETTINGS"),
                     18, 17, 0xFFF0F8FF);
        GlassPaint.panel(g, 9, h - 24, 82, 16, prefs().opacity(), tint);
        g.drawString(Minecraft.getInstance().font, "1.21.1 FORGE", 16, h - 20, 0xFFD4E7F0);
    }
}
