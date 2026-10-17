package dev.kaito.prismglass;

public final class PrismJapaneseTextTest {
    public static void main(String[] args) {
        require(PrismJapaneseText.title().equals("PRISM GLASS"), "brand");
        require(PrismJapaneseText.subtitle().contains("クリスタル"), "subtitle");
        require(PrismJapaneseText.opacity(90).equals("透明度: 90%"), "opacity");
        require(PrismJapaneseText.enabled(true).contains("オン"), "toggle enabled");
        require(PrismJapaneseText.enabled(false).contains("オフ"), "toggle disabled");
        require(PrismJapaneseText.reset().equals("初期設定に戻す"), "reset");
        require(PrismJapaneseText.done().equals("完了"), "done");
        for (CrystalPreferences.Theme t: CrystalPreferences.Theme.values()) {
            require(PrismJapaneseText.theme(t).startsWith("テーマ: "), "theme label " + t);
        }
        System.out.println("PASS: Japanese UI labels regardless of Minecraft locale");
    }
    private static void require(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
    }
}
