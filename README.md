# AndroidUtils

一个轻量级 Android 工具库，由 [ouyuanx](https://github.com/ouyuanx) 维护。

当前仓库已经完成 Android Library、Maven 发布元数据、本地发布和持续集成的基础配置。
具体工具 API 可以按实际需求逐步添加。

## 项目结构

- `utils`：真正的 Android 工具库模块，构建结果为 AAR，也是将来发布到 Maven Central 的模块。
- `app`：示例应用，用于集成验证、界面演示和手工测试。

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
./gradlew :utils:publishReleasePublicationToBuildDirectoryRepository
```

## 添加工具代码

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
5. 通过 GitHub Actions 发布到 Maven Central。

Maven Central 凭据和 PGP 私钥必须存放在 GitHub Actions Secrets 中，严禁写入源码、
`gradle.properties` 或提交到 Git 仓库。

## 开源许可证

Copyright 2026 ouyuanx

本项目使用 Apache License 2.0，详见 [LICENSE](LICENSE)。
