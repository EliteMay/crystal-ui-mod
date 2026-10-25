package dev.kaito.prismglass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.SmokerScreen;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.nio.file.Path;

/** Replaces safe vanilla containers; decorates other vanilla screens without changing input. */
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
        if (screen == null || !prefs().isEnabled() || Minecraft.getInstance().player == null) return;
        var player = Minecraft.getInstance().player;
        var inventory = player.getInventory();
        // Exact vanilla screens only: other mods' subclasses keep their own controls.
        if (screen.getClass() == InventoryScreen.class) {
            event.setNewScreen(new FrostedInventoryScreen(player, prefs()));
        } else if (screen.getClass() == ContainerScreen.class) {
            event.setNewScreen(new FrostedChestScreen(
                (ChestMenu) ((ContainerScreen) screen).getMenu(), inventory,
                screen.getTitle(), prefs()));
        } else if (screen.getClass() == CraftingScreen.class) {
            event.setNewScreen(new FrostedCraftingScreen(
                (CraftingMenu) ((CraftingScreen) screen).getMenu(), inventory,
                screen.getTitle(), prefs()));
        } else if (screen.getClass() == HopperScreen.class) {
            event.setNewScreen(new FrostedHopperScreen(
                (HopperMenu) ((HopperScreen) screen).getMenu(), inventory,
                screen.getTitle(), prefs()));
        } else if (screen.getClass() == DispenserScreen.class) {
            event.setNewScreen(new FrostedDispenserScreen(
                (DispenserMenu) ((DispenserScreen) screen).getMenu(), inventory,
                screen.getTitle(), prefs()));
        } else if (screen.getClass() == ShulkerBoxScreen.class) {
            event.setNewScreen(new FrostedShulkerBoxScreen(
                (ShulkerBoxMenu) ((ShulkerBoxScreen) screen).getMenu(), inventory,
                screen.getTitle(), prefs()));
        } else if (screen.getClass() == FurnaceScreen.class) {
            event.setNewScreen(new FrostedFurnaceScreen(
                (FurnaceMenu) ((FurnaceScreen) screen).getMenu(), inventory,
                screen.getTitle(), prefs()));
        } else if (screen.getClass() == BlastFurnaceScreen.class) {
            event.setNewScreen(new FrostedBlastFurnaceScreen(
                (BlastFurnaceMenu) ((BlastFurnaceScreen) screen).getMenu(), inventory,
                screen.getTitle(), prefs()));
        } else if (screen.getClass() == SmokerScreen.class) {
            event.setNewScreen(new FrostedSmokerScreen(
                (SmokerMenu) ((SmokerScreen) screen).getMenu(), inventory,
                screen.getTitle(), prefs()));
        } else if (screen.getClass() == BrewingStandScreen.class) {
            event.setNewScreen(new FrostedBrewingStandScreen(
                (BrewingStandMenu) ((BrewingStandScreen) screen).getMenu(), inventory,
                screen.getTitle(), prefs()));
        } else if (screen.getClass() == AnvilScreen.class) {
            event.setNewScreen(new FrostedAnvilScreen(
                (AnvilMenu) ((AnvilScreen) screen).getMenu(), inventory,
                screen.getTitle(), prefs()));
        } else if (screen.getClass() == MerchantScreen.class) {
            event.setNewScreen(new FrostedMerchantScreen(
                (MerchantMenu) ((MerchantScreen) screen).getMenu(), inventory,
                screen.getTitle(), prefs()));
        }
    }

    /** One backdrop snapshot per rendered screen frame. */
    @SubscribeEvent
    public void onRenderPre(ScreenEvent.Render.Pre event) {
        GlassBackdrop.beginFrame();
    }

    /** Global chrome covers vanilla menus and specialized containers without replacing logic. */
    @SubscribeEvent
    public void onRenderPost(ScreenEvent.Render.Post event) {
        CrystalGlobalDecor.render(event.getScreen(), event.getGuiGraphics(), prefs());
    }

    @SubscribeEvent
    public void onInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        boolean isBag = screen instanceof AbstractContainerScreen<?> &&
            (screen.getClass().getName().startsWith("net.minecraft.client.gui.screens.inventory.") ||
             screen.getClass().getName().startsWith("dev.kaito.prismglass.Frosted"));
        boolean isOptions = screen.getClass() == OptionsScreen.class;
        boolean isMenu = screen.getClass() == TitleScreen.class ||
                         screen.getClass() == PauseScreen.class;
        if (!isBag && !isOptions && !isMenu) return;
        if (screen.width < 230 || screen.height < 150) return;

        CrystalPreferences state = prefs();
        int x = isBag ? Math.min(screen.width - 54, screen.width / 2 + 96) : screen.width - 54;
        int y = isBag ? Math.max(8, ((AbstractContainerScreen<?>) screen).getGuiTop() - 21) : 8;

        event.addListener(new CrystalButton(x, y, 46, 18,
            Component.literal(PrismJapaneseText.openSettings()), state,
            button -> Minecraft.getInstance().setScreen(new CrystalSettingsScreen(screen, state))));
    }
}
