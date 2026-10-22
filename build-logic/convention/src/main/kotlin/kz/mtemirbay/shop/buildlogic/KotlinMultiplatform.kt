package kz.mtemirbay.shop.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun KotlinMultiplatformExtension.androidLibrary(action: Action<KotlinMultiplatformAndroidLibraryTarget>) {
    (this as ExtensionAware).extensions.configure(KotlinMultiplatformAndroidLibraryTarget::class.java, action)
}

internal fun Project.configureKotlinMultiplatform(kotlin: KotlinMultiplatformExtension) = with(kotlin) {
    iosArm64()
    iosSimulatorArm64()
    jvm()
    js { browser() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    androidLibrary {
        namespace = autoNamespace
        compileSdk = libs.intVersion("android-compileSdk")
        minSdk = libs.intVersion("android-minSdk")
        compilerOptions { jvmTarget.set(JVM_TARGET) }
        withHostTest { isIncludeAndroidResources = true }
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.lib("kotlin-test"))
    }
}
