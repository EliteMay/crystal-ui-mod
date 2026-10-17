# Prism Glass / Crystal UI

Minecraft **1.21.1 / Forge 52.1.0 / Java 21** 向けのクライアント側ガラスインベントリMOD（開発中）。

## v0.6：日本語・可読性改善
- v0.5のGPUガラスシェーダー（背景ぼかし、フレネル反射など）の仕組みを維持
- Minecraft本体が英語設定でも、MOD専用設定画面は日本語で表示
- 設定画面のタイトル・説明・透明度表示の後ろだけを明るい局所的な保護面に変更
- 各ボタンのラベル部分に薄いフロスト処理を足して文字とのコントラストを確保
- PRISMボタンの表示名を「設定」に変更
- 保存済みテーマ、透明度、ON/OFF、初期化、既存アイテム操作を維持

この変更はUI文字の視認性改善を主目的とし、ガラス全体を白塗りに戻さない。

## 現在の検証
- Forge 52.1.0 / Java 21 `gradlew.bat build` 成功
- Java単体テスト（日本語表示の各文言・保存設定の後方互換）成功
- 実際のMinecraftでの見た目・文字可読性とアイテム操作は、`modtest`での検証が必要

[デザイン](DESIGN.md) / [要件](REQUIREMENTS.md) / [開発担当方針](AI_WORKFLOW.md) / [作業報告](作業報告書.md)

## 安全な導入
Forge 1.21.1のテスト用ATLauncher `modtest` に `prismglass-0.6.0.jar` を入れる。同一MODの旧版JARは `disabledmods` に退避し、複数バージョンを同時に有効にしない。普段の `sabaibaru` は変更しない。
