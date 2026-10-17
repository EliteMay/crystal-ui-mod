# Prism Glass / Crystal UI

Minecraft Java **1.21.1 / Forge 52.1.0 / Java 21** 向けのクライアント側ガラスインベントリMOD（開発中）。

## v0.7：日本語テキストと本物のガラス質感を統一

v0.6のゲーム内スクリーンショットで、設定見出しの巨大な白帯とボタン内の四角い白い背景が、GPUガラスの美しさを損なっていることを確認。v0.7は**元のGPUガラス・背景ぼかし・反射を保ち、文字背景の「貼り付けた白い板」をなくす**局所修正。

- 設定画面の巨大な白い保護面を撤去、代わりに細い金色の仕切り
- 各ボタンの白い矩形を撤去し、テキストに1 GUIピクセルの淡い輪郭を付ける
- 日本語、テーマ、透明度、設定保存、ON/OFFと既存ワールドの操作を維持
- 文字輪郭はゲーム側の描画テキストにだけ適用し、GPUガラスShaderは変更しない
- `modtest` で表示と可読性を確認するまではDraft扱い

## 検証状態
- Java 21 + Forge 52.1.0 `gradlew.bat clean build` 成功
- Gradle内のJavaテスト成功
- Forgeの既存Deprecated警告あり（新たなコンパイルエラーではない）
- **v0.7の実ゲームスクリーンショット、昼夜での視認性、実スロット操作は未確認**

## 導入と復旧
ATLauncherのテスト用`modtest`へ`prismglass-0.7.0.jar`を配置。旧`prismglass-0.6.0.jar`は`disabledmods`に退避。問題があればMinecraftを終了して旧版へ戻す。同名MODの複数JARを同時にmodsへ置かない。普段使用する`sabaibaru`には触れない。

[デザイン仕様](DESIGN.md) / [開発方針](AI_WORKFLOW.md) / [作業報告書](作業報告書.md)
