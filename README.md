# Prism Glass / Crystal UI MOD

Minecraft 1.21.1 / Forge 52.1.0 / Java 21用の透明クリスタルUI MOD（開発中）。

## v0.8：虹色の聖光
v0.7の良かった**透明感とゲーム世界の透け方は維持**し、ガラスの外周にだけ淡い虹色の干渉反射を加える。画面全体を虹色にしたり、白い板を重ねたりしない。

- 上辺・側面・角に薄い虹色のフレネル反射
- 既存の斜めの光・小さなきらめきにもわずかな虹色を追加
- ゆっくりした色の移ろい。チカチカする効果は追加しない
- 日本語設定・透明度設定・テーマ・ON/OFF・Minecraft標準操作は変更なし
- GPUガラスのぼかし・屈折・各スロット描画を維持
- Shaderに失敗した場合は従来のFallbackへ切り替わる（虹色反射は出ない）

## 現時点のテスト
- Java21 / Forge52.1.0 Gradle clean build成功、JAR内Shader資産の一致検査とmodtestへのSHA256照合付き導入に成功
- 見た目は実際のMinecraft `modtest`で要確認。GradleはGLSLを実機GPU上でコンパイルしない。

[DESIGN.md](DESIGN.md) / [作業報告書](作業報告書.md) / [AI_WORKFLOW.md](AI_WORKFLOW.md)

## 安全な復元
`modtest`にだけv0.8を導入し、旧v0.7を`disabledmods`へ残す。問題が出た場合は旧版へ戻す。普段使いの`sabaibaru`には変更しない。
