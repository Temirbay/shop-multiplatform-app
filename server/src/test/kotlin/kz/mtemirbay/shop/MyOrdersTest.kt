package kz.mtemirbay.shop

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kz.mtemirbay.shop.core.model.AuthResponse
import kz.mtemirbay.shop.core.model.CreateOrderRequest
import kz.mtemirbay.shop.core.model.OrderDetailsDto
import kz.mtemirbay.shop.core.model.OrderDto
import kz.mtemirbay.shop.core.model.OrderItemDto
import kz.mtemirbay.shop.core.model.OrderItemRequest
import kz.mtemirbay.shop.core.model.OrderSummaryDto
import kz.mtemirbay.shop.core.model.RegisterRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MyOrdersTest {

    private val order = CreateOrderRequest(
        customerName = "Мирас",
        phone = "+77011234567",
        address = "Алматы, Абая 1",
        items = listOf(OrderItemRequest(productId = 1, quantity = 2), OrderItemRequest(productId = 11, quantity = 1)),
    )

    @Test
    fun `order placed with token appears in my orders`() = shopTestApplication { client ->
        val token = client.registerUser("+77011234567")
        val placed = client.postOrder(order, token).body<OrderDto>()

        val myOrders = client.get("/orders") { bearerAuth(token) }.body<List<OrderSummaryDto>>()

        assertEquals(listOf(placed.id), myOrders.map { it.id })
        assertEquals(3, myOrders.single().itemCount)
        assertEquals(392_970, myOrders.single().totalKzt)
        assertTrue(myOrders.single().createdAt > 0)
    }

    @Test
    fun `newest orders come first`() = shopTestApplication { client ->
        val token = client.registerUser("+77011234567")
        val first = client.postOrder(order, token).body<OrderDto>()
        val second = client.postOrder(order, token).body<OrderDto>()

        val myOrders = client.get("/orders") { bearerAuth(token) }.body<List<OrderSummaryDto>>()

        assertEquals(listOf(second.id, first.id), myOrders.map { it.id })
    }

    @Test
    fun `order details include items with price snapshot`() = shopTestApplication { client ->
        val token = client.registerUser("+77011234567")
        val placed = client.postOrder(order, token).body<OrderDto>()

        val details = client.get("/orders/${placed.id}") { bearerAuth(token) }.body<OrderDetailsDto>()

        assertEquals("Алматы, Абая 1", details.address)
        assertEquals(
            listOf(
                OrderItemDto(productId = 1, title = "Смартфон Nova X", quantity = 2, priceKzt = 189_990),
                OrderItemDto(productId = 11, title = "Kotlin in Action", quantity = 1, priceKzt = 12_990),
            ),
            details.items,
        )
    }

    @Test
    fun `guest order is accepted but belongs to nobody`() = shopTestApplication { client ->
        val guest = client.postOrder(order, token = null)
        assertEquals(HttpStatusCode.Created, guest.status)

        val token = client.registerUser("+77011234567")
        val myOrders = client.get("/orders") { bearerAuth(token) }.body<List<OrderSummaryDto>>()

        assertEquals(emptyList(), myOrders)
    }

    @Test
    fun `other user's order is invisible and returns 404`() = shopTestApplication { client ->
        val aliceToken = client.registerUser("+77011111111")
        val bobToken = client.registerUser("+77022222222")
        val aliceOrder = client.postOrder(order, aliceToken).body<OrderDto>()

        val bobOrders = client.get("/orders") { bearerAuth(bobToken) }.body<List<OrderSummaryDto>>()
        val bobOpensAliceOrder = client.get("/orders/${aliceOrder.id}") { bearerAuth(bobToken) }

        assertEquals(emptyList(), bobOrders)
        assertEquals(HttpStatusCode.NotFound, bobOpensAliceOrder.status)
    }

    @Test
    fun `my orders require token`() = shopTestApplication { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.get("/orders").status)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/orders/1").status)
    }

    private suspend fun HttpClient.registerUser(phone: String): String =
        post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(name = "User", phone = phone, password = "password1"))
        }.body<AuthResponse>().token

    private suspend fun HttpClient.postOrder(request: CreateOrderRequest, token: String?) = post("/orders") {
        token?.let { bearerAuth(it) }
        contentType(ContentType.Application.Json)
        setBody(request)
    }
}
