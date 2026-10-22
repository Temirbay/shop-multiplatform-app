package kz.mtemirbay.shop

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform