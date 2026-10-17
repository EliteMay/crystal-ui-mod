package dev.kaito.prismglass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.nio.file.Path;

/** Only exact vanilla inventory screens are replaced; other mods' GUI subclasses remain untouched. */
public final class ClientScreenEvents {
    private CrystalPreferences preferences;

    private CrystalPreferences prefs() {
        if (preferences == null) {
            Path path = Minecraft.getInstance().gameDirectory.toPath().resolve("config/prismglass.properties");
            preferences = CrystalPreferences.load(path);
        }
        return preferences;
    }

    @SubscribeEvent
    public void onOpen(ScreenEvent.Opening event) {
        Screen screen = event.getNewScreen();
        if (screen != null && screen.getClass() == InventoryScreen.class &&
            prefs().isEnabled() && Minecraft.getInstance().player != null) {
            event.setNewScreen(new FrostedInventoryScreen(Minecraft.getInstance().player, prefs()));
        }
    }

    /** One backdrop snapshot per rendered screen frame. */
    @SubscribeEvent
    public void onRenderPre(ScreenEvent.Render.Pre event) {
        GlassBackdrop.beginFrame();
    }

    @SubscribeEvent
    public void onInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        boolean isBag = screen instanceof FrostedInventoryScreen ||
                        screen.getClass() == InventoryScreen.class;
        boolean isOptions = screen.getClass() == OptionsScreen.class;
        if (!isBag && !isOptions) return;
        if (screen.width < 230 || screen.height < 150) return;

        CrystalPreferences state = prefs();
        int x = isBag ? Math.min(screen.width - 54, screen.width / 2 + 96) : screen.width - 54;
        int y = isBag ? Math.max(8, (screen.height - 166) / 2 - 8) : 8;

        event.addListener(new CrystalButton(x, y, 46, 18,
            Component.literal("PRISM"), state,
            button -> Minecraft.getInstance().setScreen(new CrystalSettingsScreen(screen, state))));
    }
}
