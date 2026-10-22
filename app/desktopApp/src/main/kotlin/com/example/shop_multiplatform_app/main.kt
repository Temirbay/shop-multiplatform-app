package com.example.shop_multiplatform_app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Shopmultiplatformapp",
    ) {
        App()
    }
}