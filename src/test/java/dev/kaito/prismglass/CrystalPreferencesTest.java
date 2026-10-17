package dev.kaito.prismglass;

import java.nio.file.Files;
import java.nio.file.Path;

public final class CrystalPreferencesTest {
    public static void main(String[] args) throws Exception {
        Path p = Files.createTempDirectory("prismglass-test-").resolve("config.properties");
        CrystalPreferences c = CrystalPreferences.load(p);
        require(c.isEnabled() && c.opacity() == 60 && c.theme() == CrystalPreferences.Theme.SERAPH, "defaults");
        c.changeOpacity(999);
        require(c.opacity() == 90, "clamp max");
        c.changeOpacity(-999);
        require(c.opacity() == 20, "clamp min");
        c.cycleTheme();
        c.toggle();
        CrystalPreferences loaded = CrystalPreferences.load(p);
        require(!loaded.isEnabled() && loaded.opacity() == 20 && loaded.theme() == CrystalPreferences.Theme.HALO, "save round trip");
        loaded.cycleTheme();
        require(loaded.theme() == CrystalPreferences.Theme.PRISM, "cycle third");
        loaded.reset();
        require(loaded.isEnabled() && loaded.opacity() == 60 && loaded.theme() == CrystalPreferences.Theme.SERAPH, "reset");
        for (String legacy : new String[] { "FROST", "AMETHYST", "PEARL" }) {
            Files.writeString(p, "theme=" + legacy + "\nenabled=false\nopacity=55\n");
            loaded = CrystalPreferences.load(p);
            CrystalPreferences.Theme mapped = switch (legacy) {
                case "FROST" -> CrystalPreferences.Theme.SERAPH;
                case "AMETHYST" -> CrystalPreferences.Theme.PRISM;
                default -> CrystalPreferences.Theme.HALO;
            };
            require(loaded.theme() == mapped && !loaded.isEnabled() && loaded.opacity() == 55,
                    "legacy theme mapping " + legacy);
        }
        Files.writeString(p, "opacity=bad\ntheme=INVALID\n");
        loaded = CrystalPreferences.load(p);
        require(loaded.opacity() == 60 && loaded.theme() == CrystalPreferences.Theme.SERAPH, "invalid recovery");
        System.out.println("PASS: defaults / bounds / persistence / reset / legacy migration / invalid values");
    }
    private static void require(boolean cond, String message) {
        if (!cond) throw new AssertionError(message);
    }
}
