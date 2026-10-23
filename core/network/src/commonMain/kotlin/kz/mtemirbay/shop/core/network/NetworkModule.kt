package kz.mtemirbay.shop.core.network

import org.koin.dsl.module

val networkModule = module {
    // single — один HttpClient на всё приложение: у него внутри пул соединений и потоки
    // getOrNull: если модуля с сессией нет (например, в тестах), клиент работает без токена
    single { createHttpClient(defaultBaseUrl, tokenProvider = getOrNull()) }
}
