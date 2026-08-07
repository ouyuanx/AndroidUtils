plugins {
    // Android Library 插件：把当前模块构建成可复用的 AAR，而不是可安装的 APK。
    alias(libs.plugins.android.library)

    // Gradle 官方 Maven 发布插件：生成 AAR、POM、Gradle Module Metadata 等发布产物。
    `maven-publish`

    // Gradle 官方签名插件：使用 PGP 密钥为 Maven Central 发布产物签名。
    signing
}

// GROUP 和 VERSION_NAME 定义在项目根目录的 gradle.properties 中。
// 这里设置的是 Gradle 项目身份；最终依赖坐标还需要下面 publication 中的 artifactId。
group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

android {
    // Android 资源类（R）、BuildConfig 和 Manifest 合并时使用的唯一命名空间。
    namespace = "io.github.ouyuanx.androidutils"

    // 编译时使用 Android API 37；它不决定库支持的最低 Android 版本。
    compileSdk = 37

    defaultConfig {
        // 使用本工具库的应用最低需要 Android 7.0（API 24）。
        minSdk = 24

        // 将库所需的 R8/ProGuard 规则随 AAR 一起交给使用方。
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            // 当前不压缩库本身；使用方在构建 Release 应用时仍可统一执行 R8 优化。
            isMinifyEnabled = false
        }
    }

    compileOptions {
        // Java 源码和生成的 JVM 字节码都以 Java 11 为兼容目标。
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        // Robolectric 单元测试需要读取 Android 资源和 Manifest。
        unitTests.isIncludeAndroidResources = true
    }

    publishing {
        // 只发布 release 变体，避免把带调试信息的 debug AAR 对外发布。
        singleVariant("release") {
            // 附带源码包，方便 Android Studio 跳转和查看源码。
            withSourcesJar()

            // 附带文档包；Maven Central 会校验是否存在对应的 Javadoc JAR。
            withJavadocJar()
        }
    }
}

dependencies {
    // 提供 ContextCompat、IntentCompat、WindowInsetsCompat 等版本兼容 API。
    implementation(libs.androidx.core.ktx)

    // 权限申请 API 直接公开 IPermission，因此使用 api 传递给工具库使用方。
    api(libs.xxpermissions)

    // XXPermissions 官方要求同时引入 DeviceCompat，但本库不会直接暴露其类型。
    implementation(libs.device.compat)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
}

publishing {
    publications {
        // 创建名为 release 的 Maven 发布项。
        register<MavenPublication>("release") {
            // 三个字段共同组成依赖坐标：groupId:artifactId:version。
            // 当前结果为 io.github.ouyuanx:android-utils:0.1.0。
            groupId = providers.gradleProperty("GROUP").get()
            artifactId = providers.gradleProperty("POM_ARTIFACT_ID").get()
            version = providers.gradleProperty("VERSION_NAME").get()

            // Android Gradle Plugin 会在项目配置完成后才创建 release 组件，
            // 因此需要在 afterEvaluate 中把 release AAR 交给 MavenPublication。
            afterEvaluate {
                from(components["release"])
            }

            // 以下内容会写入生成的 POM，供仓库和使用者识别项目来源、许可证与作者。
            pom {
                name = "AndroidUtils"
                description = "A lightweight Android utility library."
                url = "https://github.com/ouyuanx/AndroidUtils"

                // 声明本项目使用 Apache-2.0 开源许可证。
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        distribution = "repo"
                    }
                }

                // Maven Central 要求提供开发者信息。
                developers {
                    developer {
                        id = "ouyuanx"
                        name = "ouyuanx"
                        email = "ouyuanx@users.noreply.github.com"
                        url = "https://github.com/ouyuanx"
                    }
                }

                // 源码管理信息，指向该库对应的 GitHub Git 仓库。
                scm {
                    connection = "scm:git:git://github.com/ouyuanx/AndroidUtils.git"
                    developerConnection = "scm:git:ssh://github.com/ouyuanx/AndroidUtils.git"
                    url = "https://github.com/ouyuanx/AndroidUtils"
                }
            }
        }
    }

    repositories {
        // 这是用于本地检查的文件型 Maven 仓库，不是 Maven Central。
        // 执行 publishReleasePublicationToBuildDirectoryRepository 后，
        // 产物会生成到 utils/build/repo。
        maven {
            name = "buildDirectory"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

signing {
    // 从 Gradle 属性读取内存 PGP 私钥和密码。
    // CI 中应使用 ORG_GRADLE_PROJECT_signingInMemoryKey 和
    // ORG_GRADLE_PROJECT_signingInMemoryKeyPassword 环境变量注入，严禁提交真实密钥。
    val signingKey = providers.gradleProperty("signingInMemoryKey").orNull
    val signingPassword = providers.gradleProperty("signingInMemoryKeyPassword").orNull

    // 没有密钥时跳过签名，保证普通本地构建不受影响；正式发布 Maven Central 时必须提供。
    if (!signingKey.isNullOrBlank()) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["release"])
    }
}
