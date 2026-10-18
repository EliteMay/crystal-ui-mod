# Prism Glass / Crystal UI MOD

Minecraft Java Edition **1.21.1 / Forge 52.1.0 / Java 21** 用のクライアント専用ガラスUI MOD（開発中）。

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
```

`iterate.ps1` はワールドを削除しません。Minecraftプロセスを強制終了せず、キー入力やスクリーンショット自動撮影もしません。通常実行はGradle `build` のみで、明示的な `-RunClient` 指定時だけForge開発クライアントを起動します。既存の開発クライアントを見つけたら重複起動を拒否します。起動後にゲーム操作をする場合はテストワールドに限定し、既存セーブへの変更に注意してください。

## 検証状況（2026-10-18）

- 独立実行した非破壊 `iterate.ps1` によるGradle `build`：**成功**
- `tests/iterate-safety.ps1`：**成功**
- `run/saves/New World`：**テスト前後で存在とフォルダ更新日時を維持**
- `build/libs/prismglass-0.9.0.jar` とATLauncher `modtest/mods/prismglass-0.9.0.jar`：SHA-256一致
- 既存の開発ログ：検索対象のGPU glass disabled / shader compile / linkのエラーなし
- **新たな実ゲームE2E成功**：隔離ワールドで日本語設定、透明度・テーマ・ON/OFF・初期化、Shiftクリック移動、原木のクラフト、レシピブックを確認
- **夜間・GUI Scale 3実画面確認**。ユーザーのF3画像はv0.8 通常148 FPS／UI99 FPS、v0.9 通常138 FPS／UI181 FPS（瞬間値）
- 両環境でVSyncオフ・設定値maxFps=260を確認。ただしF3は`T: inf`表示、視点・座標も違うため**性能向上の確証ではない**
- 最新の隔離ゲーム起動でGUI Scale 4を適用し、クラッシュなく終了。描画の一部とGLSLエラー0件を確認したが、OBSウィンドウが重なったためUI全体の目視確認は保留。
- 未確認：同一視点の複数FPSサンプルとフレームタイム、GUI Scale 4の全UIレイアウト、アイテムドラッグ、Shader fallbackと他MOD互換性

## 安全な復元

ATLauncherのテスト用 `modtest` だけで検証してください。普段使いの `sabaibaru` は変更しません。

v0.9に問題があれば、Minecraftを終了し `modtest/mods/prismglass-0.9.0.jar` を外して、退避してあるv0.8 JARを有効に戻せます。戻す前に対象ファイルの存在を確認し、同じMODの複数バージョンを同時に有効にしないでください。

[作業報告書](作業報告書.md) / [開発担当方針](AI_WORKFLOW.md)
