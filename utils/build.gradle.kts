plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
    signing
}

group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

android {
    namespace = "io.github.ouyuanx.androidutils"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = providers.gradleProperty("GROUP").get()
            artifactId = providers.gradleProperty("POM_ARTIFACT_ID").get()
            version = providers.gradleProperty("VERSION_NAME").get()

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name = "AndroidUtils"
                description = "A lightweight Android utility library."
                url = "https://github.com/ouyuanx/AndroidUtils"

                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        distribution = "repo"
                    }
                }

                developers {
                    developer {
                        id = "ouyuanx"
                        name = "ouyuanx"
                        email = "ouyuanx@users.noreply.github.com"
                        url = "https://github.com/ouyuanx"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/ouyuanx/AndroidUtils.git"
                    developerConnection = "scm:git:ssh://github.com/ouyuanx/AndroidUtils.git"
                    url = "https://github.com/ouyuanx/AndroidUtils"
                }
            }
        }
    }

    repositories {
        maven {
            name = "buildDirectory"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

signing {
    val signingKey = providers.gradleProperty("signingInMemoryKey").orNull
    val signingPassword = providers.gradleProperty("signingInMemoryKeyPassword").orNull

    if (!signingKey.isNullOrBlank()) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["release"])
    }
}
