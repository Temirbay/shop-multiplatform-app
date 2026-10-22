package kz.mtemirbay.shop.core.network

import kotlin.coroutines.cancellation.CancellationException

/**
 * Выполняет запрос и заворачивает ошибку в Result.
 * Не runCatching: он перехватывает и CancellationException, и отменённая корутина продолжила бы работу.
 * Ловим Throwable, а не Exception: на Wasm Ktor бросает JsError — наследник Throwable.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
