import kz.mtemirbay.shop.buildlogic.androidLibrary
import kz.mtemirbay.shop.buildlogic.lib
import kz.mtemirbay.shop.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("shop.kmp.library")
            pluginManager.apply("org.jetbrains.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<KotlinMultiplatformExtension> {
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
            dependencies.add("androidRuntimeClasspath", libs.lib("compose-uiTooling"))
        }
    }
}
