package kz.mtemirbay.shop.core.network

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.bearerAuth
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

/**
 * Добавляет «Authorization: Bearer …» к каждому запросу, если пользователь вошёл.
 * Не стандартный Auth-плагин Ktor: тот кэширует токен, и после входа/выхода его пришлось бы сбрасывать вручную.
 * Здесь токен читается заново при каждом запросе
 */
internal fun bearerSessionPlugin(tokenProvider: AuthTokenProvider) = createClientPlugin("BearerSession") {
    onRequest { request, _ ->
        tokenProvider.currentToken()?.let { request.bearerAuth(it) }
    }
    onResponse { response ->
        // 401 без токена — это неверный пароль при входе, а не истёкшая сессия: выходить не из чего
        val sentToken = response.call.request.headers[HttpHeaders.Authorization] != null
        if (response.status == HttpStatusCode.Unauthorized && sentToken) tokenProvider.onUnauthorized()
    }
}
