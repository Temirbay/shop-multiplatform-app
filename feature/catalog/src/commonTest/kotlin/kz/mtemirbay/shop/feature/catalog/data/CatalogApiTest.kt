package kz.mtemirbay.shop.feature.catalog.data

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kz.mtemirbay.shop.core.network.createHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals

class CatalogApiTest {

    private val requestedUrls = mutableListOf<String>()

    private val api = CatalogApi(
        createHttpClient(
            baseUrl = "http://test.local/",
            engine = MockEngine { request ->
                requestedUrls += request.url.toString()
                respond("[]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            },
        ),
    )

    @Test
    fun productsWithoutFiltersHaveNoQueryParameters() = runTest {
        api.getProducts(categoryId = null, query = "")

        assertEquals(listOf("http://test.local/products"), requestedUrls)
    }

    @Test
    fun productsWithFiltersPassQueryParameters() = runTest {
        api.getProducts(categoryId = 3, query = "kotlin")

        assertEquals(listOf("http://test.local/products?categoryId=3&query=kotlin"), requestedUrls)
    }
}
