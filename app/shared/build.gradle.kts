import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.shop.kmp.compose)
    // Маршруты навигации — @Serializable, чтобы стек экранов переживал поворот и смерть процесса
    alias(libs.plugins.kotlinSerialization)
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
            implementation(project(":feature:product"))
            implementation(project(":feature:cart"))
            implementation(project(":feature:checkout"))
            implementation(project(":core:data"))
            implementation(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
            api(libs.koin.core)
        }
    }
}
