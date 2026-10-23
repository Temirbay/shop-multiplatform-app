package kz.mtemirbay.shop

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kz.mtemirbay.shop.core.model.ApiError
import kz.mtemirbay.shop.core.model.CreateOrderRequest
import kz.mtemirbay.shop.core.model.OrderDetailsDto
import kz.mtemirbay.shop.core.model.OrderDto
import kz.mtemirbay.shop.core.model.OrderItemDto
import kz.mtemirbay.shop.core.model.OrderItemRequest
import kz.mtemirbay.shop.core.model.OrderValidationError
import kz.mtemirbay.shop.routes.PRODUCT_NOT_FOUND
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderRoutesTest {

    private val validRequest = CreateOrderRequest(
        customerName = "Мирас",
        phone = "+7 (701) 123-45-67",
        address = "Алматы, Абая 1",
        // Смартфон Nova X 189 990 ×2 + Kotlin in Action 12 990 ×1
        items = listOf(OrderItemRequest(productId = 1, quantity = 2), OrderItemRequest(productId = 11, quantity = 1)),
    )

    @Test
    fun `valid order is created with total calculated by server prices`() = shopTestApplication { client ->
        val response = client.postOrder(validRequest)

        assertEquals(HttpStatusCode.Created, response.status)
        val order = response.body<OrderDto>()
        assertEquals(392_970, order.totalKzt)
        assertEquals(3, order.itemCount)
    }

    @Test
    fun `order ids increase`() = shopTestApplication { client ->
        val first = client.postOrder(validRequest).body<OrderDto>()
        val second = client.postOrder(validRequest).body<OrderDto>()

        assertEquals(first.id + 1, second.id)
    }

    @Test
    fun `blank name returns 400 with validation code`() = shopTestApplication { client ->
        val response = client.postOrder(validRequest.copy(customerName = " "))

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(OrderValidationError.NAME_EMPTY.name, response.body<ApiError>().code)
    }

    @Test
    fun `phone without plus seven returns 400`() = shopTestApplication { client ->
        val response = client.postOrder(validRequest.copy(phone = "87011234567"))

        assertEquals(OrderValidationError.PHONE_INVALID.name, response.body<ApiError>().code)
    }

    @Test
    fun `unknown product returns 400`() = shopTestApplication { client ->
        val response = client.postOrder(validRequest.copy(items = listOf(OrderItemRequest(999, 1))))

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(PRODUCT_NOT_FOUND, response.body<ApiError>().code)
    }

    @Test
    fun `empty cart returns 400`() = shopTestApplication { client ->
        val response = client.postOrder(validRequest.copy(items = emptyList()))

        assertEquals(OrderValidationError.ITEMS_EMPTY.name, response.body<ApiError>().code)
    }

    private suspend fun HttpClient.postOrder(request: CreateOrderRequest) = post("/orders") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    @Test
    fun `first order gets id 1`() = shopTestApplication { client ->
        assertEquals(1, client.postOrder(validRequest).body<OrderDto>().id)
    }
}
