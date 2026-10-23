plugins {
    alias(libs.plugins.shop.kmp.library)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api: StateFlow — часть публичного CartRepository, модулям-потребителям нужен этот тип
            api(libs.kotlinx.coroutinesCore)
            implementation(project(":core:network"))
            implementation(libs.koin.core)
            implementation(libs.kotlinx.serialization.json)
            // api: Settings — в публичном конструкторе SettingsCartStorage
            api(libs.multiplatformSettings.core)
            // Settings() без аргументов: SharedPreferences / NSUserDefaults / Preferences / localStorage
            implementation(libs.multiplatformSettings.noArg)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.multiplatformSettings.test)
        }
    }
}
