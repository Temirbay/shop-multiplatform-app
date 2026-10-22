package kz.mtemirbay.shop.feature.product.data

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kz.mtemirbay.shop.core.network.createHttpClient
import kz.mtemirbay.shop.feature.product.domain.ProductNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class ProductRepositoryImplTest {

    @Test
    fun okResponseIsMappedToDomain() = runTest {
        val json = """{"id":1,"title":"Смартфон","description":"Описание","priceKzt":189990,"imageUrl":"img","categoryId":1}"""
        val repository = repositoryRespondingWith(HttpStatusCode.OK, json)

        val product = repository.getProduct(1).getOrThrow()

        assertEquals("Смартфон", product.title)
        assertEquals(189_990, product.priceKzt)
    }

    @Test
    fun notFoundIsMappedToDomainError() = runTest {
        val repository = repositoryRespondingWith(HttpStatusCode.NotFound, "Product 999 not found")

        assertIs<ProductNotFoundException>(repository.getProduct(999).exceptionOrNull())
    }

    @Test
    fun serverErrorStaysGenericError() = runTest {
        val repository = repositoryRespondingWith(HttpStatusCode.InternalServerError, "Internal server error")

        val error = repository.getProduct(1).exceptionOrNull()

        assertFalse(error is ProductNotFoundException)
    }

    private fun repositoryRespondingWith(status: HttpStatusCode, body: String) = ProductRepositoryImpl(
        ProductApi(
            createHttpClient(
                baseUrl = "http://test.local/",
                engine = MockEngine {
                    respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
                },
            ),
        ),
    )
}
