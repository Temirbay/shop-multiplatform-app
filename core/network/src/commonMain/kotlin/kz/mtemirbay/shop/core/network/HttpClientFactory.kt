package kz.mtemirbay.shop.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * @param engine null — движок платформы из зависимостей (OkHttp, Darwin, Js).
 * В тестах передаётся MockEngine, чтобы проверить код с той же настройкой клиента, что и в приложении.
 */
fun createHttpClient(
    baseUrl: String,
    engine: HttpClientEngine? = null,
    tokenProvider: AuthTokenProvider? = null,
): HttpClient =
    if (engine == null) {
        HttpClient { configureShopClient(baseUrl, tokenProvider) }
    } else {
        HttpClient(engine) { configureShopClient(baseUrl, tokenProvider) }
    }

private fun HttpClientConfig<*>.configureShopClient(baseUrl: String, tokenProvider: AuthTokenProvider?) {
    tokenProvider?.let { install(bearerSessionPlugin(it)) }

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
    // Content-Type здесь не задаём: у GET нет тела, а лишний заголовок заставляет браузер
    // слать CORS preflight (OPTIONS). Для запросов с телом его ставит ContentNegotiation
    defaultRequest {
        url(baseUrl)
    }
}
