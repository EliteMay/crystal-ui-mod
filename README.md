# Crystal UI MOD: Prism Glass v0.1.0 (試作)

Minecraft **1.21.1 / Forge 52.1.0 / Java 21** 向けに、半透明ガラス・クリスタル風のUIを試作しています。

## 現在ある機能
- 標準インベントリとオプション画面にガラスフレームを追加
- 画面右上の `PRISM` から独自の設定画面を開く
- テーマ3種類、透明度（20～90%）、装飾ON/OFF、初期設定へ戻す
- 設定を `config/prismglass.properties` へ保存
- スロットの座標・クリック処理・クラフトやネットワーク同期は変更しない

**まだない機能**：インベントリ本体の全面リデザイン、ゲーム本体の設定メニュー全面置換、ぼかしシェーダー、音楽・YouTube Music連携。

## 検証状況
設定保存ロジックのJava 21テストは成功。**Forge 52.1.0でのGradleビルドとJava 21設定保存テストは成功しました。Minecraft実ゲーム起動と他MOD互換性は未検証です。**

## Windowsで試作ビルド
1. [Forge公式1.21.1ダウンロード](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.1.html) から **52.1.0 の MDK** をダウンロード（Installerではありません）。
2. Java 21 JDKがあるWindows環境で、このリポジトリのフォルダをPowerShellで開く。
3. 次を実行。

```powershell
powershell -ExecutionPolicy Bypass -File .\build-from-mdk.ps1 -MdkZip "$HOME\Downloads\forge-1.21.1-52.1.0-mdk.zip"
```

スクリプトは公式MDKのSHA1を照合し、既存のATLauncherインスタンスを変更せず新しい `forge-workspace` にビルドします。ビルド成功後、`forge-workspace/build/libs/prismglass-0.1.0.jar` が生成される予定です。

まずはテスト用インスタンスで起動し、アイテム移動・Shiftクリック・クラフトなどを確認してください。

## 権利について
ソースコードのライセンスは未決定です。第三者への改変・再配布権はまだ付与していません。

詳細: [要件定義](REQUIREMENTS.md)
