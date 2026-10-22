import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBrowserDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

// 8080 занят :server, поэтому dev-сервер веба — на 8081
fun KotlinJsBrowserDsl.devServerPort() {
    commonWebpackConfig {
        devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply { port = 8081 }
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    js {
        browser { devServerPort() }
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser { devServerPort() }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":app:shared"))

            implementation(libs.compose.ui)
        }
    }
}