package dev.kaito.prismglass;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

/** Draws SDF-shaped glass quads with the glass shader. All methods return false if unavailable. */
public final class GlassRenderer {
    private static final int PAD = 14;

    private GlassRenderer() {}

    public static boolean pane(GuiGraphics g, int x, int y, int w, int h, int opacity, int tint) {
        if (!GlassBackdrop.ensure(g)) return false;
        float veil = 0.05f + 0.26f * (opacity / 100f);
        return draw(g, x, y, w, h, 5f, 0f, PAD, tint, veil, 0f, 0.55f, 10f);
    }

    public static boolean button(GuiGraphics g, int x, int y, int w, int h, boolean hover,
                                 int opacity, int tint) {
        if (!GlassBackdrop.ensure(g)) return false;
        float veil = 0.28f + 0.3f * (opacity / 100f);
        return draw(g, x, y, w, h, 3f, 2f, 6, tint, veil, hover ? 1f : 0f, 0.8f, 3.5f);
    }

    /** Batched etched wells; every slot is an 18x18 square at the given top-left corners. */
    public static boolean slots(GuiGraphics g, int[] xy, int count, int tint) {
        if (!GlassShaders.ready() || count <= 0) return false;
        try {
            g.flush();
            ShaderInstance s = GlassShaders.glass();
            begin(s, 9f, 9f, 2f, 1f, 0f, 0f, 0f, 0f);
            Matrix4f m = g.pose().last().pose();
            BufferBuilder bb = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX_COLOR);
            int argb = 0xFF000000 | (tint & 0xFFFFFF);
            for (int i = 0; i < count; i++) {
                quad(bb, m, xy[i * 2], xy[i * 2 + 1], 18, 18, 1, argb);
            }
            BufferUploader.drawWithShader(bb.buildOrThrow());
            finish();
            return true;
        } catch (RuntimeException | LinkageError e) {
            GlassShaders.fail(e);
            return false;
        }
    }

    private static boolean draw(GuiGraphics g, int x, int y, int w, int h, float radius, float mode,
                                int pad, int tint, float veil, float hover, float blurMix, float refr) {
        try {
            g.flush();
            ShaderInstance s = GlassShaders.glass();
            begin(s, w / 2f, h / 2f, radius, mode, veil, hover, blurMix, refr);
            RenderSystem.setShaderTexture(0, GlassBackdrop.blurredTexture());
            RenderSystem.setShaderTexture(1, GlassBackdrop.sharpTexture());
            BufferBuilder bb = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX_COLOR);
            quad(bb, g.pose().last().pose(), x, y, w, h, pad, 0xFF000000 | (tint & 0xFFFFFF));
            BufferUploader.drawWithShader(bb.buildOrThrow());
            finish();
            return true;
        } catch (RuntimeException | LinkageError e) {
            GlassShaders.fail(e);
            return false;
        }
    }

    private static void begin(ShaderInstance s, float hx, float hy, float radius, float mode,
                              float veil, float hover, float blurMix, float refr) {
        Minecraft mc = Minecraft.getInstance();
        Window win = mc.getWindow();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> s);
        s.safeGetUniform("FbSize").set((float) mc.getMainRenderTarget().width,
                                       (float) mc.getMainRenderTarget().height);
        s.safeGetUniform("HalfSize").set(hx, hy);
        s.safeGetUniform("Radius").set(radius);
        s.safeGetUniform("Mode").set(mode);
        s.safeGetUniform("GuiScale").set((float) win.getGuiScale());
        s.safeGetUniform("Time").set((Util.getMillis() % 600000L) / 1000f);
        s.safeGetUniform("Params").set(veil, hover, blurMix, refr);
    }

    private static void finish() {
        RenderSystem.enableDepthTest();
    }

    /** Quad padded by {@code pad}; UV carries the position relative to the rect centre. */
    private static void quad(BufferBuilder bb, Matrix4f m, int x, int y, int w, int h, int pad, int argb) {
        float cx = x + w / 2f, cy = y + h / 2f;
        float x0 = x - pad, y0 = y - pad, x1 = x + w + pad, y1 = y + h + pad;
        bb.addVertex(m, x0, y0, 0f).setUv(x0 - cx, y0 - cy).setColor(argb);
        bb.addVertex(m, x0, y1, 0f).setUv(x0 - cx, y1 - cy).setColor(argb);
        bb.addVertex(m, x1, y1, 0f).setUv(x1 - cx, y1 - cy).setColor(argb);
        bb.addVertex(m, x1, y0, 0f).setUv(x1 - cx, y0 - cy).setColor(argb);
    }
}
