package dev.kaito.prismglass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Properties;

/** Versioned appearance preferences without any dependency on Minecraft classes. */
public final class CrystalPreferences {
    public enum Theme {
        SERAPH(0xEAF7FF), HALO(0xFFF4D6), PRISM(0xDFFBFF);
        public final int color;
        Theme(int color) { this.color = color; }
        public Theme next() { return values()[(ordinal() + 1) % values().length]; }

        /** Previous theme IDs remain loadable so existing player settings survive. */
        public static Theme fromSaved(String raw) {
            if (raw == null) return SERAPH;
            return switch (raw.toUpperCase(Locale.ROOT)) {
                case "FROST", "SERAPH" -> SERAPH;
                case "PEARL", "HALO" -> HALO;
                case "AMETHYST", "PRISM" -> PRISM;
                default -> SERAPH;
            };
        }
    }

    private final Path file;
    private boolean enabled = true;
    private int opacity = 60;
    private Theme theme = Theme.SERAPH;

    private CrystalPreferences(Path file) { this.file = file; }

    public static CrystalPreferences load(Path file) {
        CrystalPreferences prefs = new CrystalPreferences(file);
        if (Files.isRegularFile(file)) {
            Properties properties = new Properties();
            try (InputStream in = Files.newInputStream(file)) {
                properties.load(in);
                prefs.enabled = Boolean.parseBoolean(properties.getProperty("enabled", "true"));
                prefs.opacity = clamp(parseInt(properties.getProperty("opacity", "60"), 60));
                prefs.theme = Theme.fromSaved(properties.getProperty("theme", "SERAPH"));
            } catch (IOException e) {
                System.err.println("[PrismGlass] Cannot load settings: " + e.getMessage());
            }
        }
        return prefs;
    }

    public boolean isEnabled() { return enabled; }
    public int opacity() { return opacity; }
    public Theme theme() { return theme; }

    public void toggle() { enabled = !enabled; save(); }
    public void changeOpacity(int delta) { opacity = clamp(opacity + delta); save(); }
    public void cycleTheme() { theme = theme.next(); save(); }
    public void reset() { enabled = true; opacity = 60; theme = Theme.SERAPH; save(); }

    static int clamp(int value) { return Math.max(20, Math.min(90, value)); }
    static int parseInt(String raw, int fallback) {
        try { return Integer.parseInt(raw); }
        catch (NumberFormatException e) { return fallback; }
    }

    private void save() {
        Properties properties = new Properties();
        properties.setProperty("enabled", String.valueOf(enabled));
        properties.setProperty("opacity", String.valueOf(opacity));
        properties.setProperty("theme", theme.name());
        try {
            Files.createDirectories(file.getParent());
            Path staging = Files.createTempFile(file.getParent(), "prismglass-", ".tmp");
            try {
                try (OutputStream out = Files.newOutputStream(staging)) {
                    properties.store(out, "Prism Glass - Minecraft 1.21.1 Forge");
                }
                try {
                    Files.move(staging, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(staging, file, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(staging);
            }
        } catch (IOException e) {
            System.err.println("[PrismGlass] Cannot save settings: " + e.getMessage());
        }
    }
}
