# AndroidUtils

一个轻量级 Android 工具库，由 [ouyuanx](https://github.com/ouyuanx) 维护。

当前仓库已经完成 Android Library、常用工具 API、示例应用、Maven 发布元数据和持续集成配置。

## 已提供的工具

| 包名 | 工具 | 用途 |
| --- | --- | --- |
| `activity` | `findActivity()` | 从 `Context`、`ContextWrapper` 中安全查找 `Activity` |
| `clipboard` | `copyText()` | 将文本复制到系统剪贴板 |
| `file` | `createFile()`、`ensureDirectory()`、`copyToWithParents()`、`moveTo()` | 创建、复制、移动、重命名和删除文件 |
| `file` | `sha256()`、`formattedSize()`、`copyToFile()`、`copyToUri()` | 计算摘要与大小、在 `File` 和 `content://` 之间复制内容 |
| `intent` | `startActivitySafely()`、`openUrl()`、`shareText()` | 安全启动页面、打开 HTTP(S) 地址、分享文本 |
| `intent` | `parcelableExtra()`、`parcelable()` | 跨 Android 版本读取类型安全的 `Parcelable` |
| `log` | `LogUtils` | 基于 Timber 输出分级日志 |
| `network` | `NetworkMonitor`、`NetworkState` | 获取并监听网络可用性、验证状态、计费状态和传输类型 |
| `packageinfo` | `appName()`、`appVersionName()`、`appVersionCode()` | 获取当前应用名称和版本信息 |
| `packageinfo` | `isPackageInstalled()`、`openApp()` | 检查并打开指定应用 |
| `packageinfo` | `appSignatureMd5()`、`appSignatureSha1()`、`appSignatureSha256()` | 获取当前应用的签名证书指纹 |
| `permission` | `hasPermission()`、`requestPermissions()`、`openPermissionSettings()` | 检查、申请权限和打开权限设置页 |
| `qrcode` | `QRCodeUtils` | 生成普通或带中心 Logo 的二维码 Bitmap |
| `storage` | `MMKVUtils` | 初始化 MMKV，读写基础类型和 Parcelable |
| `toast` | `ToastUtils` | 显示普通、成功、失败和警告 Toast |
| `uri` | `displayName()`、`contentSize()`、`mimeType()` | 读取 `content://` URI 的常用元数据 |
| `view` | `showKeyboard()`、`hideKeyboard()` | 显示或隐藏软键盘 |

所有公共 API 都位于 `io.github.ouyuanx.androidutils` 包下，并按用途拆分子包，按需导入即可。

## 快速使用

复制文本、打开网页和分享文本：

```kotlin
import io.github.ouyuanx.androidutils.clipboard.copyText
import io.github.ouyuanx.androidutils.intent.openUrl
import io.github.ouyuanx.androidutils.intent.shareText

context.copyText("AndroidUtils")
context.openUrl("https://github.com/ouyuanx/AndroidUtils")
context.shareText("推荐一个 Android 工具库：AndroidUtils")
```

检查权限并打开应用设置页：

```kotlin
import android.Manifest
import io.github.ouyuanx.androidutils.permission.hasPermission
import io.github.ouyuanx.androidutils.permission.openAppSettings

if (!context.hasPermission(Manifest.permission.CAMERA)) {
    context.openAppSettings()
}
```

权限申请基于 XXPermissions。由于 XXPermissions 发布在 JitPack，使用本库前需要在调用方的
`settings.gradle.kts` 中添加仓库：

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

申请相机权限：

```kotlin
import com.hjq.permissions.permission.PermissionLists
import io.github.ouyuanx.androidutils.permission.openPermissionSettings
import io.github.ouyuanx.androidutils.permission.requestPermissions

val camera = PermissionLists.getCameraPermission()
context.requestPermissions(camera) { result ->
    when {
        result.allGranted -> openCamera()
        result.doNotAskAgain -> context.openPermissionSettings(result.deniedPermissions)
        else -> showPermissionDeniedMessage()
    }
}
```

权限封装不会自动显示 Toast 或跳转设置页，以免工具库替应用决定 UI 和交互。读取相册图片时不要继续
使用旧的读写外部存储组合；使用 XXPermissions 的 `getReadMediaImagesPermission()`，或者在仅需
用户选择图片时优先使用系统 Photo Picker。

创建、移动文件以及处理文件选择器返回的 `Uri`：

```kotlin
val draft = File(context.filesDir, "drafts/message.txt").createFile()
draft.writeText("待发布内容")

val published = draft.moveTo(File(context.filesDir, "published/message.txt"))
val checksum = published.sha256()

contentResolver.copyToFile(selectedUri, File(context.cacheDir, "selected.bin"))
published.copyToUri(contentResolver, destinationUri)
```

在 Application 中初始化 MMKV、日志和 Toast：

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        MMKVUtils.init(this)
        LogUtils.init(isDebug = BuildConfig.DEBUG)
        ToastUtils.init(this)
    }
}
```

读写 MMKV：

```kotlin
MMKVUtils.putString("username", "ouyuanx")
val username = MMKVUtils.getString("username")
```

输出日志：

```kotlin
LogUtils.d("用户 %s 登录", username)
LogUtils.e(exception, "请求失败")
```

显示 Toast 和生成二维码：

```kotlin
ToastUtils.success("保存成功")
ToastUtils.error("请求失败")

