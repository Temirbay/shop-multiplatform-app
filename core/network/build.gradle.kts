plugins {
    alias(libs.plugins.shop.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.serializationKotlinxJson)
            implementation(libs.ktor.clientLogging)
            implementation(libs.koin.core)
        }
        // Движок на каждой платформе свой. HttpClient() без аргументов сам находит его в зависимостях
        androidMain.dependencies {
            implementation(libs.ktor.clientOkhttp)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.clientOkhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.clientDarwin)
        }
        webMain.dependencies {
            implementation(libs.ktor.clientJs)
        }
        commonTest.dependencies {
            implementation(libs.ktor.clientMock)
            implementation(libs.kotlinx.coroutinesTest)
        }
    }
}
