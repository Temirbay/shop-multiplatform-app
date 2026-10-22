package kz.mtemirbay.shop.core.network

import org.koin.dsl.module

val networkModule = module {
    // single — один HttpClient на всё приложение: у него внутри пул соединений и потоки
    single { createHttpClient(defaultBaseUrl) }
}
