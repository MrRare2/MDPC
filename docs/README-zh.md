[日本語](README-ja.md) | 简体中文 | [English](README.md)

MrRare2的DPC应用基于[OwnDroid](https://github.com/BinTianqi/OwnDroid)开发。

此应用使用Android的[Device Policy Manager](https://developer.android.com/reference/android/app/admin/DevicePolicyManager)。

主要为MrRare2设计，但其他用户也可以根据需要使用。
# 功能
- 禁用摄像头
- 禁用屏幕截图
- 禁用状态栏
- 主音量静音
- 通用准则模式
- 硬件监控（温度、风扇速度）
- 设备重启
- 更改时间/时区
- 多种策略（权限、MTE、Nearby流媒体、FRP）
- 锁定任务模式
- 安全日志记录
- 网络（始终开启VPN、私有DNS、WiFi）
- 网络日志记录
- 用户限制（更改WiFi状态、更改定位状态、阻止通过设置进行出厂重置等）
- 阻止应用安装/卸载/隐藏/暂停
- 管理用户
- 重置密码
- 设置超时
- 配置键盘功能
- 应用锁定/隐藏
- 主题（Material Design 3 + 动态颜色）
# 工作模式
- Root
- Shizuku
- [Dhizuku](https://github.com/iamr0s/Dhizuku)
- ADB -> `dpm set-device-owner dev.mr2.dpc/.Receiver`
# 设备配置错误
## 已有账户
`java.lang.IllegalStateException: Not allowed to set the device owner because there are already some accounts on the device`
### 解决方法
1. 使用[Hail](https://github.com/aistra0528/Hail)冻结包含账户的应用。
2. 或卸载这些应用。
## 已有用户
`java.lang.IllegalStateException: Not allowed to set the device owner because there are already several users on the device`
### 解决方法
> [!note]
> 某些系统具有“应用克隆”或“儿童空间”等功能，这些通常被视为用户。
1. 删除这些用户（次要用户、访客账户等）。
# API
有关发送的意图附加参数及参数详情，请查看[接收器文件](https://github.com/MrRare2/MDPC/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fdev%2Fmr2%2Fdpc%2FApiReceiver.kt)。
## 通过Shell发送
```shell
am broadcast -n dev.mr2.dpc/.ApiReceiver -a dev.mr2.dpc.api.ACTION_NAME --es key ...
```
## 通过代码发送
### Java
```java
Intent intent = new Intent("dev.mr2.dpc.api.ACTION_NAME");
intent.setClassName("dev.mr2.dpc", "dev.mr2.dpc.ApiReceiver");
intent.putExtra("key", "...");
sendBroadcast(intent);
```
### Kotlin
```kotlin
val intent = Intent("dev.mr2.dpc.api.ACTION_NAME").apply {
    setClassName("dev.mr2.dpc", "dev.mr2.dpc.ApiReceiver")
    putExtra("key", "...")
}
sendBroadcast(intent)
```
# 构建
## Termux
- 安装依赖项
  - `pkg update && pkg upgrade -y && pkg install -y gradle kotlin openjdk-17 aapt2 apksigner wget`
  - `termux-setup-storage`
- 安装Android SDK工具（[此处](https://developer.android.com/studio#command-tools)）
- `gradle build` 或 `gradle assembleRelease`
- `cp app/build/outputs/apk/release/app-release.apk /sdcard/MDPC.apk`
### 构建错误
#### AAPT2
##### 变体 #1
`AAPT2 aapt2-* Daemon #0: Daemon startup failed`
###### 解决方法
在`~/.gradle/gradle.properties`中添加以下内容：
```
android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/bin/aapt2
```
然后再次尝试。
##### 变体 #2
`ERROR: AAPT: error: failed to load include path /data/data/com.termux/files/home/apps/sdk/platforms/android-*/android.jar.`（目标SDK >= 35）
###### 解决方法
目前此问题尚无官方解决方案（参见[此处](https://github.com/termux/termux-packages/issues/22667)），但可以尝试将`$PREFIX/bin`中的`aapt2`二进制文件替换为[此类](https://github.com/Maximoff/binaries/blob/main/bin%2Farm64-v8a%2Fsdk35%2Faapt2)预构建二进制文件，但**我对这些非官方`aapt2`构建不承担任何责任，使用风险自负**。
## Linux
- `./gradlew build` 或 `./gradlew assembleRelease`
## Windows
- `./gradle.bat build` 或 `./gradle.bat assembleRelease`
# 许可证
> Copyright (C) 2024 BinTianqi
>
> Copyright (C) 2025 MrRare2
>
> This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
>
> This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
>
> You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
