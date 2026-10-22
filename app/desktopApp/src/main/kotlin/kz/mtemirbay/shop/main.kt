package kz.mtemirbay.shop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kz.mtemirbay.shop.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Shopmultiplatformapp",
        ) {
            App()
        }
    }
}