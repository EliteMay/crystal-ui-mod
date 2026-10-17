package dev.kaito.prismglass;

public final class ClearGlassAlphaTest {
    public static void main(String[] args) {
        if (ClearGlassAlpha.forOpacity(20) != 22) throw new AssertionError("min");
        if (ClearGlassAlpha.forOpacity(60) != 42) throw new AssertionError("default");
        if (ClearGlassAlpha.forOpacity(90) != 57) throw new AssertionError("max");
        if (ClearGlassAlpha.forOpacity(-100) != 22) throw new AssertionError("negative clamp");
        if (ClearGlassAlpha.forOpacity(900) != 57) throw new AssertionError("max clamp");
        for (int i = 20; i <= 90; i++) {
            int a = ClearGlassAlpha.forOpacity(i);
            if (a > 64) throw new AssertionError("main pane over 25% opacity");
            if (a < 20) throw new AssertionError("main pane too transparent");
        }
        System.out.println("PASS: v0.4 clear glass alpha bounds 22..57/255");
    }
}
