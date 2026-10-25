# Project Learnings

## 2026-10-25: Forge 52.1.0 配布JARの誤再変換による起動クラッシュ

- **ID:** PRISM-20261025-REOBF
- **Symptom:** ATLauncher `modtest`でPrism Glassが起動直後にクラッシュ。`NoSuchMethodError: ResourceLocation.m_339182_`。
- **Expected:** Forge 1.21.1 / 52.1.0でMOD2件が読み込まれ、タイトル画面に到達する。
- **Actual:** `GlassShaders.java:20`のShader登録でクラッシュ（20:48:51、20:50:01）。
- **Trigger:** `build.gradle`で`reobf = true`に変更したJARをATLauncherに導入した。
- **Root cause:** 対象のForgeクライアントJARは`ResourceLocation.fromNamespaceAndPath(String,String)`を公開するのに、JARは`ResourceLocation.m_339182_`を呼び出していた。一般的なForgeGradleドキュメントだけで判断し、実行環境JARのメソッドと突合せずに配布対応完了と報告した。
- **Final fix:** `reobf = false`に戻し、`clean build`。2MODを修正版へ再配置。
- **Validation:** `javap`で実ランタイムJARとMOD JARのメソッド名が一致すること、`tests/forge-runtime-linkage.ps1`、JAR SHA-256照合、既存の非破壊回帰テストPASS。
- **Severity:** 起動阻害。テストインスタンスのみの影響。本番ワールドは非変更。
- **Regression guard:** Forge 1.21.1の配布用JARを変更する場合、`tests/forge-runtime-linkage.ps1`を実行し、実際のATLauncherランタイムJARが持つシンボル名と一致することを必須にする。
- **Prevention:** パッケージ変換完了やビルド成功だけを実ゲームでの動作保証としない。疑わしいエラーは`latest.log`→最新クラッシュレポート→元ソース→インストール済みランタイムの公開メソッドを突合せる。
- **Unverified:** 修正版を用いた実ゲーム再起動の結果はユーザー確認待ち。
- **Guideへ還元:** ランタイムでロードするMODやプラグインの配布用マッピングを、事前に実行側アーティファクトと照合する品質ガードは汎用的。