val qrCode = QRCodeUtils.generate(
    content = "https://github.com/ouyuanx/AndroidUtils",
    size = 512,
    logo = appLogo,
)
```

MMKV 2.x 支持 Android API 23 及以上，但只提供 64 位架构；需要兼容 32 位设备时应改用 MMKV
1.3.x LTS。

监听网络状态前，需要在应用的 `AndroidManifest.xml` 中声明普通权限：

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

```kotlin
import io.github.ouyuanx.androidutils.network.NetworkMonitor

val monitor = NetworkMonitor(context)
val observation = monitor.observe { state ->
    println("网络可用：${state.isAvailable}，已验证：${state.isValidated}")
}

// Activity、Fragment 或 Compose 离开作用域时停止监听。
observation.close()
```

读取 `Parcelable`：

```kotlin
import io.github.ouyuanx.androidutils.intent.parcelableExtra

val user = intent.parcelableExtra<User>("user")
```

读取应用信息和签名证书指纹：

```kotlin
import io.github.ouyuanx.androidutils.packageinfo.appName
import io.github.ouyuanx.androidutils.packageinfo.appSignatureSha256
import io.github.ouyuanx.androidutils.packageinfo.appVersionName

val name = context.appName()
val versionName = context.appVersionName()
val sha256 = context.appSignatureSha256()
```

签名指纹使用大写十六进制和冒号分隔。常规应用只有一个当前签名，可以直接使用上面的便捷方法；
如需处理多签名或签名证书轮换，可调用 `appSignatureFingerprints()` 获取列表。MD5 和 SHA-1
仅用于兼容要求这些指纹的旧平台，新接入的安全校验应优先使用 SHA-256。

Android 11（API 30）及以上版本会限制应用可见性。使用 `isPackageInstalled()` 查询普通第三方
应用前，可能需要在调用方的 `AndroidManifest.xml` 中通过 `<queries>` 声明目标包名：

```xml
<queries>
    <package android:name="com.example.target" />
</queries>
```

## 项目结构

- `utils`：真正的 Android 工具库模块，构建结果为 AAR，也是将来发布到 Maven Central 的模块。
- `app`：示例应用，用于集成验证、界面演示和手工测试。

示例应用包含权限、MMKV 存取和 Timber 日志的可点击测试用例，并展示授权、拒绝、永久拒绝、
无法发起请求以及键值读写结果。

## 环境要求

- JDK 21（Gradle 运行环境）
- Android API 37（`compileSdk`）
- 最低支持 Android 7.0 / API 24（`minSdk`）

## Maven 坐标

```text
io.github.ouyuanx:android-utils:0.1.0
```

当前版本尚未正式发布到 Maven Central。首次发布成功后，可以在其他 Android 项目中添加：

```kotlin
dependencies {
    implementation("io.github.ouyuanx:android-utils:0.1.0")
}
```

## 本地开发

在 macOS 或 Linux 上构建并测试整个项目：

```shell
./gradlew build
```

在 Windows PowerShell 或 CMD 中执行：

```bat
gradlew.bat build
```

将 `release` 版本发布到本机 Maven Local（通常位于用户目录下的 `.m2/repository`）：

```shell
./gradlew :utils:publishToMavenLocal
```

在 Windows 中对应的命令为：

```bat
gradlew.bat :utils:publishToMavenLocal
```

生成一个位于 `utils/build/repo` 的文件型 Maven 仓库，便于检查 AAR、POM、源码包和文档包：

```shell
./gradlew :utils:publishMavenPublicationToBuildDirectoryRepository
```

## 参与开发

公共 API 建议放在以下目录：

```text
utils/src/main/java/io/github/ouyuanx/androidutils
```

新增功能时建议同时完成：

- 为公共类型和方法编写 KDoc。
- 在 `utils/src/test` 中添加单元测试。
- 在 `app` 中添加使用示例。
- 避免无意中暴露实现细节，内部代码优先使用 Kotlin 的 `internal` 可见性。

## 版本与发布

项目采用语义化版本。准备发布新版本时：

1. 修改根目录 `gradle.properties` 中的 `VERSION_NAME`。
2. 更新 `CHANGELOG.md`。
3. 执行完整构建和本地 Maven 发布验证。
4. 创建与版本对应的 Git 标签，例如 `v0.1.0`。
5. 推送与版本对应的 Git 标签，由 GitHub Actions 签名并发布到 Maven Central。

Maven Central 需要在 GitHub Actions Secrets 中配置 `MAVEN_CENTRAL_USERNAME`、
`MAVEN_CENTRAL_PASSWORD`、`SIGNING_IN_MEMORY_KEY` 和 `SIGNING_IN_MEMORY_KEY_PASSWORD`。
这些凭据和 PGP 私钥严禁写入源码、`gradle.properties` 或提交到 Git 仓库。

## 开源许可证

Copyright 2026 ouyuanx

本项目使用 Apache License 2.0，详见 [LICENSE](LICENSE)。
