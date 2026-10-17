package dev.kaito.prismglass;

import java.nio.file.Files;
import java.nio.file.Path;

/** Run with plain javac/java; requires no Minecraft dependencies. */
public final class CrystalPreferencesTest {
    public static void main(String[] args) throws Exception {
        Path p = Files.createTempDirectory("prismglass-test-").resolve("config.properties");
        CrystalPreferences c = CrystalPreferences.load(p);
        require(c.isEnabled() && c.opacity() == 60 && c.theme() == CrystalPreferences.Theme.FROST, "defaults");
        c.changeOpacity(999);
        require(c.opacity() == 90, "clamp to 90");
        c.changeOpacity(-999);
        require(c.opacity() == 20, "clamp to 20");
        c.cycleTheme();
        c.toggle();
        CrystalPreferences loaded = CrystalPreferences.load(p);
        require(!loaded.isEnabled() && loaded.opacity() == 20 && loaded.theme() == CrystalPreferences.Theme.AMETHYST, "reload persistence");
        loaded.reset();
        require(loaded.isEnabled() && loaded.opacity() == 60 && loaded.theme() == CrystalPreferences.Theme.FROST, "reset");
        Files.writeString(p, "opacity=bad\ntheme=INVALID\n");
        loaded = CrystalPreferences.load(p);
        require(loaded.opacity() == 60 && loaded.theme() == CrystalPreferences.Theme.FROST, "invalid recovery");
        System.out.println("PASS: defaults / opacity clamp / persistence / reset / corrupt values");
    }
    private static void require(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }
}
