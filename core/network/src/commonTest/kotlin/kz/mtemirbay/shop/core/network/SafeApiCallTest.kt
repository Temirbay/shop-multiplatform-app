package kz.mtemirbay.shop.core.network

import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class SafeApiCallTest {

    @Test
    fun successIsWrapped() = runTest {
        assertEquals(Result.success(42), safeApiCall { 42 })
    }

    @Test
    fun throwableIsWrappedNotOnlyException() = runTest {
        // Как JsError на Wasm: наследник Throwable, но не Exception
        class PlatformError : Throwable("fetch failed")

        val result = safeApiCall { throw PlatformError() }

        assertIs<PlatformError>(result.exceptionOrNull())
    }

    @Test
    fun cancellationIsRethrown() = runTest {
        assertFailsWith<CancellationException> {
            safeApiCall { throw CancellationException("cancelled") }
        }
    }
}
