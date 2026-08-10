plugins {
    // Android Library 插件：把当前模块构建成可复用的 AAR，而不是可安装的 APK。
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech.maven.publish)
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

}

dependencies {
    // 提供 ContextCompat、IntentCompat、WindowInsetsCompat 等版本兼容 API。
    implementation(libs.androidx.core.ktx)

    // 权限申请 API 直接公开 IPermission，因此使用 api 传递给工具库使用方。
    api(libs.xxpermissions)

    // XXPermissions 官方要求同时引入 DeviceCompat，但本库不会直接暴露其类型。
    implementation(libs.device.compat)

    // MMKV 和 Timber 只是实现细节，不出现在工具库的公开 API 中。
    implementation(libs.mmkv)
    implementation(libs.timber)

    implementation(libs.zxing.core)
    implementation(libs.toaster)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
}

mavenPublishing {
    publishToMavenCentral()
    if (!providers.gradleProperty("signingInMemoryKey").orNull.isNullOrBlank()) {
        signAllPublications()
    }
    coordinates(
        providers.gradleProperty("GROUP").get(),
        providers.gradleProperty("POM_ARTIFACT_ID").get(),
        providers.gradleProperty("VERSION_NAME").get(),
    )
    pom {
        name.set("AndroidUtils")
        description.set("A lightweight Android utility library.")
        inceptionYear.set("2026")
        url.set("https://github.com/ouyuanx/AndroidUtils")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("ouyuanx")
                name.set("ouyuanx")
                email.set("ouyuanx@users.noreply.github.com")
                url.set("https://github.com/ouyuanx")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/ouyuanx/AndroidUtils.git")
            developerConnection.set("scm:git:ssh://git@github.com/ouyuanx/AndroidUtils.git")
            url.set("https://github.com/ouyuanx/AndroidUtils")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "buildDirectory"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}
