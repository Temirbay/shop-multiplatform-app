plugins {
    alias(libs.plugins.shop.kmp.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Дата заказа в местном часовом поясе: в stdlib нет часовых поясов и календарных дат
            implementation(libs.kotlinx.datetime)
        }
    }
}
