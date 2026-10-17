package dev.kaito.prismglass;

/** The main pane should stay clear even when the settings slider is at maximum. */
public final class ClearGlassAlpha {
    private ClearGlassAlpha() {}

    public static int forOpacity(int setting) {
        return 12 + Math.max(20, Math.min(90, setting)) / 2;
    }
}
