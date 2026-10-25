# Prism Glass / Crystal UI MOD

Minecraft Java Edition **1.21.1 / Forge 52.1.0 / Java 21** 用のクライアント専用ガラスUI MOD（開発中）。

## v0.10.0：UI全体へのガラスデザインの展開（開発版）

- プレイヤーのインベントリに加え、チェスト・作業台・ホッパー・ディスペンサー/ドロッパー・シュルカーの背景とスロットをガラス化。元のMinecraftのMenu・クリック・スロット座標は変更しない。
- タイトル・ポーズ・オプションなどの標準画面、複雑な収納画面には共通の反射縁を追加。タイトルとポーズ画面にもPrism設定の入口を用意。
- テーマ/透過率/ON・OFFを既存設定から継承。チャット・死亡画面・読み込み・HUD・他MOD独自画面は当面変更しない。
- **完全な「全画面ガラス化」ではなく第1段階。** 複雑な作業画面の背景とHUDは今後の対象。画面別の対応状況と実機テスト項目は[UI_COVERAGE.md](UI_COVERAGE.md)を参照。
- Java21 / Forge52.1.0でクリーンビルド、画面クラス置換の範囲、実ランタイムメソッド照合、安全なビルドスクリプトは自動検査済み。v0.10.0の実ゲーム視覚確認はまだ。
- 稼働中のMinecraftへMODを上書きしない。ATLauncher`modtest`を終了後に`install-global-ui-modtest.ps1`で導入/復元し、`sabaibaru`には触れない。

## v0.9：自然なプリズム光学

v0.8の「虹色を縁に塗った」印象を改善するため、ガラスの光学表現を一つの仮想光源で統一しました。

- 左上の光源に対し、ガラス縁の傾斜が適切な場所だけ色分散の反射が生じる
- 7波長近似による屈折・色収差。淡い銀白の鏡面光と淡金の内部散乱
- 局所的なブルーム・小さなグリント。色相を機械的に回転させない
- これまでの透明感、背景のぼかし、日本語UI、スロット、クラフト処理は維持
- ゲーム内で描画できないGPUでは既存のフォールバック処理を使用

詳しい仕組みは [DESIGN.md](DESIGN.md) にあります。

## 安全なビルド・テスト

Windows（Java 21が導入済みの開発用PC）で以下を実行します。

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tests\iterate-safety.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\iterate.ps1 -Tag manual
powershell -NoProfile -ExecutionPolicy Bypass -File .\tests\forge-runtime-linkage.ps1
```

`iterate.ps1` はワールドを削除しません。Minecraftプロセスを強制終了せず、キー入力やスクリーンショット自動撮影もしません。通常実行はGradle `build` のみで、明示的な `-RunClient` 指定時だけForge開発クライアントを起動します。既存の開発クライアントを見つけたら重複起動を拒否します。起動後にゲーム操作をする場合はテストワールドに限定し、既存セーブへの変更に注意してください。

## 2026-10-25 Forge配布互換性のクラッシュ修正

- ATLauncher `modtest` で `NoSuchMethodError: ResourceLocation.m_339182_` が発生。`GlassShaders`のシェーダー登録時にクラッシュした。
- 原因は、Forge 52.1.0の実行用JARが `ResourceLocation.fromNamespaceAndPath` を提供しているのに、直前に設定した `reobf = true` が存在しない `m_339182_` 呼び出しを生成したこと。
- **`reobf = false` に戻し、クリーンビルドとATLauncher実行用JARに対するメソッド名照合を成功させた。**
- MOD2件は修正版に差し替えたが、修正後のMinecraft再起動と実ゲーム併用動作はユーザー確認待ち。誤った判断と検証過程は [配布互換性検証_20261025.md](配布互換性検証_20261025.md) に記録。

## 検証状況（2026-10-18）

- 独立実行した非破壊 `iterate.ps1` によるGradle `build`：**成功**
- `tests/iterate-safety.ps1`：**成功**
- `run/saves/New World`：**テスト前後で存在とフォルダ更新日時を維持**
- `build/libs/prismglass-0.9.0.jar` とATLauncher `modtest/mods/prismglass-0.9.0.jar`：SHA-256一致
- 既存の開発ログ：検索対象のGPU glass disabled / shader compile / linkのエラーなし
- **新たな実ゲームE2E成功**：隔離ワールドで日本語設定、透明度・テーマ・ON/OFF・初期化、Shiftクリック移動、原木のクラフト、レシピブックを確認
- **夜間・GUI Scale 3実画面確認**。ユーザーのF3画像はv0.8 通常148 FPS／UI99 FPS、v0.9 通常138 FPS／UI181 FPS（瞬間値）
- 両環境でVSyncオフ・設定値maxFps=260を確認。ただしF3は`T: inf`表示、視点・座標も違うため**性能向上の確証ではない**
- **GUI Scale 4のユーザー提供スクショを確認**：昼の砂漠でカスタムインベントリの枠・スロット・プレイヤー・「設定」ボタンに画面端の見切れなし。F3オーバーレイで細部の視認性は評価保留。F3で315 FPS（参考瞬間値）。Video Settings画面で「GUI Scale: 4」「VSync: OFF」「Max Framerate: Unlimited」を確認。
- Minecraft標準のOptionsとVideo Settingsは、現行コードではガラス化せず、Optionsの右上に専用「設定」ボタンを追加する設計。**Prism Glass独自の日本語設定画面は今回の3枚に含まれず、GUI Scale 4での視認性は未確認**。
- 未確認：同一視点の複数FPSサンプルとフレームタイム、GUI Scale 4のPrism Glass独自設定画面とF3非表示時の読みやすさ、アイテムドラッグ、Shader fallbackと他MOD互換性

## 安全な復元

ATLauncherのテスト用 `modtest` だけで検証してください。普段使いの `sabaibaru` は変更しません。

v0.9に問題があれば、Minecraftを終了し `modtest/mods/prismglass-0.9.0.jar` を外して、退避してあるv0.8 JARを有効に戻せます。戻す前に対象ファイルの存在を確認し、同じMODの複数バージョンを同時に有効にしないでください。

[作業報告書](作業報告書.md) / [開発担当方針](AI_WORKFLOW.md)
