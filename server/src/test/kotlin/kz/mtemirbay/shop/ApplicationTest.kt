package kz.mtemirbay.shop

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kz.mtemirbay.shop.core.model.CategoryDto
import kz.mtemirbay.shop.core.model.ProductDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    @Test
    fun `GET products returns all products`() = shopTest { client ->
        val response = client.get("/products")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.body<List<ProductDto>>().isNotEmpty())
    }

    @Test
    fun `GET products filters by category`() = shopTest { client ->
        val products = client.get("/products?categoryId=1").body<List<ProductDto>>()

        assertTrue(products.isNotEmpty())
        assertTrue(products.all { it.categoryId == 1L })
    }

    @Test
    fun `GET products filters by query ignoring case`() = shopTest { client ->
        val products = client.get("/products?query=kotlin").body<List<ProductDto>>()

        assertEquals(listOf(11L), products.map { it.id })
    }

    @Test
    fun `GET product by id returns product`() = shopTest { client ->
        val response = client.get("/products/1")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(1L, response.body<ProductDto>().id)
    }

    @Test
    fun `GET unknown product returns 404`() = shopTest { client ->
        assertEquals(HttpStatusCode.NotFound, client.get("/products/999").status)
    }

    @Test
    fun `GET product with invalid id returns 400`() = shopTest { client ->
        assertEquals(HttpStatusCode.BadRequest, client.get("/products/abc").status)
    }

    @Test
    fun `GET categories returns categories`() = shopTest { client ->
        val categories = client.get("/categories").body<List<CategoryDto>>()

        assertTrue(categories.isNotEmpty())
    }

    private fun shopTest(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) = testApplication {
        application { module() }
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        block(client)
    }
}
