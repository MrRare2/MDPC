日本語 | [简体中文](README-zh.md) | [English](README.md)

MrRare2のDPCアプリは、[OwnDroid](https://github.com/BinTianqi/OwnDroid)に基づいています。

このアプリはAndroidの[Device Policy Manager](https://developer.android.com/reference/android/app/admin/DevicePolicyManager)を使用しています。

主にMrRare2向けに開発されていますが、希望すれば他のユーザーも利用可能です。
# 機能
- カメラの無効化
- スクリーンキャプチャの無効化
- ステータスバーの無効化
- マスターボリュームのミュート
- コモンクライテリアモード
- ハードウェアモニター（温度、ファン速度）
- デバイスの再起動
- 時間/タイムゾーンの変更
- 多数のポリシー（パーミッション、MTE、Nearbyストリーミング、FRP）
- ロックタスクモード
- セキュリティロギング
- ネットワーク（VPN常時接続、プライベートDNS、WiFi）
- ネットワークロギング
- ユーザー制限（WiFi状態の変更、位置情報状態の変更、設定経由での工場出荷時リセットのブロックなど）
- アプリのインストール/アンインストール/非表示/一時停止のブロック
- ユーザーの管理
- パスワードのリセット
- タイムアウトの設定
- キーボード機能の設定
- アプリのロック/非表示
- テーマ（Material Design 3 + ダイナミックカラー）
# 動作モード
- Root
- Shizuku
- [Dhizuku](https://github.com/iamr0s/Dhizuku)
- ADB -> `dpm set-device-owner dev.mr2.dpc/.Receiver`
# プロビジョニングエラー
## 既存のアカウント
`java.lang.IllegalStateException: Not allowed to set the device owner because there are already some accounts on the device`
### 解決方法
1. アカウントを持つアプリを[Hail](https://github.com/aistra0528/Hail)を使用してフリーズする。
2. または、それらのアプリをアンインストールする。
## 既存のユーザー
`java.lang.IllegalStateException: Not allowed to set the device owner because there are already several users on the device`
### 解決方法
> [!note]
> 一部のシステムには「アプリクローニング」や「キッズスペース」といった機能があり、これらは通常ユーザーとして扱われます。
1. それらのユーザー（セカンダリユーザー、ゲストアカウントなど）を削除する。
# API
インテントエクストラや引数については、[レシーバーファイル](https://github.com/MrRare2/MDPC/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fdev%2Fmr2%2Fdpc%2FApiReceiver.kt)を確認してください。
### シェル経由での送信
```shell
am broadcast -n dev.mr2.dpc/.ApiReceiver -a dev.mr2.dpc.api.ACTION_NAME --es key ...
```
### コード経由での送信
#### Java
```java
Intent intent = new Intent("dev.mr2.dpc.api.ACTION_NAME");
intent.setClassName("dev.mr2.dpc", "dev.mr2.dpc.ApiReceiver");
intent.putExtra("key", "...");
sendBroadcast(intent);
```
#### Kotlin
```kotlin
val intent = Intent("dev.mr2.dpc.api.ACTION_NAME").apply {
    setClassName("dev.mr2.dpc", "dev.mr2.dpc.ApiReceiver")
    putExtra("key", "...")
}
sendBroadcast(intent)
```
## TCP（高度）
アプリで定義されたポートの`localhost`に対して、生のTCPレスポンスを送信します。
リクエストはJSONをAES-GCM 256-ビットで暗号化し、Base64でエンコードします。
キーはAPIキーのSHA-512ハッシュの最初の32バイトを使用します。
暗号化の形式は「IV + 暗号文 + GCMタグ」です。

復号の手順も同様です：Base64デコード → AESで復号 → JSONを解析。

> [!WARNING]
> TCP APIの使用は**自己責任**で行ってください！
> このAPIおよびアプリ自体の使用によって発生した**いかなる損害に対しても、開発者は一切の責任を負いません**。

# ビルド
## Termux
- 依存関係のインストール
  - `pkg update && pkg upgrade -y && pkg install -y gradle kotlin openjdk-17 aapt2 apksigner wget`
  - `termux-setup-storage`
- Android SDKツールのインストール（[こちら](https://developer.android.com/studio#command-tools)）
- `gradle build` または `gradle assembleRelease`
- `cp app/build/outputs/apk/release/app-release.apk /sdcard/MDPC.apk`
### ビルドエラー
#### AAPT2
##### バリアント #1
`AAPT2 aapt2-* Daemon #0: Daemon startup failed`
###### 解決方法
`~/.gradle/gradle.properties`に以下を追加：
```
android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/bin/aapt2
```
そして再度試行する。
##### バリアント #2
`ERROR: AAPT: error: failed to load include path /data/data/com.termux/files/home/apps/sdk/platforms/android-*/android.jar.`（ターゲットSDK >= 35）
###### 解決方法
現在のところ、この問題に対する公式な解決策はありません（[こちら](https://github.com/termux/termux-packages/issues/22667)を参照）。ただし、`$PREFIX/bin`の`aapt2`バイナリを[このような](https://github.com/Maximoff/binaries/blob/main/bin%2Farm64-v8a%2Fsdk35%2Faapt2)事前ビルドされたバイナリに置き換えることを試せますが、**これらの非公式な`aapt2`ビルドについては一切の責任を負いません。自己責任で使用してください**。
## Linux
- `./gradlew build` または `./gradlew assembleRelease`
## Windows
- `./gradle.bat build` または `./gradle.bat assembleRelease`
# ライセンス
> Copyright (C) 2024 BinTianqi
>
> Copyright (C) 2025 MrRare2
>
> This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
>
> This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
>
> You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
