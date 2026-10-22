import kz.mtemirbay.shop.buildlogic.androidLibrary
import kz.mtemirbay.shop.buildlogic.autoNamespace
import kz.mtemirbay.shop.buildlogic.lib
import kz.mtemirbay.shop.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("shop.kmp.library")
            pluginManager.apply("org.jetbrains.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<KotlinMultiplatformExtension> {
                // Без executable Compose не бандлит Skiko для веб-тестов (CMP-4906)
                js { binaries.executable() }
                @OptIn(ExperimentalWasmDsl::class)
                wasmJs { binaries.executable() }

                androidLibrary {
                    androidResources { enable = true }
                }
                sourceSets.commonMain.dependencies {
                    implementation(libs.lib("compose-runtime"))
                    implementation(libs.lib("compose-foundation"))
                    implementation(libs.lib("compose-material3"))
                    implementation(libs.lib("compose-ui"))
                    implementation(libs.lib("compose-components-resources"))
                    implementation(libs.lib("compose-uiToolingPreview"))
                    implementation(libs.lib("androidx-lifecycle-viewmodelCompose"))
                    implementation(libs.lib("androidx-lifecycle-runtimeCompose"))
                }
                sourceSets.androidMain.dependencies {
                    implementation(libs.lib("compose-uiTooling"))
                }
            }
            // Res-класс ресурсов в пакете модуля, а не в "shopmultiplatformapp.<путь>.generated.resources"
            extensions.getByType<ComposeExtension>().extensions.configure<ResourcesExtension> {
                packageOfResClass = "$autoNamespace.generated.resources"
            }
            dependencies.add("androidRuntimeClasspath", libs.lib("compose-uiTooling"))
        }
    }
}
