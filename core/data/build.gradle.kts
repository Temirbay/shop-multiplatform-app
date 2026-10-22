plugins {
    alias(libs.plugins.shop.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api: StateFlow — часть публичного CartRepository, модулям-потребителям нужен этот тип
            api(libs.kotlinx.coroutinesCore)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutinesTest)
        }
    }
}
