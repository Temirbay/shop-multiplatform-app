package kz.mtemirbay.shop.core.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BearerSessionPluginTest {

    private val tokenProvider = FakeTokenProvider()
    private var sentAuthorization: String? = null
    private var responseStatus = HttpStatusCode.OK

    private val client = createHttpClient(
        baseUrl = "http://test.local/",
        engine = MockEngine { request ->
            sentAuthorization = request.headers[HttpHeaders.Authorization]
            respond("{}", responseStatus)
        },
        tokenProvider = tokenProvider,
    )

    @Test
    fun loggedInRequestHasBearerToken() = runTest {
        tokenProvider.token = "abc"

        client.get("me")

        assertEquals("Bearer abc", sentAuthorization)
    }

    @Test
    fun loggedOutRequestHasNoAuthorization() = runTest {
        client.get("products")

        assertNull(sentAuthorization)
    }

    @Test
    fun unauthorizedWithTokenLogsOut() = runTest {
        tokenProvider.token = "expired"
        responseStatus = HttpStatusCode.Unauthorized

        runCatching { client.get("me") }

        assertEquals(1, tokenProvider.unauthorizedCalls)
    }

    @Test
    fun unauthorizedWithoutTokenDoesNotLogOut() = runTest {
        responseStatus = HttpStatusCode.Unauthorized

        runCatching { client.get("auth/login") }

        assertEquals(0, tokenProvider.unauthorizedCalls)
    }

    private class FakeTokenProvider : AuthTokenProvider {
        var token: String? = null
        var unauthorizedCalls = 0

        override fun currentToken(): String? = token

        override fun onUnauthorized() {
            unauthorizedCalls++
        }
    }
}
