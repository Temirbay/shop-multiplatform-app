package com.example.shop_multiplatform_app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform