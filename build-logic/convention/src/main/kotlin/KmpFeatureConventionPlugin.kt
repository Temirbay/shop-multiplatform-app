import kz.mtemirbay.shop.buildlogic.lib
import kz.mtemirbay.shop.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** Фича-модуль: экран(ы) на Compose + данные из сети + Koin. */
class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("shop.kmp.compose")

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(project(":core:ui"))
                    implementation(project(":core:network"))
                    implementation(libs.lib("koin-core"))
                    implementation(libs.lib("koin-composeViewModel"))
                    implementation(libs.lib("coil-compose"))
                    implementation(libs.lib("coil-networkKtor3"))
                }
                sourceSets.commonTest.dependencies {
                    implementation(libs.lib("kotlinx-coroutinesTest"))
                    implementation(libs.lib("ktor-clientMock"))
                }
            }
        }
    }
}
