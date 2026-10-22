package kz.mtemirbay.shop.feature.checkout.data

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kz.mtemirbay.shop.core.data.cart.CartItem
import kz.mtemirbay.shop.core.model.CreateOrderRequest
import kz.mtemirbay.shop.core.model.OrderItemRequest
import kz.mtemirbay.shop.core.network.createHttpClient
import kz.mtemirbay.shop.feature.checkout.domain.OrderRejectedException
import kz.mtemirbay.shop.feature.checkout.domain.PlacedOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class CheckoutRepositoryImplTest {

    private val items = listOf(CartItem(1, "Смартфон", 189_990, "", quantity = 2))
    private var sentRequest: CreateOrderRequest? = null
    private var sentMethod: HttpMethod? = null

    @Test
    fun sendsNormalizedRequestWithoutPrices() = runTest {
        val repository = repositoryRespondingWith(HttpStatusCode.Created, """{"id":1042,"totalKzt":379980,"itemCount":2}""")

        repository.placeOrder(" Мирас ", "+7 (701) 123-45-67", " Алматы, Абая 1 ", items)

        assertEquals(HttpMethod.Post, sentMethod)
        assertEquals(
            CreateOrderRequest("Мирас", "+77011234567", "Алматы, Абая 1", listOf(OrderItemRequest(1, 2))),
            sentRequest,
        )
    }

    @Test
    fun createdResponseIsMappedToPlacedOrder() = runTest {
        val repository = repositoryRespondingWith(HttpStatusCode.Created, """{"id":1042,"totalKzt":379980,"itemCount":2}""")

        val result = repository.placeOrder("Мирас", "+77011234567", "Алматы, Абая 1", items)

        assertEquals(PlacedOrder(1042, 379_980), result.getOrThrow())
    }

    @Test
    fun badRequestWithApiErrorBecomesOrderRejected() = runTest {
        val repository = repositoryRespondingWith(HttpStatusCode.BadRequest, """{"code":"PHONE_INVALID","message":"x"}""")

        val error = repository.placeOrder("Мирас", "+7", "Алматы, Абая 1", items).exceptionOrNull()

        assertIs<OrderRejectedException>(error)
        assertEquals("PHONE_INVALID", error.code)
    }

    @Test
    fun serverErrorIsNotRejection() = runTest {
        val repository = repositoryRespondingWith(HttpStatusCode.InternalServerError, "Internal server error")

        val error = repository.placeOrder("Мирас", "+77011234567", "Алматы, Абая 1", items).exceptionOrNull()

        assertFalse(error is OrderRejectedException)
    }

    private fun repositoryRespondingWith(status: HttpStatusCode, body: String) = CheckoutRepositoryImpl(
        OrderApi(
            createHttpClient(
                baseUrl = "http://test.local/",
                engine = MockEngine { request ->
                    sentMethod = request.method
                    sentRequest = Json.decodeFromString(request.body.toByteArray().decodeToString())
                    respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
                },
            ),
        ),
    )
}
