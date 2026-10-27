# Project Learnings

## 2026-10-27: 複雑GUIの装飾だけでは全面ガラス化せず、タイトルが二重描画

- **Symptom:** エンチャント台に「Enchant」の文字が重複し、石切台が灰色のまま。3枚目の砥石は未対応だった。
- **Cause:** Forgeの`ScreenEvent.Render.Post`は元のバニラ不透明背景を消さない。60%の半透明タイトル下地は元の黒文字を隠せず、新しいタイトルとの二重描画になった。薄い反射線も選択肢に干渉。
- **Why previous tests missed it:** 既存テストはJavaのビルド、対象クラス限定、入力非上書きだけを検査。実際の文字位置と視覚密度を検証していなかった。
- **Immediate fix:** v0.13.1で不透明の細いタイトル下地を使い一度だけ読みやすい文字を描画、選択欄にかかる線を撤去。砥石も正規バニラ画面に限定した細い装飾を追加。
- **Safe prototype:** Minecraft 1.21.1ではPNGの中途半端なアルファが正しく透過しない場合があり、GUI背景の完全透明版を任意の別Resource Packで作って試す。設定のON/OFFと連動しないため、未検証のままデフォルトにしない。
- **Regression:** `tests/global-ui-coverage.ps1`でラベルマスクの不透明色・従来の半透明下地の禁止、砥石範囲、入力非上書き、Render.Preガラス下地をチェック。人間が実画面を見るVisual Reviewは引き続き必要。
- **Unverified:** v0.13.1の実機表示と透明プレビューパックによる選択操作はまだ未検証。

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
