package kz.mtemirbay.shop.core.network

/**
 * Откуда сеть берёт токен. Реализует core:data (сессия), а core:network о сессии ничего не знает —
 * так сетевой модуль не зависит от слоя данных
 */
interface AuthTokenProvider {
    fun currentToken(): String?

    /** Сервер ответил 401 на запрос с токеном: токен истёк или отозван */
    fun onUnauthorized()
}
