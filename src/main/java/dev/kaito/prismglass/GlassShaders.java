package dev.kaito.prismglass;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;

/** Owns the two core shaders. Any failure disables GPU glass; the legacy painter takes over. */
public final class GlassShaders {
    private static volatile ShaderInstance glass;
    private static volatile ShaderInstance blur;
    private static volatile boolean failed;

    private GlassShaders() {}

    public static void onRegister(RegisterShadersEvent event) {
        ShaderInstance g = null;
        try {
            g = new ShaderInstance(event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(PrismGlass.MOD_ID, "glass"),
                DefaultVertexFormat.POSITION_TEX_COLOR);
            ShaderInstance b = new ShaderInstance(event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(PrismGlass.MOD_ID, "blur"),
                DefaultVertexFormat.POSITION);
            event.registerShader(g, s -> glass = s);
            event.registerShader(b, s -> blur = s);
            failed = false;
        } catch (Exception e) {
            if (g != null) g.close();
            fail(e);
        }
    }

    public static void fail(Throwable t) {
        if (!failed) {
            System.err.println("[PrismGlass] GPU glass disabled, using flat fallback: " + t);
        }
        failed = true;
    }

    public static boolean ready() { return !failed && glass != null && blur != null; }
    public static ShaderInstance glass() { return glass; }
    public static ShaderInstance blur() { return blur; }
}
