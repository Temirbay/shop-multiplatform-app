import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.shop.kmp.compose)
}

kotlin {
    // iOS-фреймворк для Xcode нужен только корневому UI-модулю
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    android {
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:network"))
            implementation(project(":core:designsystem"))
            implementation(project(":feature:catalog"))
            api(libs.koin.core)
        }
    }
}
