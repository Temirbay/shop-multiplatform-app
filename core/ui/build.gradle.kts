plugins {
    alias(libs.plugins.shop.kmp.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:designsystem"))
        }
    }
}
