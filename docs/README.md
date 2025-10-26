[日本語](README-ja.md) | [简体中文](README-zh.md) | English

MrRare2's DPC app based on [OwnDroid](https://github.com/BinTianqi/OwnDroid).

This uses Android's [Device Policy Manager](https://developer.android.com/reference/android/app/admin/DevicePolicyManager).

This is mostly only used for MrRare2 but you can use it as well if you want.
# Features
- Disable camera
- Disable screen capture
- Disable status bar
- Master volume mute
- Common Criteria mode
- Hardware monitor (temperatures, fan speeds)
- Reboot device
- Change time / time zone
- Lots of policies (permission, MTE, Nearby streaming, FRP)
- Lock task mode
- Security logging
- Network (VPN always on, private DNS, WiFi)
- Network logging
- User restrictions (change wifi state, change location state, block factory resetting via settings, etc..)
- App block install / uninstall / hide / suspend
- Manage users
- Reset password
- Set timeout
- Configure keyboard features
- App lock/hide
- Themes (Material Design 3 + Dynamic Color)
# Working modes
- Root
- Shizuku
- [Dhizuku](https://github.com/iamr0s/Dhizuku)
- ADB -> `dpm set-device-owner dev.mr2.dpc/.Receiver`
# Provisioning errors
## Existing accounts
`java.lang.IllegalStateException: Not allowed to set the device owner because there are already some accounts on the device`
### Solution
1. Freeze those apps with accounts using [Hail](https://github.com/aistra0528/Hail).
2. Or uninstall them
## Existing users
`java.lang.IllegalStateException: Not allowed to set the device owner because there are already several users on the device`
### Solution
> [!note]
> Some systems have a feature like "App cloning" or "Kids Space" which are usually just users.
1. Remove those users (secondary users, guest accounts, etc...)
# API
You can check [the receiver](https://github.com/MrRare2/MDPC/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fdev%2Fmr2%2Fdpc%2FApiReceiver.kt) file for the intent extras to sends as well as the arguments
## Intent based
### Sending via shell
```shell
am broadcast -n dev.mr2.dpc/.ApiReceiver -a dev.mr2.dpc.api.ACTION_NAME --es key ...
```
### Sending via code
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
## TCP (advanced)
Send a raw TCP response to `localhost` at the port defined in the app with a base64 encoded AES-GCM 256-bit JSON request, with the key being the SHA-512 of the API key using only the first 32 bytes, under the spec of `IV + ciphertext + tag`. Decoding is the opposite way: base64 decode -> AES decrypt with key -> parse JSON. It also uses the same spec.
# Build
## Termux
- Install deps
  - `pkg update && pkg upgrade -y && pkg install -y gradle kotlin openjdk-17 aapt2 apksigner wget`
  - `termux-setup-storage`
- Install Android SDK tools ([here](https://developer.android.com/studio#command-tools))
- `gradle build` or `gradle assembleRelease`
- `cp app/build/outputs/apk/release/app-release.apk /sdcard/MDPC.apk`
### Build errors
#### AAPT2
##### variant #1
`AAPT2 aapt2-* Daemon #0: Daemon startup failed`
###### Solution
Append this into `~/.gradle/gradle.properties`:
```
android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/bin/aapt2
```
And try again
##### variant #2
`ERROR: AAPT: error: failed to load include path /data/data/com.termux/files/home/apps/sdk/platforms/android-*/android.jar.` (target sdk >= 35)
###### Solution
As of now there has no official solution for this issue (as seen [here](https://github.com/termux/termux-packages/issues/22667)) but you can try replacing the `aapt2` binary on `$PREFIX/bin` with some pre built binaries like [this one](https://github.com/Maximoff/binaries/blob/main/bin%2Farm64-v8a%2Fsdk35%2Faapt2) but **I take no responsibility on these unofficial `aapt2` builds, use at your own risk!**.
## Linux
- `./gradlew build` or `./gradlew assembleRelease`
## Windows
- `./gradle.bat build` or `./gradle.bat assembleRelease`
# License
> Copyright (C) 2024 BinTianqi
>
> Copyright (C) 2025 MrRare2
>
> This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
>
> This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
>
> You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
