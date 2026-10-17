package dev.kaito.prismglass;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;

/**
 * Per-frame snapshot of the already drawn scene: one sharp copy and one dual-Kawase blurred
 * copy. The glass shader samples these, never the main target itself (no feedback loop).
 * Captured lazily the first time glass is drawn in a frame, i.e. after the screen background
 * and before any glass, so the glass shows exactly what is behind it.
 */
public final class GlassBackdrop {
    private static final int LINEAR = 9729;
    private static TextureTarget sharp, half, quarter, eighth;
    private static int width = -1, height = -1;
    private static long frame;
    private static long capturedFrame = -1;
    private static boolean valid;

    private GlassBackdrop() {}

    /** Called once per screen render, before the screen draws anything. */
    public static void beginFrame() {
        frame++;
        valid = false;
    }

    public static int sharpTexture() { return sharp.getColorTextureId(); }
    public static int blurredTexture() { return half.getColorTextureId(); }

    public static boolean ensure(GuiGraphics g) {
        if (!GlassShaders.ready()) return false;
        if (capturedFrame == frame) return valid;
        capturedFrame = frame;
        valid = false;
        try {
            g.flush();
            capture();
            valid = true;
        } catch (RuntimeException | LinkageError e) {
            GlassShaders.fail(e);
        }
        return valid;
    }

    private static TextureTarget target(int w, int h) {
        TextureTarget t = new TextureTarget(Math.max(1, w), Math.max(1, h), false, Minecraft.ON_OSX);
        t.setFilterMode(LINEAR);
        t.setClearColor(0f, 0f, 0f, 1f);
        return t;
    }

    private static void ensureTargets(int w, int h) {
        if (sharp != null && w == width && h == height) return;
        release();
        width = w;
        height = h;
        sharp = target(w, h);
        half = target(w / 2, h / 2);
        quarter = target(w / 4, h / 4);
        eighth = target(w / 8, h / 8);
    }

    public static void release() {
        for (TextureTarget t : new TextureTarget[] {sharp, half, quarter, eighth}) {
            if (t != null) t.destroyBuffers();
        }
        sharp = half = quarter = eighth = null;
        width = height = -1;
    }

    private static void capture() {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        ensureTargets(main.width, main.height);

        pass(sharp, main.getColorTextureId(), 0f, 0.5f / main.width, 0.5f / main.height);
        pass(half, sharp.getColorTextureId(), 1f, 0.5f / half.width, 0.5f / half.height);
        pass(quarter, half.getColorTextureId(), 1f, 0.5f / quarter.width, 0.5f / quarter.height);
        pass(eighth, quarter.getColorTextureId(), 1f, 0.5f / eighth.width, 0.5f / eighth.height);
        pass(quarter, eighth.getColorTextureId(), 2f, 0.5f / eighth.width, 0.5f / eighth.height);
        pass(half, quarter.getColorTextureId(), 2f, 0.5f / quarter.width, 0.5f / quarter.height);

        main.bindWrite(true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static void pass(RenderTarget dst, int srcTexture, float mode, float hpx, float hpy) {
        ShaderInstance shader = GlassShaders.blur();
        dst.bindWrite(true);
        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, srcTexture);
        shader.safeGetUniform("Mode").set(mode);
        shader.safeGetUniform("HalfPixel").set(hpx, hpy);
        shader.safeGetUniform("Offset").set(1.0f);
        BufferBuilder bb = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bb.addVertex(-1f, -1f, 0f);
        bb.addVertex(1f, -1f, 0f);
        bb.addVertex(1f, 1f, 0f);
        bb.addVertex(-1f, 1f, 0f);
        BufferUploader.drawWithShader(bb.buildOrThrow());
    }
}
