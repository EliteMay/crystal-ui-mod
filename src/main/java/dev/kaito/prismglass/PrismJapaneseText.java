package dev.kaito.prismglass;

/**
 * Dedicated Japanese in-game UI copy. Minecraft's current language can be English
 * without switching the player's global language just to use this client-only mod.
 */
public final class PrismJapaneseText {
    private PrismJapaneseText() {}

    public static String title() { return "PRISM GLASS"; }
    public static String subtitle() { return "神聖なクリスタルUI"; }
    public static String opacity(int percent) { return "透明度: " + percent + "%"; }
    public static String enabled(boolean on) { return "ガラスUI: " + (on ? "オン" : "オフ"); }
    public static String theme(CrystalPreferences.Theme theme) {
        return "テーマ: " + switch (theme) {
            case SERAPH -> "セラフ";
            case HALO -> "ヘイロー";
            case PRISM -> "プリズム";
        };
    }
    public static String reset() { return "初期設定に戻す"; }
    public static String done() { return "完了"; }
    public static String openSettings() { return "設定"; }
}
