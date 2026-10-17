# Crystal UI MOD / Prism Glass

Minecraft **1.21.1 / Forge 52.1.0 / Java 21** 向けクライアント専用のカスタムUI MOD（開発中）。

## v0.3：Seraph Crystal（試作）
- 暗いSF系から白金・薄水色・淡い金の「神聖なクリスタル」UIへ改修
- 半透明パネル、光輪、反射、細い金属の縁、虹色のハイライト
- 明るいインベントリスロットと濃い文字色
- 宝石風のPRISMボタン
- 3テーマ：SERAPH / HALO / PRISM、透明度、ON/OFF、設定保存
- v0.1 / v0.2の設定値を互換読み込み
- バニラのインベントリとレシピブック・クラフト・アイテム操作の仕組みを継承

デザインの詳細: [DESIGN.md](DESIGN.md)

## 検証
- Java 21 + Forge 52.1.0：`gradlew.bat --no-daemon build` 成功
- 設定の単体テスト：初期化・透明度・保存・リセット・旧テーマ移行・不正値のケースすべて成功
- v0.3 `prismglass-0.3.0.jar` をATLauncher `modtest` へ配置し、SHA256を照合
- **v0.3の実際の画面描画、Shiftクリック、クラフト、他MOD互換性は未検証**
- リアルタイムの背景ぼかし／屈折は未実装（軽量な疑似ガラス）

## 導入
`build/libs/prismglass-0.3.0.jar` をテスト用Forge 1.21.1インスタンスの `mods` に入れます。同じ `mods` に旧版を同時に入れないでください。

`modtest` では旧版 `prismglass-0.2.0.jar` を `disabledmods` に退避済み。元に戻す場合はv0.3を外してv0.2を `mods` に戻します。普段使いの `sabaibaru` は変更しません。

[要件定義](REQUIREMENTS.md) / [作業報告書](作業報告書.md)
