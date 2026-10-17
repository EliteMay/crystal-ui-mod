package dev.kaito.prismglass;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.Dist;

/** Client-only visual customization. Does not touch menus, slots, packets, or inventory state. */
@Mod(PrismGlass.MOD_ID)
public final class PrismGlass {
    public static final String MOD_ID = "prismglass";

    public PrismGlass() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(new ClientScreenEvents());
            FMLJavaModLoadingContext.get().getModEventBus().addListener(GlassShaders::onRegister);
        }
    }
}
