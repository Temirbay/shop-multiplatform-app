package kz.mtemirbay.shop.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(baseUrl: String): HttpClient = HttpClient {
    configureShopClient(baseUrl)
}

/** Общая настройка клиента. Отдельно от createHttpClient, чтобы в тестах применить её к MockEngine. */
internal fun HttpClientConfig<*>.configureShopClient(baseUrl: String) {
    // Без expectSuccess Ktor не бросает исключение на 4xx/5xx — ответ пришлось бы проверять вручную
    expectSuccess = true

    install(ContentNegotiation) {
        json(
            Json {
                // Сервер может добавить новое поле — старая версия приложения не должна из-за этого падать
                ignoreUnknownKeys = true
            },
        )
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
    }
    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.INFO
    }
    defaultRequest {
        url(baseUrl)
        contentType(ContentType.Application.Json)
    }
}
