plugins {
    alias(libs.plugins.shop.kmp.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:ui"))
            implementation(project(":core:network"))
            implementation(libs.koin.core)
            implementation(libs.koin.composeViewModel)
            implementation(libs.coil.compose)
            implementation(libs.coil.networkKtor3)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.ktor.clientMock)
        }
    }
}
