package kz.mtemirbay.shop.feature.orders.data

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kz.mtemirbay.shop.core.network.createHttpClient
import kz.mtemirbay.shop.feature.orders.domain.OrderLine
import kz.mtemirbay.shop.feature.orders.domain.OrderNotFoundException
import kz.mtemirbay.shop.feature.orders.domain.OrderSummary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class OrdersRepositoryImplTest {

    @Test
    fun myOrdersAreParsed() = runTest {
        val repository = repositoryRespondingWith(
            HttpStatusCode.OK,
            """[{"id":3,"totalKzt":459970,"itemCount":3,"createdAt":1792731720000}]""",
        )

        assertEquals(listOf(OrderSummary(3, 459_970, 3, 1_792_731_720_000)), repository.getMyOrders().getOrThrow())
    }

    @Test
    fun orderDetailsAreParsed() = runTest {
        val repository = repositoryRespondingWith(
            HttpStatusCode.OK,
            """{"id":3,"customerName":"Мирас","phone":"+77011234567","address":"Алматы","totalKzt":459970,"createdAt":1,
               "items":[{"productId":3,"title":"Ноутбук Air 14","quantity":1,"priceKzt":449990}]}""",
        )

        val order = repository.getOrder(3).getOrThrow()

        assertEquals(listOf(OrderLine("Ноутбук Air 14", 1, 449_990)), order.lines)
    }

    @Test
    fun notFoundBecomesDomainError() = runTest {
        val repository = repositoryRespondingWith(HttpStatusCode.NotFound, "Order 999 not found")

        assertIs<OrderNotFoundException>(repository.getOrder(999).exceptionOrNull())
    }

    private fun repositoryRespondingWith(status: HttpStatusCode, body: String) = OrdersRepositoryImpl(
        OrdersApi(
            createHttpClient(
                baseUrl = "http://test.local/",
                engine = MockEngine { respond(body, status, headersOf(HttpHeaders.ContentType, "application/json")) },
            ),
        ),
    )
}
