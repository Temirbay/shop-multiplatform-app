import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":app:shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "kz.mtemirbay.shop"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "kz.mtemirbay.shop"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}
// Пробрасывает localhost:8080 устройства на localhost:8080 компьютера, где запущен :server.
// Запускается после каждой debug-сборки, в том числе из Android Studio. Нет устройства или adb — просто пропускается
val adbReverse by tasks.registering(Exec::class) {
    val adb = androidComponents.sdkComponents.adb.get().asFile
    onlyIf { adb.exists() }
    executable = adb.absolutePath
    args("reverse", "tcp:8080", "tcp:8080")
    isIgnoreExitValue = true
}
tasks.matching { it.name == "assembleDebug" || it.name == "installDebug" }.configureEach {
    finalizedBy(adbReverse)
}
