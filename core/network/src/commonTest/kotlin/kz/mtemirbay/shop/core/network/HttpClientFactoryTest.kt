package kz.mtemirbay.shop.core.network

import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kz.mtemirbay.shop.core.model.CategoryDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HttpClientFactoryTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun relativePathIsResolvedAgainstBaseUrl() = runTest {
        var requestedUrl = ""
        val client = mockClient { request ->
            requestedUrl = request.url.toString()
            respond("[]", HttpStatusCode.OK, jsonHeaders)
        }

        client.get("categories")

        assertEquals("http://test.local/categories", requestedUrl)
    }

    @Test
    fun jsonIsParsedAndUnknownKeysAreIgnored() = runTest {
        val client = mockClient {
            respond("""[{"id":1,"title":"Книги","newField":true}]""", HttpStatusCode.OK, jsonHeaders)
        }

        val categories = client.get("categories").body<List<CategoryDto>>()

        assertEquals(listOf(CategoryDto(id = 1, title = "Книги")), categories)
    }

    @Test
    fun errorStatusThrowsException() = runTest {
        val client = mockClient { respond("Not found", HttpStatusCode.NotFound) }

        assertFailsWith<ClientRequestException> { client.get("products/999") }
    }

    private fun mockClient(handler: MockRequestHandler) =
        createHttpClient("http://test.local/", MockEngine(handler))
}
